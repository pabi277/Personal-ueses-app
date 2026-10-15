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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.NotificationUiState
import io.github.pabi277.dayledger.NotificationViewModel
import io.github.pabi277.dayledger.data.NotificationRuleEntity
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

@Composable
fun NotificationsScreen(state: NotificationUiState, viewModel: NotificationViewModel) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Notifications") }
        item {
            AppCard(title = "Daily cap") {
                Text("Daily cap: ${state.dailyCap}", style = MaterialTheme.typography.bodyMedium)
                Text("Adjust rules below to control how often reminders appear.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.rules.isEmpty()) {
            item { EmptyState("No notification rules yet.") }
        } else {
            items(state.rules) { rule ->
                RuleEditor(rule, viewModel)
            }
        }
    }
}

@Composable
fun RuleEditor(rule: NotificationRuleEntity, viewModel: NotificationViewModel) {
    var text by remember(rule.id, rule.templateText) { mutableStateOf(rule.templateText) }
    var times by remember(rule.id, rule.times) { mutableStateOf(rule.times) }
    var qf by remember(rule.id, rule.quietFrom) { mutableStateOf(rule.quietFrom) }
    var qt by remember(rule.id, rule.quietTo) { mutableStateOf(rule.quietTo) }
    AppCard(title = rule.id) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enabled", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = rule.enabled, onCheckedChange = { viewModel.toggle(rule.id, it) })
        }
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Wording") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { viewModel.updateText(rule.id, text) }) { Text("Save wording") }
        OutlinedTextField(value = times, onValueChange = { times = it }, label = { Text("Times (HH:mm, comma)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = qf, onValueChange = { qf = it }, label = { Text("Quiet from") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = qt, onValueChange = { qt = it }, label = { Text("Quiet to") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = { viewModel.updateSchedule(rule.id, times, qf, qt) }) { Text("Save schedule") }
        Text("Preview: $text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}