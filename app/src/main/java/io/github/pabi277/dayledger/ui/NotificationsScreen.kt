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

@Composable
fun NotificationsScreen(state: NotificationUiState, viewModel: NotificationViewModel) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Daily cap: ${state.dailyCap}")
            Spacer(Modifier.height(12.dp))
        }
        items(state.rules) { rule ->
            RuleEditor(rule, viewModel)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RuleEditor(rule: NotificationRuleEntity, viewModel: NotificationViewModel) {
    var text by remember(rule.id, rule.templateText) { mutableStateOf(rule.templateText) }
    var times by remember(rule.id, rule.times) { mutableStateOf(rule.times) }
    var qf by remember(rule.id, rule.quietFrom) { mutableStateOf(rule.quietFrom) }
    var qt by remember(rule.id, rule.quietTo) { mutableStateOf(rule.quietTo) }
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(rule.id, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = rule.enabled, onCheckedChange = { viewModel.toggle(rule.id, it) })
        }
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Wording") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { viewModel.updateText(rule.id, text) }) { Text("Save wording") }
        OutlinedTextField(value = times, onValueChange = { times = it }, label = { Text("Times (HH:mm, comma)") }, modifier = Modifier.fillMaxWidth())
        Row {
            OutlinedTextField(value = qf, onValueChange = { qf = it }, label = { Text("Quiet from") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = qt, onValueChange = { qt = it }, label = { Text("Quiet to") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = { viewModel.updateSchedule(rule.id, times, qf, qt) }) { Text("Save schedule") }
        Text("Preview: $text", style = MaterialTheme.typography.bodySmall)
    }
}
