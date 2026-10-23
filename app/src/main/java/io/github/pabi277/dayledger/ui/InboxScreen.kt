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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.InboxUiState
import io.github.pabi277.dayledger.InboxViewModel
import io.github.pabi277.dayledger.data.checklist.InboxItemEntity
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

/**
 * Inbox — Phase 9 part 9.1. Title-first capture, organisation optional.
 *
 * The capture field is the first thing on the screen and the only required input; the
 * date buttons live on each row, after the fact, so nothing forces classification.
 */
@Composable
fun InboxScreen(state: InboxUiState, viewModel: InboxViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Inbox") }
        item {
            AppCard(title = "Capture") {
                Text(
                    "A title is enough. Date and domain are optional, and can wait.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        viewModel.capture(title, note = note)
                        title = ""
                        note = ""
                    }) { Text("Capture") }
                    if (state.justCapturedId != null) {
                        TextButton(onClick = { viewModel.undoLastCapture() }) { Text("Undo") }
                    }
                }
                state.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { SectionTitle("Inbox (${state.items.size})") }
        if (state.items.isEmpty()) {
            item {
                AppCard {
                    EmptyState("Nothing waiting. Captures land here until you give them a date.")
                }
            }
        } else {
            items(state.items) { item -> InboxRow(item, viewModel) }
        }
        item { OutlinedButton(onClick = onBack) { Text("Back to Today") } }
    }
}

@Composable
private fun InboxRow(item: InboxItemEntity, viewModel: InboxViewModel) {
    var showDate by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf("") }
    AppCard {
        Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        if (item.note.isNotBlank()) {
            Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.scheduleToday(item.id) }) { Text("Today") }
            OutlinedButton(onClick = { viewModel.scheduleTomorrow(item.id) }) { Text("Tomorrow") }
            OutlinedButton(onClick = { showDate = !showDate }) { Text("Pick date") }
            TextButton(onClick = { viewModel.discard(item.id) }) { Text("Discard") }
        }
        if (showDate) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date yyyy-MM-dd") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    if (viewModel.scheduleOn(item.id, dateText)) showDate = false
                }) { Text("Schedule") }
            }
        }
    }
}
