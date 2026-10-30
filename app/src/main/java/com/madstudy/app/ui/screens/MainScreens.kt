package com.madstudy.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.madstudy.app.data.AppState
import com.madstudy.app.data.Task
import com.madstudy.app.data.TimerUi
import com.madstudy.app.data.doneTasks
import com.madstudy.app.data.streak
import com.madstudy.app.data.todayMin
import com.madstudy.app.data.todaySessions
import com.madstudy.app.data.totalMin
import com.madstudy.app.mmss
import com.madstudy.app.ui.AppCard
import com.madstudy.app.ui.CheckCircle
import com.madstudy.app.ui.GhostButton
import com.madstudy.app.ui.LocalPalette
import com.madstudy.app.ui.PrimaryButton
import com.madstudy.app.ui.PriorityDot
import com.madstudy.app.ui.SectionTitle
import com.madstudy.app.ui.StatTile
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun HomeScreen(state: AppState, onToggle: (String) -> Unit, onStartFocus: () -> Unit, onOpenTasks: () -> Unit) {
    val palette = LocalPalette.current
    val todayTasks = state.tasks.filter { it.day == "today" }
    val done = todayTasks.count { it.done }
    val fraction = if (todayTasks.isEmpty()) 0f else done.toFloat() / todayTasks.size

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        LocalDate.now().toString().let {
                            java.time.LocalDate.parse(it).format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale("id", "ID")))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Halo, siap belajar?", style = MaterialTheme.typography.headlineLarge)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        "S",
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (palette.pixel) {
            item {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.size(10.dp))
                        Text("Cari tugas, jurnal, kebiasaan…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌿", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.size(8.dp))
                        Text(
                            if (todayTasks.isEmpty()) "Belum ada tugas hari ini" else "${todayTasks.first().title} · ${todayTasks.first().time}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            AppCard(color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Progres hari ini", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$done dari ${todayTasks.size} tugas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 4.dp))
                        Text(
                            "${state.todayMin()} menit fokus · ${state.todaySessions()} sesi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    RingProgress(fraction)
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${state.todayMin()}m", "Fokus hari ini", Modifier.weight(1f))
                StatTile("${state.streak()}🔥", "Streak hari", Modifier.weight(1f))
                StatTile("24", "Hari ke ujian", Modifier.weight(1f))
            }
        }

        item {
            PrimaryButton("Mulai Sesi Fokus", Icons.Rounded.PlayArrow, onClick = onStartFocus)
        }

        item {
            SectionTitle("Tugas hari ini", "Lihat semua", onOpenTasks)
        }

        if (todayTasks.isEmpty()) {
            item {
                AppCard {
                    Text("Belum ada tugas hari ini.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(todayTasks.take(4), key = { it.id }) { task ->
                TaskCard(task, onToggle = { onToggle(task.id) }, onDelete = null)
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💡")
                    Text(
                        "Belajar sedikit setiap hari lebih kuat daripada belajar keras sekali seminggu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun RingProgress(fraction: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val soft = MaterialTheme.colorScheme.primaryContainer
    Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(soft, -90f, 360f, false, style = Stroke(9f, cap = StrokeCap.Round))
            drawArc(primary, -90f, 360f * fraction.coerceIn(0f, 1f), false, style = Stroke(9f, cap = StrokeCap.Round))
        }
        Text("${(fraction * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TaskCard(task: Task, onToggle: (() -> Unit), onDelete: (() -> Unit)?) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CheckCircle(task.done, onToggle)
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            task.cat,
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(task.time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (task.st > 0) {
                    Spacer(Modifier.height(8.dp))
                    val f = task.sd.toFloat() / task.st
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                        Box(Modifier.fillMaxWidth(f).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                    Text(
                        "${task.sd} dari ${task.st} subtugas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(6.dp))
                PriorityDot(task.prio)
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TasksScreen(state: AppState, onToggle: (String) -> Unit, onDelete: (String) -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var filter by remember { mutableStateOf("all") }
    var showAdd by remember { mutableStateOf(false) }

    val list = when (filter) {
        "today" -> state.tasks.filter { it.day == "today" }
        "done" -> state.tasks.filter { it.done }
        "high" -> state.tasks.filter { it.prio == "high" }
        else -> state.tasks
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tugas", style = MaterialTheme.typography.headlineLarge)
                Text("${state.tasks.size} tugas · ${state.doneTasks()} selesai", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Tambah tugas")
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all" to "Semua", "today" to "Hari ini", "done" to "Selesai", "high" to "Prioritas").forEach { (key, label) ->
                FilterChip(
                    selected = filter == key,
                    onClick = { filter = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            if (list.isEmpty()) {
                item {
                    AppCard {
                        Text("Tidak ada tugas di filter ini.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            items(list, key = { it.id }) { task ->
                TaskCard(task, onToggle = { onToggle(task.id) }, onDelete = { onDelete(task.id) })
            }
            item {
                Spacer(Modifier.height(8.dp))
                GhostButton("+ Tambah tugas baru") { showAdd = true }
            }
        }
    }

    if (showAdd) {
        AddTaskDialog(onDismiss = { showAdd = false }, onSave = { title, cat, prio, day, time ->
            onAdd(title, cat, prio, day, time)
            showAdd = false
        })
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Kalkulus") }
    var prio by remember { mutableStateOf("med") }
    var day by remember { mutableStateOf("today") }
    val cats = listOf("Kalkulus", "Biologi", "Fisika", "Sejarah", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onSave(title.trim(), cat, prio, day, time.trim()) }) {
                Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
        title = { Text("Tugas baru") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul tugas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Kategori", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cats.take(3).forEach { c ->
                        FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cats.drop(3).forEach { c ->
                        FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Prioritas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("high" to "Tinggi", "med" to "Sedang", "low" to "Rendah").forEach { (k, v) ->
                        FilterChip(selected = prio == k, onClick = { prio = k }, label = { Text(v) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Kapan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = day == "today", onClick = { day = "today" }, label = { Text("Hari ini") })
                    FilterChip(selected = day == "tomorrow", onClick = { day = "tomorrow" }, label = { Text("Besok") })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Jam (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun FocusScreen(
    state: AppState,
    timer: TimerUi,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onPreset: (Int) -> Unit,
    onFinish: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val soft = MaterialTheme.colorScheme.primaryContainer
    val fraction = if (timer.total == 0) 0f else timer.left.toFloat() / timer.total

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Sesi Fokus", style = MaterialTheme.typography.headlineLarge)
                Text("Pomodoro · ${timer.total / 60} menit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    "Hari ini ${state.todaySessions()} sesi",
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(soft, -90f, 360f, false, style = Stroke(20f, cap = StrokeCap.Round))
                    drawArc(primary, -90f, 360f * fraction, false, style = Stroke(20f, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(mmss(timer.left), style = MaterialTheme.typography.headlineLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified))
                    Text(if (timer.running) "FOKUS" else "SIAP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            listOf(25 to "25 / 5", 50 to "50 / 10", 45 to "45 / 10").forEach { (m, label) ->
                FilterChip(
                    selected = timer.total == m * 60,
                    onClick = { onPreset(m) },
                    label = { Text(label) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(52.dp).clickable(onClick = onReset)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Refresh, contentDescription = "Reset") }
            }
            Spacer(Modifier.size(16.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(74.dp).clickable { if (timer.running) onPause() else onStart() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (timer.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (timer.running) "Jeda" else "Mulai",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(52.dp).clickable(onClick = onFinish)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.SkipNext, contentDescription = "Selesaikan sesi") }
            }
        }

        SectionTitle("Untuk sesi ini")
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🌱", style = MaterialTheme.typography.headlineSmall)
                Column(Modifier.weight(1f)) {
                    Text("Focus Garden", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Selesaikan sesi untuk menumbuhkan tanamanmu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        "Level ${1 + state.sessions.size / 5}",
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${state.todayMin()}m", "Fokus hari ini", Modifier.weight(1f))
            StatTile("${state.totalMin() / 60}j", "Total jam", Modifier.weight(1f))
            StatTile("${state.sessions.size}", "Sesi", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun MusicScreen() {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) { onDispose { player.release() } }

    val tracks = remember { mutableListOf<Uri>() }
    var names by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var current by remember { mutableStateOf<Uri?>(null) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    LaunchedEffect(playing) {
        while (playing) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0)
            playing = player.isPlaying
            delay(400)
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
            if (!tracks.contains(uri)) {
                tracks.add(uri)
                names = names + (uri.toString() to displayName(context, uri))
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Musik", style = MaterialTheme.typography.headlineLarge)
                Text("Dari penyimpanan HP · offline", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { picker.launch(arrayOf("audio/*")) }) {
                Icon(Icons.Rounded.Add, contentDescription = "Tambah audio")
            }
        }

        Spacer(Modifier.height(14.dp))
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
                Text(
                    current?.let { names[it.toString()] ?: "Audio lokal" } ?: "Belum ada lagu diputar",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Audio dari perangkat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))) {
                    val f = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
                    Box(Modifier.fillMaxWidth(f).height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(mmss((position / 1000).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text(mmss((duration / 1000).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp).clickable {
                        val c = current
                        if (c == null) {
                            tracks.firstOrNull()?.let { first ->
                                current = first
                                player.setMediaItem(MediaItem.fromUri(first))
                                player.prepare()
                                player.play()
                                playing = true
                            }
                        } else if (player.isPlaying) {
                            player.pause()
                            playing = false
                        } else {
                            player.play()
                            playing = true
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Putar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        SectionTitle("Lagu lokal", "Tambah", { picker.launch(arrayOf("audio/*")) })

        if (tracks.isEmpty()) {
            AppCard {
                Text(
                    "Belum ada lagu. Tap + untuk memilih file audio dari HP.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AppCard {
                tracks.forEach { uri ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                current = uri
                                player.setMediaItem(MediaItem.fromUri(uri))
                                player.prepare()
                                player.play()
                                playing = true
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                names[uri.toString()] ?: "Audio",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (current == uri) "Sedang diputar" else "Tap untuk putar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (current == uri && playing) {
                            Icon(Icons.Rounded.Pause, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun displayName(context: android.content.Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Audio"
    } catch (e: Exception) {
        uri.lastPathSegment?.substringAfterLast('/') ?: "Audio"
    }
}
