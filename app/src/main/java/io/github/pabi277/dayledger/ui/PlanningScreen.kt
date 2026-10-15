package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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

fun minutesToTime(m: Int): String = "%02d:%02d".format(m / 60, m % 60)

@Composable
fun PlanningScreen(state: PlanningUiState, viewModel: PlanningViewModel, dateKey: String) {
    LaunchedEffect(dateKey) { viewModel.load(dateKey) }
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Plan: ${state.dateKey}", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Templates", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.templates.forEach { t ->
                            Button(onClick = { viewModel.applyTemplate(t.id) }) { Text(t.name) }
                        }
                    }
                    Button(onClick = { viewModel.shiftDay(15) }) { Text("Shift rest of day +15m") }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add block", style = MaterialTheme.typography.titleMedium)
                    AddBlockForm(onAdd = { s, e, title -> viewModel.addBlock(s, e, title) })
                }
            }
        }
        item { Text("Blocks", style = MaterialTheme.typography.titleMedium) }
        items(state.blocks) { b ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${minutesToTime(b.startMin)}–${minutesToTime(b.endMin)} ${b.title}")
                    Button(onClick = { viewModel.deleteBlock(b.id) }) { Text("Delete") }
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start HH:mm") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End HH:mm") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            val s = start.split(":"); val e = end.split(":")
            if (s.size == 2 && e.size == 2) {
                val sm = s[0].toIntOrNull()?.times(60)?.plus(s[1].toIntOrNull() ?: 0)
                val em = e[0].toIntOrNull()?.times(60)?.plus(e[1].toIntOrNull() ?: 0)
                if (sm != null && em != null && em > sm) onAdd(sm, em, title.ifBlank { "Block" })
            }
        }) { Text("Add block") }
    }
}
