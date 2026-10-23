package io.github.pabi277.dayledger

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import io.github.pabi277.dayledger.ui.theme.DayLedgerTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.pabi277.dayledger.backup.BackupCodec
import io.github.pabi277.dayledger.backup.BackupService
import io.github.pabi277.dayledger.backup.RestoreOutcome
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import io.github.pabi277.dayledger.ui.HabitsScreen
import io.github.pabi277.dayledger.ui.InboxScreen
import io.github.pabi277.dayledger.ui.DayCloseScreen
import io.github.pabi277.dayledger.ui.RemindersScreen
import io.github.pabi277.dayledger.ui.TodayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DayLedgerTheme {
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
    val checklistVm: ChecklistViewModel = viewModel()
    val habitsVm: HabitsViewModel = viewModel()
    val inboxVm: InboxViewModel = viewModel()
    val dayCloseVm: DayCloseViewModel = viewModel()
    val remindersVm: RemindersViewModel = viewModel()
    val state by todayVm.state.collectAsState()
    val catState by categoryVm.state.collectAsState()
    val notifState by notificationVm.state.collectAsState()
    val planningState by planningVm.state.collectAsState()
    val limitsState by limitsVm.state.collectAsState()
    val earningsState by earningsVm.state.collectAsState()
    val learningState by learningVm.state.collectAsState()
    val weeklyState by weeklyVm.state.collectAsState()
    val checklistState by checklistVm.state.collectAsState()
    val habitsState by habitsVm.state.collectAsState()
    val inboxState by inboxVm.state.collectAsState()
    val dayCloseState by dayCloseVm.state.collectAsState()
    val remindersState by remindersVm.state.collectAsState()

    var screen by remember { mutableStateOf(if (state.hasUsagePermission) "today" else "onboarding") }

    // Phase 8: the checklist front door owns the first screen. Generation runs on every
    // start; the ids are deterministic, so a restart or reboot adds no duplicates.
    LaunchedEffect(Unit) { checklistVm.load() }

    // Phase 10.4: rebuild every reminder alarm on launch. The schedule is derived from
    // persisted state, so this is also the recovery path after an app update or a
    // permission re-grant - no separate repair job to forget about.
    LaunchedEffect(Unit) { remindersVm.syncSchedules() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DayLedgerDatabase.getInstance(context) }
    val backupService = remember { BackupService(database) }
    var dataStatus by remember { mutableStateOf("") }

    fun appVersionName(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }

    // Real CSV export of the stored usage rows (the Phase 6 placeholder is gone).
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val message = try {
                    val rows = database.usageDao().allRows()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        val writer = os.bufferedWriter()
                        writer.write("dateKey,packageName,foregroundMinutes,pickups,longestSessionMin,source\n")
                        for (row in rows) {
                            writer.write(
                                "${row.dateKey},${row.packageName},${row.foregroundMinutes}," +
                                    "${row.pickups},${row.longestSessionMin},${row.source}\n"
                            )
                        }
                        writer.flush()
                    }
                    "CSV exported: ${rows.size} usage rows."
                } catch (e: Exception) {
                    "CSV export failed: ${e.message ?: e::class.java.simpleName}"
                }
                withContext(Dispatchers.Main) { dataStatus = message }
            }
        }
    }

    // Versioned, complete backup through the file the owner explicitly picks.
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val message = try {
                    val document = backupService.exportDocument(
                        appVersionName = appVersionName(),
                        nowEpochMs = System.currentTimeMillis()
                    )
                    val text = BackupCodec.encode(document)
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(text.toByteArray(Charsets.UTF_8))
                    }
                    "Backup written: ${document.tables.size} tables, ${document.rowCount()} rows (schema v${document.schemaVersion})."
                } catch (e: Exception) {
                    "Backup failed: ${e.message ?: e::class.java.simpleName}"
                }
                withContext(Dispatchers.Main) { dataStatus = message }
            }
        }
    }

    // Restore from a backup the owner explicitly picks. Refused files leave the
    // database untouched; a failed write is rolled back.
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val message = try {
                    val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    } ?: ""
                    when (val decoded = BackupCodec.decode(text)) {
                        is BackupCodec.DecodeResult.Failure -> decoded.message
                        is BackupCodec.DecodeResult.Success -> when (val outcome = backupService.restore(decoded.document)) {
                            is RestoreOutcome.Restored ->
                                "Restore complete: ${outcome.rowsWritten} rows across ${outcome.tables.size} tables" +
                                    if (outcome.untouched.isEmpty()) "." else " (left alone: ${outcome.untouched.joinToString()})."
                            is RestoreOutcome.Refused -> "Restore refused: " + outcome.reasons.joinToString(" ")
                            is RestoreOutcome.Failed -> outcome.message
                        }
                    }
                } catch (e: Exception) {
                    "Restore failed: ${e.message ?: e::class.java.simpleName}"
                }
                withContext(Dispatchers.Main) { dataStatus = message }
            }
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

    // UI Design v2 primary navigation: Today · Plan · Habits · Review · More.
    // Usage, Earn, Learn, Limits, Categories, Notifications and Settings remain reachable
    // from More; nothing is stranded.
    val tabs = listOf(
        "today" to ("Today" to Icons.Default.Home),
        "plan" to ("Plan" to Icons.Default.List),
        "habits" to ("Habits" to Icons.Default.Notifications),
        "review" to ("Review" to Icons.Default.TrendingUp),
        "more" to ("More" to Icons.Default.MoreHoriz)
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { (key, pair) ->
                    val label = pair.first
                    val icon = pair.second
                    NavigationBarItem(
                        selected = screen == key,
                        onClick = { screen = key },
                        label = { Text(label) },
                        icon = { Icon(icon, contentDescription = label) }
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
        when (screen) {
            "today" -> TodayScreen(
                checklist = checklistState,
                usage = state,
                checklistVm = checklistVm,
                onRefreshUsage = { todayVm.refresh() },
                onOpenInbox = { screen = "inbox" },
                onOpenDayClose = { screen = "dayclose" }
            )
            "inbox" -> {
                InboxScreen(state = inboxState, viewModel = inboxVm, onBack = { screen = "today" })
                LaunchedEffect(Unit) { inboxVm.load() }
            }
            "dayclose" -> {
                DayCloseScreen(state = dayCloseState, viewModel = dayCloseVm, onBack = { screen = "today" })
                LaunchedEffect(Unit) { dayCloseVm.load() }
            }
            "habits" -> {
                HabitsScreen(state = habitsState, viewModel = habitsVm)
                LaunchedEffect(Unit) { habitsVm.load() }
            }
            "review" -> {
                WeeklyReportScreen(state = weeklyState)
                LaunchedEffect(Unit) { weeklyVm.load() }
            }
            "categories" -> {
                CategoriesScreen(state = catState, viewModel = categoryVm)
                LaunchedEffect(Unit) { categoryVm.load() }
            }
            "notifications" -> NotificationsScreen(state = notifState, viewModel = notificationVm)
            "reminders" -> {
                RemindersScreen(state = remindersState, viewModel = remindersVm)
            }
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
            "more" -> MoreScreen(
                onOpen = { screen = it },
                items = listOf(
                    "inbox" to "Inbox",
                    "dayclose" to "Close day",
                    "limits" to "Limits",
                    "earnings" to "Earn",
                    "learning" to "Learn",
                    "categories" to "Categories",
                    "notifications" to "Notifications",
                    "reminders" to "Reminders",
                    "tomorrow" to "Tomorrow",
                    "report" to "Weekly report",
                    "settings" to "Settings"
                )
            )
            "settings" -> SettingsScreen(
                onExportCsv = { exportLauncher.launch("dayledger.csv") },
                onBackup = {
                    val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    backupLauncher.launch("dayledger-backup-$stamp.json")
                },
                onRestore = { restoreLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) },
                status = dataStatus
            )
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

@Composable
fun MoreScreen(onOpen: (String) -> Unit, items: List<Pair<String, String>>) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier.padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        io.github.pabi277.dayledger.ui.components.ScreenTitle("More")
        io.github.pabi277.dayledger.ui.components.AppCard {
            items.forEach { (key, label) ->
                androidx.compose.material3.Button(onClick = { onOpen(key) }, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text(label)
                }
            }
        }
    }
}
