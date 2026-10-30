package com.madstudy.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.madstudy.app.data.AppState
import com.madstudy.app.data.Habit
import com.madstudy.app.data.Journal
import com.madstudy.app.data.dayOff
import com.madstudy.app.data.doneTasks
import com.madstudy.app.data.streak
import com.madstudy.app.data.today
import com.madstudy.app.data.todayMin
import com.madstudy.app.data.totalMin
import com.madstudy.app.ui.AppCard
import com.madstudy.app.ui.GhostButton
import com.madstudy.app.ui.PageHeader
import com.madstudy.app.ui.Palettes
import com.madstudy.app.ui.PrimaryButton
import com.madstudy.app.ui.SectionTitle
import com.madstudy.app.ui.StatTile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idLocale = Locale("id", "ID")

private fun prettyDate(iso: String): String =
    runCatching {
        LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("d MMM yyyy", idLocale))
    }.getOrDefault(iso)

/* ------------------------------------------------------------------ */
/* Profil                                                             */
/* ------------------------------------------------------------------ */

@Composable
fun ProfileScreen(state: AppState, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "S",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Siswa MadStudy", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Belajar offline · tanpa akun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${state.streak()}🔥", "Streak hari", Modifier.weight(1f))
            StatTile("${state.totalMin() / 60}j", "Total fokus", Modifier.weight(1f))
            StatTile("${state.doneTasks()}", "Tugas selesai", Modifier.weight(1f))
        }

        SectionTitle("Belajar kamu")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuRow(Icons.Rounded.BarChart, "Statistik", "Grafik fokus 7 hari & kategori") { onOpen("stats") }
            MenuRow(Icons.Rounded.EditNote, "Jurnal", "${state.journal.size} catatan refleksi") { onOpen("journal") }
            MenuRow(Icons.Rounded.Repeat, "Kebiasaan", "${state.habits.size} kebiasaan aktif") { onOpen("habits") }
            MenuRow(Icons.Rounded.EmojiEvents, "Pencapaian", "Kumpulkan badge belajar") { onOpen("achievements") }
        }

        SectionTitle("Aplikasi")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuRow(Icons.Rounded.Settings, "Pengaturan", "Tema, target harian, notifikasi") { onOpen("settings") }
            MenuRow(Icons.Rounded.CloudUpload, "Backup & Restore", "Simpan data ke file JSON") { onOpen("backup") }
        }

        Spacer(Modifier.height(18.dp))
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🔒")
                Text(
                    "Semua datamu tersimpan di HP ini. Tidak ada login, tidak ada server, tidak ada iklan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Statistik                                                          */
/* ------------------------------------------------------------------ */

@Composable
fun StatsScreen(state: AppState, onBack: () -> Unit) {
    val days = (0 until 7).map { i -> dayOff((6 - i).toLong()) }
    val values = days.map { d -> d to state.sessions.filter { it.date == d }.sumOf { it.minutes } }
    val total = state.totalMin()
    val avg = if (state.sessions.isEmpty()) 0 else total / state.sessions.size
    val done = state.tasks.count { it.done }
    val totalTasks = state.tasks.size
    val fraction = if (totalTasks == 0) 0f else done.toFloat() / totalTasks

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PageHeader("Statistik", "Ringkasan belajar kamu", onBack)

        SectionTitle("Menit fokus · 7 hari terakhir")
        AppCard {
            WeekChart(values)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${state.todayMin()}m", "Hari ini", Modifier.weight(1f))
            StatTile("${total / 60}j", "Total", Modifier.weight(1f))
            StatTile("$avg m", "Rata-rata", Modifier.weight(1f))
        }

        SectionTitle("Penyelesaian tugas")
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$done dari $totalTasks tugas", style = MaterialTheme.typography.titleMedium)
                Text("${(fraction * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "${state.tasks.count { !it.done }} tugas belum selesai · ${state.tasks.count { it.prio == "high" && !it.done }} prioritas tinggi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionTitle("Per kategori")
        AppCard {
            val cats = state.tasks.groupBy { it.cat }.mapValues { (_, list) -> list.count { it.done } to list.size }
            if (cats.isEmpty()) {
                Text("Belum ada tugas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                cats.forEach { (cat, pair) ->
                    val (c, t) = pair
                    val f = if (t == 0) 0f else c.toFloat() / t
                    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text("$c/$t", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.size(8.dp))
                        Box(Modifier.size(width = 60.dp, height = 6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                            Box(Modifier.fillMaxWidth(f).height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${state.sessions.size}", "Sesi", Modifier.weight(1f))
            StatTile("${state.journal.size}", "Jurnal", Modifier.weight(1f))
            StatTile("${state.streak()}🔥", "Streak", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WeekChart(values: List<Pair<String, Int>>) {
    val primary = MaterialTheme.colorScheme.primary
    val soft = MaterialTheme.colorScheme.primaryContainer
    val maxV = (values.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Column {
        Canvas(Modifier.fillMaxWidth().height(130.dp)) {
            val gap = size.width / (values.size * 5f)
            val barW = (size.width - gap * (values.size - 1)) / values.size
            values.forEachIndexed { i, pair ->
                val x = i * (barW + gap)
                drawRoundRect(
                    soft,
                    topLeft = Offset(x, 0f),
                    size = Size(barW, size.height),
                    cornerRadius = CornerRadius(barW / 2, barW / 2)
                )
                val h = size.height * pair.second / maxV
                if (h > 0f) {
                    drawRoundRect(
                        primary,
                        topLeft = Offset(x, size.height - h),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 2, barW / 2)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            values.forEach { (date, minutes) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (minutes > 0) "$minutes" else "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        runCatching {
                            LocalDate.parse(date).format(DateTimeFormatter.ofPattern("EE", idLocale))
                        }.getOrDefault("-"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Jurnal                                                             */
/* ------------------------------------------------------------------ */

@Composable
fun JournalScreen(
    state: AppState,
    onAdd: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    val entries = state.journal.sortedByDescending { it.date }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                PageHeader("Jurnal", "${entries.size} catatan refleksi", onBack)
            }
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Tulis jurnal")
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            if (entries.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            "Belum ada catatan. Tulis refleksi singkat setelah belajar — 2 menit cukup.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(entries, key = { it.id }) { entry ->
                JournalCard(entry, onDelete = { onDelete(entry.id) })
            }
            item {
                Spacer(Modifier.height(6.dp))
                GhostButton("+ Tulis catatan baru") { showAdd = true }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank() || body.isNotBlank()) {
                        onAdd(title.trim(), body.trim())
                        showAdd = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Batal") } },
            title = { Text("Catatan baru") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Apa yang kamu pelajari hari ini?") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun JournalCard(entry: Journal, onDelete: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(prettyDate(entry.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(entry.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 3.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Hapus catatan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            if (entry.body.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(entry.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Kebiasaan                                                          */
/* ------------------------------------------------------------------ */

@Composable
fun HabitsScreen(
    state: AppState,
    onToggle: (String) -> Unit,
    onAdd: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                PageHeader("Kebiasaan", "Ceklis setiap hari, bangun rutinitas", onBack)
            }
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Tambah kebiasaan")
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            if (state.habits.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            "Belum ada kebiasaan. Mulai kecil: baca 20 menit atau review catatan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(state.habits, key = { it.id }) { habit ->
                HabitCard(habit, onToggle = { onToggle(habit.id) })
            }
            item {
                Spacer(Modifier.height(6.dp))
                GhostButton("+ Kebiasaan baru") { showAdd = true }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("📖") }
        val icons = listOf("📖", "✍️", "💧", "🏃", "🧘", "🌙")
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), icon)
                        showAdd = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Batal") } },
            title = { Text("Kebiasaan baru") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama kebiasaan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Ikon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        icons.forEach { i ->
                            FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) })
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun HabitCard(habit: Habit, onToggle: () -> Unit) {
    val days = (0 until 7).map { i -> dayOff((6 - i).toLong()) }
    val todayDate = today()
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Text(habit.icon, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(habit.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${habit.checks.size} hari · streak ${habitStreak(habit)}🔥",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                days.forEach { d ->
                    val checked = habit.checks.contains(d)
                    val isToday = d == todayDate
                    Box(
                        Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isToday && !checked) 1.5.dp else 0.dp,
                                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(11.dp)
                            )
                            .clickable(enabled = isToday, onClick = onToggle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (checked) "✓" else runCatching {
                                LocalDate.parse(d).format(DateTimeFormatter.ofPattern("EE", idLocale)).take(1)
                            }.getOrDefault("·"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun habitStreak(habit: Habit): Int {
    var start = today()
    if (!habit.checks.contains(start)) start = dayOff(1)
    var n = 0
    while (n < 365) {
        val d = runCatching { LocalDate.parse(start).minusDays(n.toLong()).toString() }.getOrNull() ?: break
        if (habit.checks.contains(d)) n++ else break
    }
    return n
}

/* ------------------------------------------------------------------ */
/* Pencapaian                                                         */
/* ------------------------------------------------------------------ */

private data class Badge(
    val icon: String,
    val title: String,
    val desc: String,
    val progress: Float,
    val detail: String
)

@Composable
fun AchievementsScreen(state: AppState, onBack: () -> Unit) {
    val badges = listOf(
        Badge(
            "🌱", "Langkah Pertama", "Selesaikan tugas pertamamu",
            (state.doneTasks().toFloat() / 1f).coerceIn(0f, 1f),
            "${state.doneTasks()}/1 tugas"
        ),
        Badge(
            "🔥", "Konsisten", "Capai streak 3 hari",
            (state.streak().toFloat() / 3f).coerceIn(0f, 1f),
            "${state.streak()}/3 hari"
        ),
        Badge(
            "⏱️", "Fokus 5 Jam", "Kumpulkan 300 menit fokus",
            (state.totalMin().toFloat() / 300f).coerceIn(0f, 1f),
            "${state.totalMin()}/300 menit"
        ),
        Badge(
            "✍️", "Penulis Jurnal", "Tulis 3 catatan refleksi",
            (state.journal.size.toFloat() / 3f).coerceIn(0f, 1f),
            "${state.journal.size}/3 catatan"
        ),
        Badge(
            "💪", "Habit Builder", "Miliki 3 kebiasaan aktif",
            (state.habits.size.toFloat() / 3f).coerceIn(0f, 1f),
            "${state.habits.size}/3 kebiasaan"
        ),
        Badge(
            "🏆", "Maraton", "Selesaikan 20 sesi fokus",
            (state.sessions.size.toFloat() / 20f).coerceIn(0f, 1f),
            "${state.sessions.size}/20 sesi"
        )
    )
    val unlocked = badges.count { it.progress >= 1f }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PageHeader("Pencapaian", "$unlocked dari ${badges.size} badge terbuka", onBack)
        Spacer(Modifier.height(16.dp))

        badges.forEach { badge ->
            val isUnlocked = badge.progress >= 1f
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Text(badge.icon, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                badge.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isUnlocked) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        "terbuka",
                                        Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            badge.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(7.dp))
                        Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)) {
                            Box(
                                Modifier
                                    .fillMaxWidth(badge.progress)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            badge.detail,
                            Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(9.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

/* ------------------------------------------------------------------ */
/* Pengaturan                                                         */
/* ------------------------------------------------------------------ */

@Composable
fun SettingsScreen(
    state: AppState,
    onTheme: (String) -> Unit,
    onToggle: (String) -> Unit,
    onGoal: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PageHeader("Pengaturan", "Sesuaikan tampilan & target", onBack)

        SectionTitle("Tema")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Palettes.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { p ->
                        val selected = state.theme == p.id
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onTheme(p.id) },
                            shape = MaterialTheme.shapes.medium,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (selected) 2.dp else 1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    p.swatch.forEach { c ->
                                        Box(Modifier.size(14.dp).clip(CircleShape).background(c))
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        p.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        p.note,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        SectionTitle("Target harian")
        AppCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Target fokus", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${state.settings.goal} menit per hari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onGoal(-15) }) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Kurangi target", modifier = Modifier.size(18.dp))
                        }
                        Text("${state.settings.goal}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onGoal(15) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Tambah target", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        SectionTitle("Preferensi")
        AppCard {
            ToggleRow("Jeda otomatis", "Sesi 5 menit setelah fokus selesai", state.settings.autoBreak) { onToggle("autoBreak") }
            Spacer(Modifier.height(4.dp))
            ToggleRow("Musik otomatis", "Putar lagu saat sesi fokus mulai", state.settings.autoMusic) { onToggle("autoMusic") }
            Spacer(Modifier.height(4.dp))
            ToggleRow("Pengingat harian", "Ingatkan tugas hari ini", state.settings.remind) { onToggle("remind") }
        }

        SectionTitle("Tentang")
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MadStudy", style = MaterialTheme.typography.titleMedium)
                Text("versi 1.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Teman belajar offline. Data tersimpan lokal di perangkatmu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = { onChange() })
    }
}

/* ------------------------------------------------------------------ */
/* Backup                                                             */
/* ------------------------------------------------------------------ */

@Composable
fun BackupScreen(
    state: AppState,
    exportJson: () -> String,
    onExported: () -> Unit,
    onImport: (String) -> Boolean,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var status by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(exportJson()) }
                    ?: error("stream null")
            }.isSuccess
            if (ok) {
                onExported()
                status = "Backup tersimpan: ${state.tasks.size} tugas, ${state.sessions.size} sesi, ${state.journal.size} jurnal."
            } else {
                status = "Gagal menyimpan file. Coba lokasi lain."
            }
        }
    }

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val raw = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            status = if (raw != null && onImport(raw)) "Backup berhasil dipulihkan." else "File tidak dikenali. Pastikan file backup MadStudy."
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PageHeader("Backup & Restore", "Simpan datamu ke file JSON", onBack)
        Spacer(Modifier.height(16.dp))

        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Backup terakhir", style = MaterialTheme.typography.titleMedium)
                Text(
                    state.lastBackup.ifBlank { "belum pernah" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${state.tasks.size} tugas · ${state.sessions.size} sesi · ${state.journal.size} jurnal · ${state.habits.size} kebiasaan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionTitle("Simpan")
        PrimaryButton("Backup ke file", Icons.Rounded.Download) {
            exporter.launch("madstudy-backup.json")
        }
        Spacer(Modifier.height(8.dp))
        GhostButton("Pulihkan dari file") {
            importer.launch(arrayOf("application/json", "text/plain", "*/*"))
        }

        status?.let { msg ->
            Spacer(Modifier.height(10.dp))
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    msg,
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        SectionTitle("Zona bahaya")
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { confirmReset = true },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = Color(0xFFB3261E))
                Column(Modifier.weight(1f)) {
                    Text("Reset semua data", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Kembali ke data contoh awal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${state.totalMin()}m", "Total fokus", Modifier.weight(1f))
            StatTile("${state.doneTasks()}", "Selesai", Modifier.weight(1f))
            StatTile("${state.streak()}🔥", "Streak", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            confirmButton = {
                TextButton(onClick = {
                    onReset()
                    confirmReset = false
                    status = "Data sudah direset ke contoh awal."
                }) { Text("Reset", color = Color(0xFFB3261E)) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } },
            title = { Text("Reset semua data?") },
            text = { Text("Semua tugas, sesi, jurnal dan kebiasaan akan diganti data contoh. Backup dulu kalau ragu.") }
        )
    }
}
