package com.madstudy.app

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.madstudy.app.data.AppViewModel
import com.madstudy.app.data.TimerUi
import com.madstudy.app.ui.LocalPalette
import com.madstudy.app.ui.MadStudyTheme
import com.madstudy.app.ui.paletteById
import com.madstudy.app.ui.screens.AchievementsScreen
import com.madstudy.app.ui.screens.BackupScreen
import com.madstudy.app.ui.screens.FocusScreen
import com.madstudy.app.ui.screens.HabitsScreen
import com.madstudy.app.ui.screens.HomeScreen
import com.madstudy.app.ui.screens.JournalScreen
import com.madstudy.app.ui.screens.MusicScreen
import com.madstudy.app.ui.screens.ProfileScreen
import com.madstudy.app.ui.screens.SettingsScreen
import com.madstudy.app.ui.screens.StatsScreen
import com.madstudy.app.ui.screens.TasksScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Rounded.Home),
    Tab("tasks", "Tasks", Icons.Rounded.Checklist),
    Tab("focus", "Focus", Icons.Rounded.Timer),
    Tab("music", "Music", Icons.Rounded.MusicNote),
    Tab("profile", "Profile", Icons.Rounded.Person)
)

fun mmss(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
fun MadStudyApp(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    val timer by vm.timer.collectAsState()
    val palette = paletteById(state.theme)

    MadStudyTheme(palette) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val route = backStack?.destination?.route ?: "home"
        val isTab = tabs.any { it.route == route }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (isTab) {
                    BottomBar(nav, route, palette.pixel)
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.fillMaxSize()) {
                    composable("home") {
                        HomeScreen(
                            state = state,
                            onToggle = vm::toggleTask,
                            onStartFocus = { nav.navigate("focus") },
                            onOpenTasks = { nav.navigate("tasks") }
                        )
                    }
                    composable("tasks") {
                        TasksScreen(
                            state = state,
                            onToggle = vm::toggleTask,
                            onDelete = vm::deleteTask,
                            onAdd = vm::addTask
                        )
                    }
                    composable("focus") {
                        FocusScreen(
                            state = state,
                            timer = timer,
                            onStart = vm::startTimer,
                            onPause = vm::pauseTimer,
                            onReset = vm::resetTimer,
                            onPreset = vm::setPreset,
                            onFinish = vm::finishEarly
                        )
                    }
                    composable("music") { MusicScreen() }
                    composable("profile") {
                        ProfileScreen(
                            state = state,
                            onOpen = { nav.navigate(it) }
                        )
                    }
                    composable("stats") { StatsScreen(state) { nav.popBackStack() } }
                    composable("journal") {
                        JournalScreen(state, onAdd = vm::addJournal, onDelete = vm::deleteJournal) { nav.popBackStack() }
                    }
                    composable("habits") {
                        HabitsScreen(state, onToggle = vm::toggleHabit, onAdd = vm::addHabit) { nav.popBackStack() }
                    }
                    composable("achievements") { AchievementsScreen(state) { nav.popBackStack() } }
                    composable("settings") {
                        SettingsScreen(
                            state = state,
                            onTheme = vm::setTheme,
                            onToggle = vm::toggleSetting,
                            onGoal = vm::changeGoal
                        ) { nav.popBackStack() }
                    }
                    composable("backup") {
                        BackupScreen(
                            state = state,
                            exportJson = vm::exportJson,
                            onExported = vm::markBackup,
                            onImport = vm::importJson,
                            onReset = vm::resetAll
                        ) { nav.popBackStack() }
                    }
                }

                if (timer.running && route != "focus" && isTab) {
                    MiniTimer(timer, Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
                        nav.navigate("focus")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController, route: String, pixel: Boolean) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { tab ->
            val selected = route == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) nav.navigate(tab.route) {
                        popUpTo("home") { saveState = false }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(22.dp)) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (pixel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = if (pixel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun MiniTimer(timer: TimerUi, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (palette.pill) 28.dp else 18.dp),
        color = MaterialTheme.colorScheme.primary,
        onClick = onClick
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text("Sesi fokus berjalan", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tap untuk buka timer",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                mmss(timer.left),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyState(title: String, hint: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Checklist,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
