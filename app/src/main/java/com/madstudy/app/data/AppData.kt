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
    val st: Int = 0,
    val dueDate: String = "",
    val inProgress: Boolean = false
)

@Serializable
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val minutes: Int,
    val seconds: Int = minutes * 60,
    val tasks: List<SessionTask> = emptyList(),
    val endedAt: Long = 0L,
    val subject: String = "",
    val journalId: String? = null
)

@Serializable
data class SessionTask(val id: String, val title: String, val completed: Boolean = false)

@Serializable
data class Journal(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val title: String,
    val body: String,
    val sessionId: String? = null
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
    val remind: Boolean = true,
    val examName: String = "",
    val examDate: String = ""
)

@Serializable
data class AppState(
    val theme: String = "sage",
    val lastBackup: String = "",
    val focusTaskId: String? = null,
    val focusTaskIds: List<String> = emptyList(),
    val savedTimer: TimerSnapshot? = null,
    val unlockedBadgeIds: Set<String> = emptySet(),
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

@Serializable
data class TimerSnapshot(val total: Int = 1500, val left: Int = 1500,
    val deadline: Long = 0, val running: Boolean = false, val awaitingCheckout: Boolean = false,
    val isBreak: Boolean = false, val focusTotal: Int = 1500)

fun Task.due(): LocalDate? = runCatching { LocalDate.parse(dueDate) }.getOrNull()
fun Task.isToday(): Boolean = due()?.let { it <= LocalDate.now() } ?: (day == "today")
fun migrateState(state: AppState): AppState = state.copy(tasks = state.tasks.map {
    if (it.dueDate.isBlank()) it.copy(dueDate = LocalDate.now().plusDays(if (it.day == "tomorrow") 1 else 0).toString()) else it
})

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

data class TimerUi(
    val total: Int = 25 * 60,
    val left: Int = 25 * 60,
    val running: Boolean = false,
    val awaitingCheckout: Boolean = false,
    val isBreak: Boolean = false,
    val focusTotal: Int = 25 * 60
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AppStore(app)
    private val _state = MutableStateFlow(defaultState())
    val state: StateFlow<AppState> = _state

    private val _timer = MutableStateFlow(TimerUi())
    val timer: StateFlow<TimerUi> = _timer
    private var timerJob: Job? = null
    private val saveMutex = kotlinx.coroutines.sync.Mutex()
    private var deadline = 0L
    private val appContext = app

    init {
        viewModelScope.launch {
            _state.value = migrateState(store.flow.first())
            _state.value.savedTimer?.let { t ->
                val focusTotal = t.focusTotal.coerceIn(60, 180 * 60)
                deadline = t.deadline
                val left = if (t.running) ((deadline - System.currentTimeMillis() + 999) / 1000).toInt().coerceIn(0, t.total) else t.left
                _timer.value = if (t.isBreak && left <= 0) {
                    // Istirahat sudah habis saat app ditutup — langsung siap untuk sesi berikutnya.
                    TimerUi(total = focusTotal, left = focusTotal, focusTotal = focusTotal)
                } else {
                    TimerUi(
                        total = t.total,
                        left = left,
                        running = false,
                        awaitingCheckout = t.awaitingCheckout || (t.running && left == 0 && !t.isBreak),
                        isBreak = t.isBreak,
                        focusTotal = focusTotal
                    )
                }
                if (t.running && left > 0) startTimer()
            }
        }
    }

    private fun set(s: AppState) {
        _state.value = s
        viewModelScope.launch {
            saveMutex.lock()
            try { store.save(_state.value) } finally { saveMutex.unlock() }
        }
        com.madstudy.app.notifications.StudyNotifications.scheduleReminders(appContext, s)
    }

    fun addTask(title: String, cat: String, prio: String, day: String, time: String) {
        if (title.isBlank()) return
        val date = runCatching { LocalDate.parse(day).toString() }.getOrElse { LocalDate.now().plusDays(if (day == "tomorrow") 1 else 0).toString() }
        val t = Task(title = title.trim(), cat = cat.trim().ifBlank { "Lainnya" }, prio = prio, day = day, time = time.ifBlank { "—" }, dueDate = date)
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
        val s = _state.value
        if (id == null) { set(s.copy(focusTaskId = null, focusTaskIds = emptyList())); return }
        if (s.tasks.none { it.id == id && !it.done }) return
        val ids = (s.focusTaskIds + listOfNotNull(s.focusTaskId)).distinct()
        val next = if (id in ids) ids - id else ids + id
        set(s.copy(focusTaskId = next.firstOrNull(), focusTaskIds = next))
    }

    fun deleteTask(id: String) {
        val s = _state.value
        set(
            s.copy(
                tasks = s.tasks.filterNot { it.id == id },
                focusTaskId = if (s.focusTaskId == id) null else s.focusTaskId,
                focusTaskIds = s.focusTaskIds - id
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

    fun editJournal(id: String, title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        set(_state.value.copy(journal = _state.value.journal.map {
            if (it.id == id) it.copy(title = title.trim().ifBlank { "Catatan" }, body = body.trim()) else it
        }))
    }

    fun editTask(task: Task) {
        if (task.title.isBlank() || task.due() == null) return
        set(_state.value.copy(tasks = _state.value.tasks.map { if (it.id == task.id) task.copy(title = task.title.trim()) else it }))
    }

    fun setExam(name: String, date: String): Boolean {
        if (date.isNotBlank() && runCatching { LocalDate.parse(date) }.isFailure) return false
        set(_state.value.copy(settings = _state.value.settings.copy(examName = name.trim(), examDate = date)))
        return true
    }

    fun unlockBadges(ids: Set<String>) {
        val next = _state.value.unlockedBadgeIds + ids
        if (next != _state.value.unlockedBadgeIds) set(_state.value.copy(unlockedBadgeIds = next))
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
        if (_timer.value.running || _timer.value.awaitingCheckout) return
        _timer.value = _timer.value.copy(running = true)
        deadline = System.currentTimeMillis() + _timer.value.left * 1000L
        val ids = (_state.value.focusTaskIds + listOfNotNull(_state.value.focusTaskId)).toSet()
        set(_state.value.copy(tasks = _state.value.tasks.map { if (it.id in ids && !it.done) it.copy(inProgress = true) else it }))
        persistTimer()
        com.madstudy.app.notifications.StudyNotifications.timer(appContext, deadline, _timer.value.isBreak)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timer.value.left > 0) {
                delay(1000)
                val left = ((deadline - System.currentTimeMillis() + 999) / 1000).toInt().coerceIn(0, _timer.value.total)
                _timer.value = _timer.value.copy(left = left)
            }
            finishTimer()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timer.value = _timer.value.copy(running = false)
        persistTimer()
        com.madstudy.app.notifications.StudyNotifications.cancelTimer(appContext)
    }

    fun resetTimer() {
        if (_timer.value.isBreak) { skipBreak(); return }
        if (_timer.value.total != _timer.value.left || _timer.value.awaitingCheckout) { finishEarly(); return }
        timerJob?.cancel()
        _timer.value = TimerUi(total = _timer.value.total, left = _timer.value.total, running = false, focusTotal = _timer.value.focusTotal)
        persistTimer()
    }

    fun setPreset(minutes: Int) {
        if (_timer.value.running || _timer.value.awaitingCheckout || _timer.value.isBreak || _timer.value.left != _timer.value.total) return
        timerJob?.cancel()
        val seconds = minutes.coerceIn(1, 180) * 60
        _timer.value = TimerUi(total = seconds, left = seconds, running = false, focusTotal = seconds)
        persistTimer()
    }

    fun finishEarly() {
        if (_timer.value.isBreak) { skipBreak(); return }
        pauseTimer()
        _timer.value = _timer.value.copy(awaitingCheckout = true)
        persistTimer()
    }

    /** Lewati fase istirahat dan kembali siap untuk sesi fokus berikutnya. */
    fun skipBreak() {
        if (!_timer.value.isBreak) return
        timerJob?.cancel()
        com.madstudy.app.notifications.StudyNotifications.cancelTimer(appContext)
        val total = _timer.value.focusTotal.coerceIn(60, 180 * 60)
        _timer.value = TimerUi(total = total, left = total, running = false, focusTotal = total)
        persistTimer()
    }

    private fun finishTimer() {
        timerJob?.cancel()
        com.madstudy.app.notifications.StudyNotifications.cancelTimer(appContext)
        val t = _timer.value
        if (t.isBreak) {
            val total = t.focusTotal.coerceIn(60, 180 * 60)
            _timer.value = TimerUi(total = total, left = total, running = false, focusTotal = total)
            persistTimer()
            return
        }
        _timer.value = t.copy(left = 0, running = false, awaitingCheckout = true)
        persistTimer()
    }

    private fun breakSecondsFor(focusTotal: Int): Int = if (focusTotal >= 45 * 60) 10 * 60 else 5 * 60

    fun checkout(completedIds: Set<String>, reflection: String = "") {
        val t = _timer.value
        if (!t.awaitingCheckout) return
        val s = _state.value
        val ids = (s.focusTaskIds + listOfNotNull(s.focusTaskId)).toSet()
        val selected = s.tasks.filter { it.id in ids }
        val seconds = (t.total - t.left).coerceAtLeast(0)
        val entryId = UUID.randomUUID().toString()
        val journal = reflection.takeIf { it.isNotBlank() }?.let { Journal(date = today(), title = "Refleksi sesi fokus", body = it.trim(), sessionId = entryId) }
        val entry = Session(id = entryId, date = today(), minutes = seconds / 60, seconds = seconds,
            tasks = selected.map { SessionTask(it.id, it.title, it.id in completedIds || it.done) },
            endedAt = System.currentTimeMillis(), subject = selected.map { it.cat }.distinct().joinToString(", "), journalId = journal?.id)
        set(s.copy(tasks = s.tasks.map { if (it.id in ids) it.copy(done = it.done || it.id in completedIds, inProgress = it.id !in completedIds && !it.done) else it },
            sessions = s.sessions + entry, journal = if (journal != null) listOf(journal) + s.journal else s.journal,
            focusTaskId = null, focusTaskIds = emptyList()))
        _timer.value = if (s.settings.autoBreak) {
            val breakSeconds = breakSecondsFor(t.focusTotal)
            TimerUi(total = breakSeconds, left = breakSeconds, running = false, isBreak = true, focusTotal = t.focusTotal)
        } else {
            TimerUi(total = t.focusTotal, left = t.focusTotal, running = false, focusTotal = t.focusTotal)
        }
        persistTimer()
        if (s.settings.autoBreak) startTimer()
    }

    fun cancelCheckout() {
        val total = _timer.value.focusTotal.coerceIn(60, 180 * 60)
        _timer.value = TimerUi(total = total, left = total, running = false, focusTotal = total)
        persistTimer()
    }

    private fun persistTimer() {
        val t = _timer.value
        set(_state.value.copy(savedTimer = TimerSnapshot(t.total, t.left, deadline, t.running, t.awaitingCheckout, t.isBreak, t.focusTotal)))
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
            pauseTimer()
            _timer.value = TimerUi()
            set(migrateState(parsed).copy(savedTimer = null))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resetAll() {
        pauseTimer(); _timer.value = TimerUi()
        set(defaultState())
    }
}
