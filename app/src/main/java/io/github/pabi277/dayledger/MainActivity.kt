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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
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

    if (screen == "onboarding") {
        OnboardingScreen(
            hasUsagePermission = state.hasUsagePermission,
            onRequestUsageAccess = { },
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
        return
    }

    val tabs = listOf("today" to "Today", "plan" to "Plan", "limits" to "Limits", "earnings" to "Earn", "learning" to "Learn", "report" to "Report", "notifications" to "Notify", "settings" to "Settings", "categories" to "Cats")
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { (key, label) ->
                    NavigationBarItem(
                        selected = screen == key,
                        onClick = { screen = key },
                        label = { Text(label) },
                        icon = { Text(label.take(1)) }
                    )
                }
            }
        }
    ) { padding ->
        when (screen) {
            "today" -> TodayScreen(state = state, onRefresh = { todayVm.refresh() })
            "categories" -> {
                CategoriesScreen(state = catState, viewModel = categoryVm)
                LaunchedEffect(Unit) { categoryVm.load() }
            }
            "notifications" -> NotificationsScreen(state = notifState, viewModel = notificationVm)
            "plan" -> PlanningScreen(state = planningState, viewModel = planningVm, dateKey = java.time.LocalDate.now().toString())
            "tomorrow" -> PlanningScreen(state = planningState, viewModel = planningVm, dateKey = java.time.LocalDate.now().plusDays(1).toString())
            "limits" -> {
                LimitsScreen(state = limitsState, viewModel = limitsVm)
                LaunchedEffect(Unit) { limitsVm.load() }
            }
            "earnings" -> {
                EarningsScreen(state = earningsState, viewModel = earningsVm)
                LaunchedEffect(Unit) { earningsVm.load() }
            }
            "learning" -> {
                LearningScreen(state = learningState, viewModel = learningVm)
                LaunchedEffect(Unit) { learningVm.load() }
            }
            "report" -> {
                WeeklyReportScreen(state = weeklyState)
                LaunchedEffect(Unit) { weeklyVm.load() }
            }
            "settings" -> SettingsScreen(onExportCsv = { exportLauncher.launch("dayledger.csv") }, onBackup = { })
        }
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
