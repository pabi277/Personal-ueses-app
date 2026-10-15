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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.PlanningUiState
import io.github.pabi277.dayledger.PlanningViewModel
import io.github.pabi277.dayledger.data.BlockEntity
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

fun minutesToTime(m: Int): String = "%02d:%02d".format(m / 60, m % 60)

@Composable
fun PlanningScreen(state: PlanningUiState, viewModel: PlanningViewModel, dateKey: String) {
    LaunchedEffect(dateKey) { viewModel.load(dateKey) }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Plan: ${state.dateKey}") }
        item {
            AppCard(title = "Templates") {
                if (state.templates.isEmpty()) {
                    Text("No templates yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.templates.forEach { t ->
                            OutlinedButton(onClick = { viewModel.applyTemplate(t.id) }) { Text(t.name) }
                        }
                    }
                }
                Button(onClick = { viewModel.shiftDay(15) }) { Text("Shift rest of day +15m") }
            }
        }
        item {
            AppCard(title = "Add block") {
                AddBlockForm(onAdd = { s, e, title -> viewModel.addBlock(s, e, title) })
            }
        }
        item { SectionTitle("Blocks") }
        if (state.blocks.isEmpty()) {
            item { io.github.pabi277.dayledger.ui.components.EmptyState("No blocks for this day.") }
        } else {
            items(state.blocks) { b ->
                AppCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(b.title, style = MaterialTheme.typography.titleMedium)
                            Text("${minutesToTime(b.startMin)}–${minutesToTime(b.endMin)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { viewModel.deleteBlock(b.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
fun AddBlockForm(onAdd: (Int, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("09:00") }
    var end by remember { mutableStateOf("10:00") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start HH:mm") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End HH:mm") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            val s = start.split(":").let { (it.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (it.getOrNull(1)?.toIntOrNull() ?: 0) }
            val e = end.split(":").let { (it.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (it.getOrNull(1)?.toIntOrNull() ?: 0) }
            onAdd(s, e, title)
        }) { Text("Add block") }
    }
}