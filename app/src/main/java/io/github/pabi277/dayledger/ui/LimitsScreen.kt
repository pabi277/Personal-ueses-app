package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.LimitsUiState
import io.github.pabi277.dayledger.LimitsViewModel
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle

@Composable
fun LimitsScreen(state: LimitsUiState, viewModel: LimitsViewModel) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Limits & always-allowed") }
        item {
            AppCard(title = "Focus") {
                Text(
                    "Start a focus session to temporarily block distracting apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { }) { Text("Start focus session (50 min)") }
            }
        }
        item { io.github.pabi277.dayledger.ui.components.SectionTitle("Apps") }
        if (state.apps.isEmpty()) {
            item { EmptyState("No apps found yet.") }
        } else {
            items(state.apps) { app ->
                AppCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.titleMedium)
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = app.alwaysAllowed, onCheckedChange = { viewModel.toggleAlwaysAllowed(app.packageName, it) })
                    }
                }
            }
        }
    }
}