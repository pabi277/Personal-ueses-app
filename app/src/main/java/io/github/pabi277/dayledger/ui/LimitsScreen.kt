package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun LimitsScreen(state: LimitsUiState, viewModel: LimitsViewModel) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Limits & always-allowed", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* focus session placeholder */ }) { Text("Start focus session (50 min)") }
            Spacer(Modifier.height(12.dp))
        }
        items(state.apps) { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(app.label)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = app.alwaysAllowed, onCheckedChange = { viewModel.toggleAlwaysAllowed(app.packageName, it) })
            }
        }
    }
}
