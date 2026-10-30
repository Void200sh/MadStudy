package com.madstudy.app

import android.app.Activity
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.madstudy.app.data.AppViewModel
import com.madstudy.app.data.TimerUi
import com.madstudy.app.ui.DividerLine
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

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !palette.isDark
        controller.isAppearanceLightNavigationBars = !palette.isDark
        @Suppress("DEPRECATION")
        window.navigationBarColor = palette.scheme.background.toArgb()
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
    }

    MadStudyTheme(palette) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val route = backStack?.destination?.route ?: "home"
        val isTab = tabs.any { it.route == route }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (isTab) BottomBar(nav, route)
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.fillMaxSize()) {
                    composable("home") {
                        HomeScreen(
                            state = state,
                             onToggle = vm::toggleTask,
                            onSelectTask = vm::selectFocusTask,
                            onStartFocus = { vm.startTimer(); nav.navigate("focus") { launchSingleTop = true } },
                            onOpenTasks = { nav.navigate("tasks") },
                            onOpenSettings = { nav.navigate("settings") }
                        )
                    }
                    composable("tasks") {
                        TasksScreen(
                            state = state,
                            onToggle = vm::toggleTask,
                            onComplete = vm::completeTask,
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
                            onFinish = vm::finishEarly,
                            onSelectTask = vm::selectFocusTask,
                            onCompleteTask = vm::completeTask,
                            onCheckout = { ids -> vm.checkout(ids); nav.navigate("study-history") { launchSingleTop = true } },
                            onCancelCheckout = vm::cancelCheckout,
                            onOpenHistory = { nav.navigate("study-history") },
                            onOpenTasks = { nav.navigate("tasks") },
                            onOpenMusic = { nav.navigate("music") }
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
                    composable("study-history") {
                        com.madstudy.app.ui.screens.StudyHistoryScreen(state) { nav.popBackStack() }
                    }
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
                    MiniTimer(
                        timer = timer,
                        onPause = vm::pauseTimer,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp)
                    ) { nav.navigate("focus") }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController, route: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DividerLine()
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            tabs.forEach { tab ->
                val selected = route == tab.route
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (!selected) nav.navigate(tab.route) {
                                popUpTo("home") { saveState = false }
                                launchSingleTop = true
                            }
                        }
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(21.dp),
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(3.dp))
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniTimer(timer: TimerUi, onPause: () -> Unit, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(15.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                "Sesi fokus berjalan",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Tetap jalan saat pindah tab",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
        Text(
            mmss(timer.left),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f))
                .clickable(onClick = onPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Pause,
                contentDescription = "Jeda",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
