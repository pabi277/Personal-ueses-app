package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.ScreenTitle

@Composable
fun SettingsScreen(
    onExportCsv: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit = {},
    status: String = ""
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle("Settings")
        AppCard(title = "Data") {
            Text(
                "Backup writes every table and setting to one file you pick. Restore reads it back; " +
                    "a backup from a newer app version is refused rather than half-applied.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onBackup) { Text("Back up everything") }
            OutlinedButton(onClick = onRestore) { Text("Restore from backup") }
            OutlinedButton(onClick = onExportCsv) { Text("Export CSV") }
            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
        }
        AppCard(title = "About") {
            Text(
                "Day Ledger · personal use only. Data stays on this device: Android cloud backup " +
                    "and device transfer are switched off for this app, so the file you export here is the only backup.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}