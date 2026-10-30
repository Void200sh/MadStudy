package com.madstudy.app.data

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val cat: String = "Lainnya",
    val prio: String = "med",
    val time: String = "—",
    val day: String = "today",
    val done: Boolean = false,
    val sd: Int = 0,
    val st: Int = 0
)

@Serializable
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val minutes: Int
)

@Serializable
data class Journal(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val title: String,
    val body: String
)

@Serializable
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val icon: String = "📖",
    val checks: List<String> = emptyList()
)

@Serializable
data class Settings(
    val goal: Int = 60,
    val autoBreak: Boolean = true,
    val autoMusic: Boolean = false,
    val remind: Boolean = true
)

@Serializable
data class AppState(
    val theme: String = "sage",
    val lastBackup: String = "",
    val focusTaskId: String? = null,
    val settings: Settings = Settings(),
    val tasks: List<Task> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val journal: List<Journal> = emptyList(),
    val habits: List<Habit> = emptyList()
)

val isoDate: (LocalDate) -> String = { it.toString() }
fun today(): String = LocalDate.now().toString()
fun dayOff(n: Long): String = LocalDate.now().minusDays(n).toString()

fun defaultState(): AppState = AppState()

fun AppState.todayMin(): Int = sessions.filter { it.date == today() }.sumOf { it.minutes }
fun AppState.totalMin(): Int = sessions.sumOf { it.minutes }
fun AppState.doneTasks(): Int = tasks.count { it.done }
fun AppState.todaySessions(): Int = sessions.count { it.date == today() }
fun AppState.streak(): Int {
    var n = 0
    for (i in 0 until 90) {
        val d = dayOff(i.toLong())
        val minutes = sessions.filter { it.date == d }.sumOf { it.minutes }
        if (minutes >= settings.goal) n++
        else if (i == 0) continue
        else break
    }
    return n
}

private val Context.dataStore by preferencesDataStore(name = "madstudy")

class AppStore(private val context: Context) {
    private val key = stringPreferencesKey("state_json")
    private val json = Json { ignoreUnknownKeys = true }

    val flow: Flow<AppState> = context.dataStore.data.map { prefs ->
        prefs[key]?.let { raw ->
            runCatching { json.decodeFromString<AppState>(raw) }.getOrNull()
        } ?: defaultState()
    }

    suspend fun save(state: AppState) {
        context.dataStore.edit { it[key] = json.encodeToString(state) }
    }
}

data class TimerUi(val total: Int = 25 * 60, val left: Int = 25 * 60, val running: Boolean = false)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AppStore(app)
    private val _state = MutableStateFlow(defaultState())
    val state: StateFlow<AppState> = _state

    private val _timer = MutableStateFlow(TimerUi())
    val timer: StateFlow<TimerUi> = _timer
    private var timerJob: Job? = null

    init {
        viewModelScope.launch { _state.value = store.flow.first() }
    }

    private fun set(s: AppState) {
        _state.value = s
        viewModelScope.launch { store.save(s) }
    }

    fun addTask(title: String, cat: String, prio: String, day: String, time: String) {
        val t = Task(title = title, cat = cat, prio = prio, day = day, time = time.ifBlank { "—" })
        set(_state.value.copy(tasks = listOf(t) + _state.value.tasks))
    }

    fun toggleTask(id: String) {
        set(_state.value.copy(tasks = _state.value.tasks.map { if (it.id == id) it.copy(done = !it.done) else it }))
    }

    fun completeTask(id: String) {
        val s = _state.value
        set(
            s.copy(
                tasks = s.tasks.map { if (it.id == id) it.copy(done = true) else it },
                focusTaskId = if (s.focusTaskId == id) null else s.focusTaskId
            )
        )
    }

    fun selectFocusTask(id: String?) {
        set(_state.value.copy(focusTaskId = id))
    }

    fun deleteTask(id: String) {
        val s = _state.value
        set(
            s.copy(
                tasks = s.tasks.filterNot { it.id == id },
                focusTaskId = if (s.focusTaskId == id) null else s.focusTaskId
            )
        )
    }

    fun addJournal(title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        val j = Journal(date = today(), title = title.ifBlank { "Catatan" }, body = body)
        set(_state.value.copy(journal = listOf(j) + _state.value.journal))
    }

    fun deleteJournal(id: String) {
        set(_state.value.copy(journal = _state.value.journal.filterNot { it.id == id }))
    }

    fun addHabit(title: String, icon: String) {
        if (title.isBlank()) return
        set(_state.value.copy(habits = _state.value.habits + Habit(title = title, icon = icon)))
    }

    fun toggleHabit(id: String) {
        val d = today()
        set(_state.value.copy(habits = _state.value.habits.map { h ->
            if (h.id != id) h
            else if (h.checks.contains(d)) h.copy(checks = h.checks - d)
            else h.copy(checks = h.checks + d)
        }))
    }

    fun setTheme(id: String) = set(_state.value.copy(theme = id))

    fun toggleSetting(key: String) {
        val s = _state.value.settings
        val next = when (key) {
            "autoBreak" -> s.copy(autoBreak = !s.autoBreak)
            "autoMusic" -> s.copy(autoMusic = !s.autoMusic)
            else -> s.copy(remind = !s.remind)
        }
        set(_state.value.copy(settings = next))
    }

    fun changeGoal(delta: Int) {
        val g = (_state.value.settings.goal + delta).coerceIn(15, 180)
        set(_state.value.copy(settings = _state.value.settings.copy(goal = g)))
    }

    fun addSession(minutes: Int) {
        if (minutes <= 0) return
        set(_state.value.copy(sessions = _state.value.sessions + Session(date = today(), minutes = minutes)))
    }

    /* ---------- timer ---------- */
    fun startTimer() {
        if (_timer.value.running) return
        _timer.value = _timer.value.copy(running = true)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timer.value.left > 0) {
                delay(1000)
                _timer.value = _timer.value.copy(left = _timer.value.left - 1)
            }
            finishTimer()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timer.value = _timer.value.copy(running = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timer.value = TimerUi(total = _timer.value.total, left = _timer.value.total, running = false)
    }

    fun setPreset(minutes: Int) {
        timerJob?.cancel()
        _timer.value = TimerUi(total = minutes * 60, left = minutes * 60, running = false)
    }

    fun finishEarly() {
        val t = _timer.value
        val spent = ((t.total - t.left) / 60f).roundToInt()
        timerJob?.cancel()
        _timer.value = TimerUi(total = t.total, left = t.total, running = false)
        if (spent >= 1) addSession(spent)
    }

    private fun finishTimer() {
        val t = _timer.value
        timerJob?.cancel()
        _timer.value = TimerUi(total = t.total, left = t.total, running = false)
        addSession((t.total / 60f).roundToInt())
    }

    /* ---------- backup ---------- */
    fun exportJson(): String {
        val json = Json { prettyPrint = true; encodeDefaults = true }
        return json.encodeToString(_state.value)
    }

    fun markBackup() {
        set(_state.value.copy(lastBackup = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("d MMM HH:mm", java.util.Locale("id", "ID"))
        )))
    }

    fun importJson(raw: String): Boolean {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<AppState>(raw)
            set(parsed)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resetAll() {
        set(defaultState())
    }
}
