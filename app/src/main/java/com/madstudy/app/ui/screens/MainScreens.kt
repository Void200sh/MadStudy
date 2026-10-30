package com.madstudy.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.madstudy.app.data.AppState
import com.madstudy.app.data.Task
import com.madstudy.app.data.TimerUi
import com.madstudy.app.data.streak
import com.madstudy.app.data.todayMin
import com.madstudy.app.data.todaySessions
import com.madstudy.app.mmss
import com.madstudy.app.ui.AppCard
import com.madstudy.app.ui.AppSheet
import com.madstudy.app.ui.CheckCircle
import com.madstudy.app.ui.Chip
import com.madstudy.app.ui.DividerLine
import com.madstudy.app.ui.EmptyHint
import com.madstudy.app.ui.FieldLabel
import com.madstudy.app.ui.FilterPill
import com.madstudy.app.ui.GhostButton
import com.madstudy.app.ui.GroupLabel
import com.madstudy.app.ui.IconBtn
import com.madstudy.app.ui.LocalPalette
import com.madstudy.app.ui.PrimaryButton
import com.madstudy.app.ui.PriorityDot
import com.madstudy.app.ui.SectionTitle
import com.madstudy.app.ui.StatTile
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val idLocale = Locale("id", "ID")

private fun greeting(): String {
    val h = java.time.LocalTime.now().hour
    return when {
        h < 11 -> "Selamat pagi 🌿"
        h < 15 -> "Selamat siang ☀️"
        h < 19 -> "Selamat sore 🌤️"
        else -> "Selamat malam 🌙"
    }
}

