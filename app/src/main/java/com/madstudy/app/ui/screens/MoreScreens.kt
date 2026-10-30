package com.madstudy.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madstudy.app.data.AppState
import com.madstudy.app.data.Habit
import com.madstudy.app.data.Journal
import com.madstudy.app.data.dayOff
import com.madstudy.app.data.doneTasks
import com.madstudy.app.data.streak
import com.madstudy.app.data.today
import com.madstudy.app.data.totalMin
import com.madstudy.app.ui.AppCard
import com.madstudy.app.ui.AppSheet
import com.madstudy.app.ui.BigStat
import com.madstudy.app.ui.Chip
import com.madstudy.app.ui.DividerLine
import com.madstudy.app.ui.EmptyHint
import com.madstudy.app.ui.FieldLabel
import com.madstudy.app.ui.FilterPill
import com.madstudy.app.ui.GhostButton
import com.madstudy.app.ui.IconBtn
import com.madstudy.app.ui.LocalPalette
import com.madstudy.app.ui.PageHeader
import com.madstudy.app.ui.Palettes
import com.madstudy.app.ui.Palette
import com.madstudy.app.ui.PrimaryButton
import com.madstudy.app.ui.SectionTitle
import com.madstudy.app.ui.ToggleSwitch
import com.madstudy.app.ui.paletteById
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idLocale = Locale("id", "ID")

private fun prettyDate(iso: String): String = runCatching {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("d MMM yyyy", idLocale))
}.getOrDefault(iso)

private fun unlockedBadges(state: AppState): Int {
    var n = 0
    if (state.sessions.isNotEmpty()) n++
    if (state.streak() >= 7) n++
    if (state.totalMin() >= 600) n++
    if (state.doneTasks() >= 10) n++
    if (state.journal.size >= 3) n++
    if (state.sessions.size >= 5) n++
    return n
}

/* ------------------------------------------------------------------ */
/* Profil                                                             */
/* ------------------------------------------------------------------ */

