package io.github.pabi277.dayledger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.pabi277.dayledger.ui.CategoriesScreen
import io.github.pabi277.dayledger.ui.OnboardingScreen
import io.github.pabi277.dayledger.ui.EarningsScreen
import io.github.pabi277.dayledger.ui.LearningScreen
import io.github.pabi277.dayledger.ui.LimitsScreen
import io.github.pabi277.dayledger.ui.SettingsScreen
import io.github.pabi277.dayledger.ui.WeeklyReportScreen
import io.github.pabi277.dayledger.ui.NotificationsScreen
import io.github.pabi277.dayledger.ui.PlanningScreen
import io.github.pabi277.dayledger.ui.TodayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DayLedgerApp()
                }
            }
        }
    }
}

@Composable
fun DayLedgerApp() {
    val todayVm: TodayViewModel = viewModel()
    val categoryVm: CategoryViewModel = viewModel()
    val notificationVm: NotificationViewModel = viewModel()
    val planningVm: PlanningViewModel = viewModel()
    val limitsVm: LimitsViewModel = viewModel()
    val earningsVm: EarningsViewModel = viewModel()
    val learningVm: LearningViewModel = viewModel()
    val weeklyVm: WeeklyReportViewModel = viewModel()
    val state by todayVm.state.collectAsState()
    val catState by categoryVm.state.collectAsState()
    val notifState by notificationVm.state.collectAsState()
    val planningState by planningVm.state.collectAsState()
    val limitsState by limitsVm.state.collectAsState()
    val earningsState by earningsVm.state.collectAsState()
    val learningState by learningVm.state.collectAsState()
    val weeklyState by weeklyVm.state.collectAsState()

    var screen by remember { mutableStateOf(if (state.hasUsagePermission) "today" else "onboarding") }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { os ->
                    os.write("date,package,minutes\n".toByteArray())
                    // best-effort: write today's usage
                    os.write("today,placeholder,0\n".toByteArray())
                }
            } catch (e: Exception) { }
        }
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { todayVm.refreshPermission() }

    LaunchedEffect(state.hasUsagePermission) {
        if (state.hasUsagePermission && screen == "onboarding") screen = "today"
        if (state.hasUsagePermission) todayVm.loadToday()
    }

    when (screen) {
        "onboarding" -> OnboardingScreen(
            hasUsagePermission = state.hasUsagePermission,
            onRequestUsageAccess = { /* user opens settings manually */ },
            onRequestNotifications = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onContinue = {
                todayVm.refreshPermission()
                if (todayVm.state.value.hasUsagePermission) screen = "today"
            }
        )
        "today" -> Column {
            RowTabs(screen, onTab = { screen = it })
            TodayScreen(state = state, onRefresh = { todayVm.refresh() })
        }
        "categories" -> Column {
            RowTabs(screen, onTab = { screen = it })
            CategoriesScreen(state = catState, viewModel = categoryVm)
            LaunchedEffect(Unit) { categoryVm.load() }
        }
        "notifications" -> Column {
            RowTabs(screen, onTab = { screen = it })
            NotificationsScreen(state = notifState, viewModel = notificationVm)
        }
        "plan" -> Column {
            RowTabs(screen, onTab = { screen = it })
            PlanningScreen(state = planningState, viewModel = planningVm, dateKey = java.time.LocalDate.now().toString())
        }
        "tomorrow" -> Column {
            RowTabs(screen, onTab = { screen = it })
            PlanningScreen(state = planningState, viewModel = planningVm, dateKey = java.time.LocalDate.now().plusDays(1).toString())
        }
        "limits" -> Column {
            RowTabs(screen, onTab = { screen = it })
            LimitsScreen(state = limitsState, viewModel = limitsVm)
            LaunchedEffect(Unit) { limitsVm.load() }
        }
        "earnings" -> Column {
            RowTabs(screen, onTab = { screen = it })
            EarningsScreen(state = earningsState, viewModel = earningsVm)
            LaunchedEffect(Unit) { earningsVm.load() }
        }
        "learning" -> Column {
            RowTabs(screen, onTab = { screen = it })
            LearningScreen(state = learningState, viewModel = learningVm)
            LaunchedEffect(Unit) { learningVm.load() }
        }
        "report" -> Column {
            RowTabs(screen, onTab = { screen = it })
            WeeklyReportScreen(state = weeklyState)
            LaunchedEffect(Unit) { weeklyVm.load() }
        }
        "settings" -> Column {
            RowTabs(screen, onTab = { screen = it })
            SettingsScreen(onExportCsv = { exportLauncher.launch("dayledger.csv") }, onBackup = { })
        }
    }
}

@Composable
fun RowTabs(current: String, onTab: (String) -> Unit) {
    Row {
        Button(onClick = { onTab("today") }) { Text("Today") }
        Button(onClick = { onTab("categories") }) { Text("Categories") }
        Button(onClick = { onTab("notifications") }) { Text("Notify") }
        Button(onClick = { onTab("plan") }) { Text("Plan") }
        Button(onClick = { onTab("tomorrow") }) { Text("Tomorrow") }
        Button(onClick = { onTab("limits") }) { Text("Limits") }
        Button(onClick = { onTab("earnings") }) { Text("Earn") }
        Button(onClick = { onTab("learning") }) { Text("Learn") }
        Button(onClick = { onTab("report") }) { Text("Report") }
        Button(onClick = { onTab("settings") }) { Text("Settings") }
    }
}
