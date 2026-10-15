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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.ui.components.AppCard

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
        Text("Day Ledger", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Personal usage, on your device only.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        AppCard(title = "Usage access") {
            Text("This app reads your device's own usage statistics. Grant Usage access once in system Settings. Nothing leaves your phone.")
            Text("Status: ${if (hasUsagePermission) "Granted" else "Not granted"}", style = MaterialTheme.typography.bodySmall, color = if (hasUsagePermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text("Open Usage access settings") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRequestNotifications) { Text("Request notification permission") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onContinue) { Text("Continue") }
    }
}