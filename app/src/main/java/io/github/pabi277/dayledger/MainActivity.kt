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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.pabi277.dayledger.ui.CategoriesScreen
import io.github.pabi277.dayledger.ui.OnboardingScreen
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
    val state by todayVm.state.collectAsState()
    val catState by categoryVm.state.collectAsState()

    var screen by remember { mutableStateOf(if (state.hasUsagePermission) "today" else "onboarding") }

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
    }
}

@Composable
fun RowTabs(current: String, onTab: (String) -> Unit) {
    Row {
        Button(onClick = { onTab("today") }) { Text("Today") }
        Button(onClick = { onTab("categories") }) { Text("Categories") }
    }
}