@Composable
fun ProfileScreen(state: AppState, onOpen: (String) -> Unit) {
    val p = LocalPalette.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profil", style = MaterialTheme.typography.headlineLarge)
            IconBtn(Icons.Rounded.Settings, "Pengaturan") { onOpen("settings") }
        }

        Column(Modifier.padding(top = 18.dp)) {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(p.gradA, p.gradB))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Siswa Rajin", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${state.streak()} hari belajar berturut-turut 🔥",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Chip("Level ${1 + state.sessions.size / 5}")
                }
            }
        }

        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigStat(
                    "${state.totalMin() / 60}j ${state.totalMin() % 60}m",
                    "Total fokus",
                    Modifier.weight(1f)
                )
                BigStat("${state.sessions.size}", "Sesi selesai", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigStat("${state.doneTasks()}", "Tugas selesai", Modifier.weight(1f))
                BigStat("${unlockedBadges(state)}", "Pencapaian", Modifier.weight(1f))
            }
        }

        Column(Modifier.padding(top = 18.dp)) {
            AppCard {
                ProfileRow(Icons.Rounded.BarChart, "Statistik") { onOpen("stats") }
                DividerLine()
                ProfileRow(Icons.Rounded.EditNote, "Jurnal Belajar") { onOpen("journal") }
                DividerLine()
                ProfileRow(Icons.Rounded.Checklist, "Habit Tracker") { onOpen("habits") }
                DividerLine()
                ProfileRow(Icons.Rounded.EmojiEvents, "Pencapaian") { onOpen("achievements") }
                DividerLine()
                ProfileRow(Icons.Rounded.Palette, "Tema", trailingChip = paletteById(state.theme).name) { onOpen("settings") }
                DividerLine()
                ProfileRow(Icons.Rounded.CloudUpload, "Backup & Restore") { onOpen("backup") }
                DividerLine()
                ProfileRow(Icons.Rounded.Settings, "Pengaturan") { onOpen("settings") }
            }
        }

        Text(
            "MadStudy v1.0 · 100% offline",
            Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 24.dp),
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailingChip: String? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }
        Text(title, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (trailingChip != null) {
            Chip(trailingChip)
        } else {
            Text("›", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/* ------------------------------------------------------------------ */
/* Statistik                                                          */
/* ------------------------------------------------------------------ */

@Composable
fun StatsScreen(state: AppState, onBack: () -> Unit) {
    val p = LocalPalette.current
    val days = (0 until 7).map { i -> dayOff((6 - i).toLong()) }
    val values = days.map { d -> state.sessions.filter { it.date == d }.sumOf { it.minutes } }
    val best = values.maxOrNull() ?: 0
    val avg = values.sum() / 7
    val tm = state.totalMin()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Box(Modifier.padding(top = 6.dp)) { PageHeader("Statistik", onBack = onBack) }

        Column(Modifier.padding(top = 18.dp)) {
            AppCard(gradient = true) {
                Text("Total waktu belajar", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${tm / 60}j ${tm % 60}m",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    "${state.sessions.size} sesi · rata-rata $avg mnt/hari",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 9.dp, start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("7 hari terakhir", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Chip("Terbaik ${best}m")
        }
        AppCard { WeekChart(values) }

        SectionTitle("Tugas per kategori")
        AppCard {
            val cats = state.tasks.groupBy { it.cat }
            if (cats.isEmpty()) {
                EmptyHint("Belum ada tugas.")
            } else {
                cats.forEach { (cat, list) ->
                    val done = list.count { it.done }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(cat, Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Box(Modifier.weight(1.4f).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                            Box(
                                Modifier.fillMaxWidth(done.toFloat() / list.size).height(8.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text("$done/${list.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(34.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }

        SectionTitle("Ringkasan")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigStat("${state.doneTasks()}", "Tugas selesai", Modifier.weight(1f))
            BigStat("${state.streak()}", "Streak hari", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WeekChart(values: List<Int>) {
    val p = LocalPalette.current
    val max = maxOf(30, values.maxOrNull() ?: 0)
    val shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 5.dp, bottomEnd = 5.dp)
    Column {
        Row(Modifier.fillMaxWidth().height(130.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { minutes ->
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight((minutes.toFloat() / max).coerceAtLeast(0.05f))
                            .clip(shape)
                            .background(
                                if (minutes == values.maxOrNull() && minutes > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEachIndexed { i, _ ->
                val date = dayOff((6 - i).toLong())
                Text(
                    runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("EE", idLocale)) }.getOrDefault("-"),
                    Modifier.weight(1f),
                    fontSize = 9.5.sp,
                    textAlign = TextAlign.Center,
                    color = if (date == today()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
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

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { PageHeader("Jurnal Belajar", onBack = onBack) }
                IconBtn(Icons.Rounded.Add, "Tulis jurnal") { showAdd = true }
            }
            EmptyHint("Catatan sesi & refleksi harian. Tersimpan di HP.")
        }
        if (entries.isEmpty()) {
            item { EmptyHint("Belum ada jurnal. Tulis refleksi pertamamu lewat tombol +.") }
        }
        items(entries, key = { it.id }) { entry ->
            Column(Modifier.padding(bottom = 10.dp)) {
                JournalCard(entry) { onDelete(entry.id) }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AppSheet("Jurnal baru", { showAdd = false }) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Refleksi hari ini", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            FieldLabel("Isi jurnal")
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                placeholder = { Text("Apa yang dipelajari hari ini? Apa yang masih sulit?", fontSize = 13.sp) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton("Simpan jurnal", onClick = {
                if (title.isNotBlank() || body.isNotBlank()) {
                    onAdd(title.trim(), body.trim())
                    showAdd = false
                }
            })
        }
    }
}

@Composable
private fun JournalCard(entry: Journal, onDelete: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, shape)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                prettyDate(entry.date).uppercase(),
                Modifier.weight(1f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.size(22.dp).clip(CircleShape).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
        }
        Text(entry.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        if (entry.body.isNotBlank()) {
            Text(
                entry.body,
                fontSize = 12.sp,
                lineHeight = 18.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 7.dp)
            )
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
    val days = (0 until 7).map { i -> dayOff((6 - i).toLong()) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f)) { PageHeader("Habit Tracker", onBack = onBack) }
            IconBtn(Icons.Rounded.Add, "Tambah kebiasaan") { showAdd = true }
        }
        EmptyHint("Centang setiap hari — 7 kotak terakhir menunjukkan konsistensimu.")

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp)
        ) {
            item {
                AppCard {
                    if (state.habits.isEmpty()) {
                        EmptyHint("Belum ada kebiasaan. Tambah satu untuk mulai membangun rutinitas.")
                    } else {
                        state.habits.forEachIndexed { index, habit ->
                            if (index > 0) DividerLine()
                            HabitRow(habit, days, onToggle = { onToggle(habit.id) })
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(14.dp))
                GhostButton("+ Tambah kebiasaan") { showAdd = true }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("📖") }
        val icons = listOf("📖", "✍️", "💧", "🧘", "🏃")
        AppSheet("Kebiasaan baru", { showAdd = false }) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Mis. Baca 20 menit", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            FieldLabel("Ikon")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                icons.forEach { i -> FilterPill(i, icon == i) { icon = i } }
            }
            Spacer(Modifier.height(18.dp))
            PrimaryButton("Simpan kebiasaan", onClick = {
                if (title.isNotBlank()) {
                    onAdd(title.trim(), icon)
                    showAdd = false
                }
            })
        }
    }
}

@Composable
private fun HabitRow(habit: Habit, days: List<String>, onToggle: () -> Unit) {
    val p = LocalPalette.current
    val checkedToday = habit.checks.contains(today())
    Row(
        Modifier.fillMaxWidth().padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(habit.icon, fontSize = 18.sp) }
        Column(Modifier.weight(1f)) {
            Text(habit.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                days.forEach { d ->
                    Box(
                        Modifier
                            .size(width = 12.dp, height = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (habit.checks.contains(d)) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }
        }
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (checkedToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(2.dp, if (checkedToday) MaterialTheme.colorScheme.primary else p.line, CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (checkedToday) {
                Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Pencapaian                                                         */
/* ------------------------------------------------------------------ */

private data class Badge(val icon: String, val title: String, val desc: String, val unlocked: Boolean)

@Composable
fun AchievementsScreen(state: AppState, onBack: () -> Unit) {
    val badges = listOf(
        Badge("🌱", "Langkah Pertama", "Selesaikan sesi fokus pertama", state.sessions.isNotEmpty()),
        Badge("🔥", "7 Hari Konsisten", "Streak 7 hari berturut-turut", state.streak() >= 7),
        Badge("⏳", "10 Jam Fokus", "Total 10 jam belajar", state.totalMin() >= 600),
        Badge("✅", "Kolektor Tugas", "Selesaikan 10 tugas", state.doneTasks() >= 10),
        Badge("📔", "Penulis Rajin", "Tulis 3 jurnal belajar", state.journal.size >= 3),
        Badge("🌸", "Taman Berbunga", "Kumpulkan 5 sesi fokus", state.sessions.size >= 5)
    )
    val open = badges.count { it.unlocked }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Box(Modifier.padding(top = 6.dp)) { PageHeader("Pencapaian", onBack = onBack) }
        Text(
            "$open dari ${badges.size} badge terbuka. Terus belajar untuk membuka sisanya.",
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
        Spacer(Modifier.height(18.dp))

        badges.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { badge ->
                    BadgeCard(badge, Modifier.weight(1f).fillMaxHeight())
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    val alpha = if (badge.unlocked) 1f else 0.45f
    Column(
        modifier
            .alpha(alpha)
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, if (badge.unlocked) MaterialTheme.colorScheme.primary else p.line, shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(badge.icon, fontSize = 24.sp)
        Text(
            badge.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            badge.desc,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp)
        )
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
    var showThemes by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Box(Modifier.padding(top = 6.dp)) { PageHeader("Pengaturan", onBack = onBack) }

        Column(Modifier.padding(top = 18.dp)) {
            AppCard {
                Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Target harian", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Menentukan hari valid untuk streak",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepperButton("−") { onGoal(-15) }
                        Text("${state.settings.goal}m", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                        StepperButton("+") { onGoal(15) }
                    }
                }
                DividerLine()
                Row(
                    Modifier.fillMaxWidth().clickable { showThemes = true }.padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Tema", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${Palettes.size} palet warna",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    Chip(paletteById(state.theme).name)
                }
                DividerLine()
                SettingSwitchRow("Jeda otomatis", "Mulai istirahat setelah sesi fokus", state.settings.autoBreak) { onToggle("autoBreak") }
                DividerLine()
                SettingSwitchRow("Musik otomatis", "Putar playlist saat sesi dimulai", state.settings.autoMusic) { onToggle("autoMusic") }
                DividerLine()
                SettingSwitchRow("Pengingat lokal", "Notifikasi tugas & sesi (offline)", state.settings.remind) { onToggle("remind") }
            }
        }

        Text(
            "MadStudy v1.0 · data hanya di perangkat ini",
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showThemes) {
        AppSheet("Pilih tema", { showThemes = false }) {
            Text(
                "${Palettes.size} tema · terang & gelap",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Palettes.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { palette ->
                        ThemeTile(palette, palette.id == state.theme, Modifier.weight(1f)) {
                            onTheme(palette.id)
                            showThemes = false
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(9.dp)
    Box(
        Modifier
            .size(28.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, p.line, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingSwitchRow(title: String, subtitle: String, checked: Boolean, onChange: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        ToggleSwitch(checked, onChange)
    }
}

@Composable
private fun ThemeTile(palette: Palette, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) palette.scheme.primary else palette.line, shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            palette.swatch.forEach { c ->
                Box(Modifier.size(22.dp).clip(RoundedCornerShape(8.dp)).background(c))
            }
        }
        Text(
            palette.name,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 9.dp),
            color = if (selected) palette.scheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            palette.note,
            fontSize = 10.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
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
                status = "Backup tersimpan."
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
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Box(Modifier.padding(top = 6.dp)) { PageHeader("Backup & Restore", onBack = onBack) }

        Column(Modifier.padding(top = 18.dp)) {
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🗂️", fontSize = 24.sp)
                    Column(Modifier.weight(1f)) {
                        Text("Backup data lokal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Semua tugas, sesi, jurnal, habit & pengaturan dalam satu file JSON.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Text(
                    "Backup terakhir: ${state.lastBackup.ifBlank { "belum pernah" }}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Spacer(Modifier.height(18.dp))
                PrimaryButton("Ekspor backup (.json)", onClick = { exporter.launch("madstudy-backup.json") })
                Spacer(Modifier.height(10.dp))
                GhostButton("Impor dari file") { importer.launch(arrayOf("application/json", "text/plain", "*/*")) }
                Spacer(Modifier.height(10.dp))
                GhostButton("Hapus semua data") { confirmReset = true }

                status?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp)
                    ) {
                        Text(msg, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Text(
            "File musik tidak ikut dibackup (hanya referensi). Setelah restore, hubungkan ulang folder musik bila perlu.",
            Modifier.padding(top = 14.dp, bottom = 24.dp),
            fontSize = 11.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            confirmButton = {
                TextButton(onClick = {
                    onReset()
                    confirmReset = false
                    status = "Semua data sudah dihapus."
                }) { Text("Hapus", color = Color(0xFFB3261E)) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } },
            title = { Text("Hapus semua data?") },
            text = { Text("Semua tugas, sesi, jurnal dan kebiasaan akan dihapus. Backup dulu kalau ragu.") }
        )
    }
}
