package com.madstudy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.madstudy.app.data.AppState
import com.madstudy.app.data.TimerUi
import com.madstudy.app.ui.*

@Composable
fun CheckoutSheet(state: AppState, timer: TimerUi, onCancel: () -> Unit, onSave: (Set<String>, String) -> Unit) {
    val ids = (state.focusTaskIds + listOfNotNull(state.focusTaskId)).toSet()
    val tasks = state.tasks.filter { it.id in ids }
    var completed by remember { mutableStateOf(tasks.filter { it.done }.map { it.id }.toSet()) }
    var reflection by remember { mutableStateOf("") }
    AppSheet("Checkout sesi", onCancel) {
        Text("Waktu fokus: ${com.madstudy.app.mmss(timer.total - timer.left)}")
        Text("Centang tugas yang benar-benar selesai. Tugas lainnya tetap bisa dilanjutkan.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
            tasks.forEach { task ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    CheckCircle(task.id in completed) {
                        completed = if (task.id in completed) completed - task.id else completed + task.id
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(task.title, Modifier.weight(1f))
                }
            }
            if (tasks.isEmpty()) EmptyHint("Sesi tanpa tugas terpilih tetap dicatat ke Study History.")
        }
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.OutlinedTextField(value = reflection, onValueChange = { reflection = it }, label = { Text("Refleksi (opsional, disimpan ke jurnal)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Simpan ke Study History", onClick = { onSave(completed, reflection) })
        GhostButton("Kembali ke sesi", onClick = onCancel)
    }
}

@Composable
fun StudyHistoryScreen(state: AppState, onBack: () -> Unit) {
    val sessions = remember(state.sessions) { state.sessions.sortedWith(compareByDescending<com.madstudy.app.data.Session> { it.date }.thenByDescending { it.endedAt }) }
    val groups = remember(sessions) { sessions.groupBy { it.date } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageHeader("Study History", "Perjalanan belajarmu, satu sesi setiap waktu.", onBack) }
        item {
            AppCard(gradient = true) {
                Text("TOTAL WAKTU FOKUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(studyDuration(sessions.sumOf { it.seconds.toLong() }), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("${sessions.size} sesi")
                    Chip("${sessions.sumOf { it.tasks.count { t -> t.completed } }} tugas selesai")
                }
            }
        }
        if (sessions.isEmpty()) item { AppCard {
            Text("Belum ada sesi tersimpan", style = MaterialTheme.typography.titleMedium)
            Text("Mulai sesi fokus lalu checkout. Durasi dan tugasmu akan dirangkum di sini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }
        groups.forEach { (date, entries) ->
            item(key = "date-$date") { GroupLabel(historyDate(date)) }
            items(entries, key = { it.id }) { session ->
                AppCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (session.tasks.isEmpty()) "Fokus mandiri" else "Sesi belajar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (session.endedAt > 0) Text(java.text.SimpleDateFormat("HH:mm", java.util.Locale("id")).format(java.util.Date(session.endedAt)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Chip(studyDuration(session.seconds.toLong()))
                    }
                    if (session.tasks.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp)); DividerLine(); Spacer(Modifier.height(8.dp))
                        session.tasks.forEach { task ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(8.dp).padding(top = 2.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Column(Modifier.weight(1f)) {
                                    Text(task.title, fontWeight = FontWeight.Medium)
                                    Text(if (task.completed) "Selesai" else "Dilanjutkan nanti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun studyDuration(seconds: Long): String {
    val s = seconds.coerceAtLeast(0)
    return when {
        s >= 3600 -> "${s / 3600} jam ${(s % 3600) / 60} mnt"
        s >= 60 -> "${s / 60} mnt ${s % 60} dtk"
        else -> "$s dtk"
    }
}

private fun historyDate(date: String): String = when (date) {
    com.madstudy.app.data.today() -> "Hari ini"
    com.madstudy.app.data.dayOff(1) -> "Kemarin"
    else -> runCatching { java.time.LocalDate.parse(date).format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale("id"))) }.getOrDefault(date)
}
