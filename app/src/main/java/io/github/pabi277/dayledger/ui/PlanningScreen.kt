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
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Plan: ${state.dateKey}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Templates", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row {
                state.templates.forEach { t ->
                    Button(onClick = { viewModel.applyTemplate(t.id) }) { Text(t.name) }
                }
            }
            Spacer(Modifier.height(12.dp))
            AddBlockForm(onAdd = { s, e, title -> viewModel.addBlock(s, e, title) })
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.shiftDay(15) }) { Text("Shift rest of day +15m") }
            Spacer(Modifier.height(12.dp))
            Text("Blocks", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
        }
        items(state.blocks) { b ->
            BlockRow(b, onDelete = { viewModel.deleteBlock(b.id) })
        }
    }
}

@Composable
fun AddBlockForm(onAdd: (Int, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("09:00") }
    var end by remember { mutableStateOf("10:00") }
    Column {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Row {
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

@Composable
fun BlockRow(b: BlockEntity, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${minutesToTime(b.startMin)}–${minutesToTime(b.endMin)} ${b.title}")
        Button(onClick = onDelete) { Text("Delete") }
    }
}
