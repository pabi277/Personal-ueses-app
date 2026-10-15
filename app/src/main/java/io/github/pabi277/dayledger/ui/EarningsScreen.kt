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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.EarningsUiState
import io.github.pabi277.dayledger.EarningsViewModel
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.KeyValueRow
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

@Composable
fun EarningsScreen(state: EarningsUiState, viewModel: EarningsViewModel) {
    var source by remember { mutableStateOf("ProRewards") }
    var amount by remember { mutableStateOf("12.50") }
    var task by remember { mutableStateOf("install") }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Earnings") }
        item {
            AppCard(title = "Today") {
                Text(
                    text = "₹${state.totalPaiseToday / 100}.${"%02d".format(state.totalPaiseToday % 100)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Total earned today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            AppCard(title = "Log earning") {
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Source") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = task, onValueChange = { task = it }, label = { Text("Task type") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount ₹") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    val paise = ((amount.toDoubleOrNull() ?: 0.0) * 100).toInt()
                    viewModel.logEarning(source, task, null, paise)
                }) { Text("Log earning") }
            }
        }
        item { SectionTitle("History") }
        if (state.entries.isEmpty()) {
            item { EmptyState("No earnings logged yet.") }
        } else {
            items(state.entries) { e ->
                val mins = e.attributedMinutes
                val rate = if (mins > 0) "₹%.2f/h".format(e.amountPaise / 100.0 / (mins / 60.0)) else "n/a"
                AppCard {
                    KeyValueRow("${e.source} · ${e.taskType}", "₹${e.amountPaise / 100}.${"%02d".format(e.amountPaise % 100)}")
                    Text("attributed ${mins}m · $rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}