package io.github.pabi277.dayledger.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    hasUsagePermission: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRequestNotifications: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Day Ledger", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("This app reads your device's own usage statistics. Grant Usage access once in system Settings. Nothing leaves your phone.")
                Spacer(Modifier.height(8.dp))
                Text("Usage access: ${if (hasUsagePermission) "Granted" else "Not granted"}")
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text("Open Usage access settings") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRequestNotifications) { Text("Request notification permission") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onContinue) { Text("Continue") }
    }
}
