package com.madstudy.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun CheckoutSheet(state: AppState, timer: TimerUi, onCancel: () -> Unit, onSave: (Set<String>) -> Unit) {
    val ids = (state.focusTaskIds + listOfNotNull(state.focusTaskId)).toSet()
    val tasks = state.tasks.filter { it.id in ids }
    var completed by remember { mutableStateOf(tasks.filter { it.done }.map { it.id }.toSet()) }
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
        PrimaryButton("Simpan ke Study History", onClick = { onSave(completed) })
        GhostButton("Kembali ke sesi", onClick = onCancel)
    }
}

@Composable
fun StudyHistoryScreen(state: AppState, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        GhostButton("Kembali", onClick = onBack)
        Text("Study History", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        if (state.sessions.isEmpty()) EmptyHint("Sesi yang di-checkout akan muncul di sini.")
        state.sessions.asReversed().forEach { session ->
            AppCard {
                Text("${session.date} · ${com.madstudy.app.mmss(session.seconds)} fokus", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                if (session.tasks.isEmpty()) Text("Fokus mandiri", color = MaterialTheme.colorScheme.onSurfaceVariant)
                session.tasks.forEach { task ->
                    Text("${if (task.completed) "Selesai" else "Dikerjakan"} · ${task.title}")
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
