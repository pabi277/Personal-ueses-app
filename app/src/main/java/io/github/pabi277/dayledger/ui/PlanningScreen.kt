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
                    Text(
                        "No templates yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // One row per template, full width. The previous single Row of chips
                    // overflowed on a 360dp screen and left the card looking empty
                    // (owner screenshot, 2026-10-23).
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        state.templates.forEach { t ->
                            val count = state.templateItemCounts[t.id] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(t.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        if (count == 0) "Empty template" else "$count block${if (count == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.applyTemplate(t.id) },
                                        enabled = count > 0
                                    ) { Text("Apply") }
                                    OutlinedButton(onClick = { viewModel.deleteTemplate(t.id) }) { Text("Delete") }
                                }
                            }
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
        item { SectionTitle("Checklist for this date") }
        if (state.checklistItems.isEmpty()) {
            item {
                AppCard {
                    io.github.pabi277.dayledger.ui.components.EmptyState(
                        "Nothing scheduled. Habits, dated tasks and carried items appear here."
                    )
                }
            }
        } else {
            items(state.checklistItems) { item ->
                val isTop3 = state.top3.contains(item.id)
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = buildString {
                                    append(item.kind)
                                    item.windowStartMin?.let { append(" · ${minutesToTime(it)}") }
                                    append(" · ${item.status}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Top 3 is a planning aid, so it lives with the date, not with Today.
                        OutlinedButton(onClick = {
                            if (isTop3) viewModel.removeFromTop3(item.id) else viewModel.addToTop3(item.id)
                        }) { Text(if (isTop3) "Top 3 ✓" else "Top 3") }
                    }
                }
            }
        }
        if (state.top3.isNotEmpty()) {
            item {
                AppCard(title = "Top 3") {
                    Text(
                        text = "At most three, by your choice. Not a limit on the day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.top3.forEach { id ->
                        val title = state.checklistItems.firstOrNull { it.id == id }?.title ?: id
                        Text("• $title", style = MaterialTheme.typography.bodyMedium)
                    }
                }
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