@Composable
fun HomeScreen(
    state: AppState,
    onToggle: (String) -> Unit,
    onStartFocus: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val today = state.tasks.filter { it.day == "today" }
    val done = today.count { it.done }
    val fraction = if (today.isEmpty()) 0f else done.toFloat() / today.size

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", idLocale))
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(greeting(), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 4.dp))
                }
                IconBtn(Icons.Rounded.Palette, "Tema & pengaturan", onOpenSettings)
            }
        }

        item {
            Column(Modifier.padding(top = 18.dp)) {
                AppCard(gradient = true) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Progres hari ini", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$done dari ${today.size} tugas",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Text(
                                "${state.todayMin()} menit fokus · ${state.todaySessions()} sesi",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        ProgressRing(fraction)
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                        Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${state.todayMin()}m", "Fokus hari ini", Modifier.weight(1f))
                StatTile("${state.streak()}🔥", "Streak hari", Modifier.weight(1f))
                StatTile("24", "Hari ke ujian", Modifier.weight(1f))
            }
        }

        item {
            Column(Modifier.padding(top = 18.dp)) {
                PrimaryButton("Mulai Sesi Fokus", Icons.Rounded.PlayArrow, onClick = onStartFocus)
            }
        }

        item {
            SectionTitle("Tugas hari ini", "Lihat semua", onOpenTasks)
        }

        if (today.isEmpty()) {
            item { EmptyHint("Belum ada tugas hari ini. Tambah dari tab Tasks.") }
        } else {
            items(today.take(3), key = { it.id }) { task ->
                Box(Modifier.padding(bottom = 9.dp)) {
                    HomeTaskRow(task, onToggle = { onToggle(task.id) })
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("💡", fontSize = 17.sp)
                Text(
                    "“Belajar sedikit setiap hari lebih kuat daripada belajar keras sekali seminggu.”",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(fraction: Float) {
    val p = LocalPalette.current
    Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(p.scheme.primaryContainer, -90f, 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
            drawArc(p.scheme.primary, -90f, 360f * fraction.coerceIn(0f, 1f), false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
        }
        Text("${(fraction * 100).roundToInt()}%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun HomeTaskRow(task: Task, onToggle: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border1(p)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.padding(top = 1.dp)) { CheckCircle(task.done, onToggle) }
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(task.cat, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(task.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Box(Modifier.padding(top = 6.dp)) { PriorityDot(task.prio) }
    }
}

private fun Modifier.border1(p: com.madstudy.app.ui.Palette): Modifier =
    this.border(1.dp, p.line, RoundedCornerShape(18.dp))

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: (() -> Unit)?) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, shape)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.padding(top = 1.dp)) { CheckCircle(task.done, onToggle) }
            Column(Modifier.weight(1f).clickable(onClick = onToggle)) {
                Text(
                    task.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Chip(task.cat)
                    Text(task.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (task.st > 0) {
                    Spacer(Modifier.height(9.dp))
                    val f = task.sd.toFloat() / task.st
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                        Box(Modifier.fillMaxWidth(f).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                    Text(
                        "${task.sd} dari ${task.st} subtugas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
            Box(Modifier.padding(top = 6.dp)) { PriorityDot(task.prio) }
            if (onDelete != null) {
                Box(
                    Modifier.size(24.dp).clip(CircleShape).clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TasksScreen(state: AppState, onToggle: (String) -> Unit, onDelete: (String) -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var filter by remember { mutableStateOf("all") }
    var showAdd by remember { mutableStateOf(false) }

    val filtered = when (filter) {
        "today" -> state.tasks.filter { it.day == "today" }
        "done" -> state.tasks.filter { it.done }
        "high" -> state.tasks.filter { it.prio == "high" }
        else -> state.tasks
    }
    val todayTasks = filtered.filter { it.day == "today" }
    val tomorrowTasks = filtered.filter { it.day == "tomorrow" }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Tugas", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "${state.tasks.size} tugas · ${state.tasks.count { it.done }} selesai",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            IconBtn(Icons.Rounded.Add, "Tambah tugas") { showAdd = true }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all" to "Semua", "today" to "Hari ini", "done" to "Selesai", "high" to "Prioritas").forEach { (key, label) ->
                FilterPill(label, filter == key) { filter = key }
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            if (filtered.isEmpty()) {
                item { EmptyHint("Tidak ada tugas di filter ini.") }
            }
            if (todayTasks.isNotEmpty()) {
                item { GroupLabel("Hari ini") }
                items(todayTasks, key = { it.id }) { task ->
                    TaskCard(task, onToggle = { onToggle(task.id) }, onDelete = { onDelete(task.id) })
                }
            }
            if (tomorrowTasks.isNotEmpty()) {
                item { GroupLabel("Besok") }
                items(tomorrowTasks, key = { it.id }) { task ->
                    TaskCard(task, onToggle = { onToggle(task.id) }, onDelete = { onDelete(task.id) })
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                GhostButton("+ Tambah tugas baru") { showAdd = true }
            }
        }
    }

    if (showAdd) {
        AddTaskSheet(
            onDismiss = { showAdd = false },
            onSave = { title, cat, prio, day, time ->
                onAdd(title, cat, prio, day, time)
                showAdd = false
            }
        )
    }
}

@Composable
private fun AddTaskSheet(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Kalkulus") }
    var prio by remember { mutableStateOf("med") }
    var day by remember { mutableStateOf("today") }
    val cats = listOf("Kalkulus", "Biologi", "Fisika", "Sejarah", "Lainnya")

    AppSheet("Tugas baru", onDismiss) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Mis. Bab 4 — Integral", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        FieldLabel("Kategori")
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            cats.take(3).forEach { c -> FilterPill(c, cat == c) { cat = c } }
        }
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            cats.drop(3).forEach { c -> FilterPill(c, cat == c) { cat = c } }
        }
        FieldLabel("Prioritas")
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("high" to "Tinggi", "med" to "Sedang", "low" to "Rendah").forEach { (k, v) ->
                FilterPill(v, prio == k) { prio = k }
            }
        }
        FieldLabel("Kapan")
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterPill("Hari ini", day == "today") { day = "today" }
            FilterPill("Besok", day == "tomorrow") { day = "tomorrow" }
        }
        FieldLabel("Jam (opsional)")
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            placeholder = { Text("10.00", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton("Simpan tugas", onClick = {
            if (title.isNotBlank()) onSave(title.trim(), cat, prio, day, time.trim())
        })
    }
}

@Composable
fun FocusScreen(
    state: AppState,
    timer: TimerUi,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onPreset: (Int) -> Unit,
    onFinish: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMusic: () -> Unit
) {
    val p = LocalPalette.current
    val fraction = if (timer.total == 0) 0f else timer.left.toFloat() / timer.total

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Sesi Fokus", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Pomodoro · ${timer.total / 60} menit",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Chip("Hari ini ${state.todaySessions()} sesi")
        }

        Box(Modifier.fillMaxWidth().padding(top = 14.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(p.scheme.primaryContainer, 0f, 360f, false, style = Stroke(9.dp.toPx()))
                    drawArc(p.scheme.primary, -90f, 360f * fraction, false, style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        mmss(timer.left),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        if (timer.running) "Fokus" else "Siap",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.Center) {
            FilterPill("25 / 5", timer.total == 25 * 60) { onPreset(25) }
            Spacer(Modifier.width(8.dp))
            FilterPill("50 / 10", timer.total == 50 * 60) { onPreset(50) }
            Spacer(Modifier.width(8.dp))
            FilterPill("45 / 10", timer.total == 45 * 60) { onPreset(45) }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 22.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundButton(Icons.Rounded.Refresh, "Reset", 52, onReset)
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier
                    .size(72.dp)
                    .shadow(16.dp, CircleShape, clip = false, ambientColor = p.glow, spotColor = p.glow)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(p.gradA, p.gradB)))
                    .clickable { if (timer.running) onPause() else onStart() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (timer.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (timer.running) "Jeda" else "Mulai",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            RoundButton(Icons.Rounded.SkipNext, "Selesaikan sesi", 52, onFinish)
        }

        SectionTitle("Untuk sesi ini")
        SelectRow(
            emoji = "✅",
            title = "Belum pilih tugas",
            subtitle = "Opsional — fokus tetap jalan",
            onClick = onOpenTasks
        )
        Spacer(Modifier.height(10.dp))
        SelectRow(
            emoji = "🎵",
            title = "Musik pengiring",
            subtitle = "Pilih audio dari tab Music",
            onClick = onOpenMusic
        )
        Spacer(Modifier.height(14.dp))
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🌱", fontSize = 22.sp)
                Column(Modifier.weight(1f)) {
                    Text("Focus Garden", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Selesaikan sesi untuk menumbuhkan tanamanmu",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Chip("Level ${1 + state.sessions.size / 5}")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, size: Int, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .size(size.dp)
            .shadow(2.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SelectRow(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 15.sp) }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        }
        Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MusicScreen() {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) { onDispose { player.release() } }

    val p = LocalPalette.current
    val tracks = remember { mutableListOf<Uri>() }
    var names by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var current by remember { mutableStateOf<Uri?>(null) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var shuffle by remember { mutableStateOf(false) }
    var repeat by remember { mutableStateOf(false) }

    LaunchedEffect(playing) {
        while (playing) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0)
            playing = player.isPlaying
            delay(400)
        }
    }

    fun playAt(uri: Uri) {
        current = uri
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
        playing = true
    }

    fun step(delta: Int) {
        if (tracks.isEmpty()) return
        val idx = tracks.indexOf(current)
        val next = ((if (idx < 0) 0 else idx + delta) % tracks.size + tracks.size) % tracks.size
        playAt(tracks[next])
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
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Musik", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Dari penyimpanan HP · offline",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            IconBtn(Icons.Rounded.Add, "Tambah audio") { picker.launch(arrayOf("audio/*")) }
        }

        AppCard(modifier = Modifier.padding(top = 18.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(p.gradA, p.gradB, MaterialTheme.colorScheme.primaryContainer))),
                contentAlignment = Alignment.Center
            ) { Text("🎧", fontSize = 56.sp) }

            Column(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    current?.let { names[it.toString()] ?: "Audio lokal" } ?: "Belum ada lagu diputar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Audio dari perangkat",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(mmss((position / 1000).toInt()), fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                    val f = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
                    Box(Modifier.fillMaxWidth(f).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                }
                Text(mmss((duration / 1000).toInt()), fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "Acak",
                    modifier = Modifier.size(19.dp).clickable {
                        shuffle = !shuffle
                        player.shuffleModeEnabled = shuffle
                    },
                    tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Rounded.SkipPrevious,
                    contentDescription = "Sebelumnya",
                    modifier = Modifier.size(22.dp).clickable { step(-1) }
                )
                Box(
                    Modifier
                        .size(56.dp)
                        .shadow(14.dp, CircleShape, clip = false, ambientColor = p.glow, spotColor = p.glow)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(p.gradA, p.gradB)))
                        .clickable {
                            if (current == null) tracks.firstOrNull()?.let { playAt(it) }
                            else if (player.isPlaying) {
                                player.pause(); playing = false
                            } else {
                                player.play(); playing = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Putar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Berikutnya",
                    modifier = Modifier.size(22.dp).clickable { step(1) }
                )
                Icon(
                    Icons.Rounded.Repeat,
                    contentDescription = "Ulangi",
                    modifier = Modifier.size(19.dp).clickable {
                        repeat = !repeat
                        player.repeatMode = if (repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                    },
                    tint = if (repeat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SectionTitle("Playlist", "Semua") { picker.launch(arrayOf("audio/*")) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val playlists = listOf(
                Triple("Lo-Fi Study", "24 lagu", Color(0xFFC7D3E8)),
                Triple("Deep Focus", "18 lagu", Color(0xFF9FB0C9)),
                Triple("Pagi Santai", "11 lagu", Color(0xFFE8D3C7))
            )
            items(playlists.size) { i ->
                val (title, count, cover) = playlists[i]
                Column(
                    Modifier
                        .width(132.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, p.line, RoundedCornerShape(18.dp))
                        .clickable { picker.launch(arrayOf("audio/*")) }
                        .padding(12.dp)
                ) {
                    Box(
                        Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(cover, cover.copy(alpha = 0.65f))))
                    )
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 9.dp))
                    Text(count, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        SectionTitle("Lagu lokal", "+ Tambah") { picker.launch(arrayOf("audio/*")) }
        AppCard {
            if (tracks.isEmpty()) {
                EmptyHint("Belum ada lagu. Tap + untuk memilih file audio dari HP.")
            } else {
                tracks.forEachIndexed { index, uri ->
                    if (index > 0) DividerLine()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { playAt(uri) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                names[uri.toString()] ?: "Audio",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (current == uri) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (current == uri) "Sedang diputar" else "Tap untuk putar",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (current == uri && playing) {
                            Icon(Icons.Rounded.Pause, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
