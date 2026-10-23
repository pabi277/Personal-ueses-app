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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.DayCloseUiState
import io.github.pabi277.dayledger.DayCloseViewModel
import io.github.pabi277.dayledger.data.checklist.Leftover
import io.github.pabi277.dayledger.data.checklist.LeftoverDecision
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle

/**
 * Day close — Phase 9 part 9.4.
 *
 * Every unfinished item is given one explicit outcome. Repeated carryover is shown plainly,
 * without shame and without a streak penalty: the flag exists so a genuinely stuck item is
 * visible, not so the owner feels bad about it.
 */
@Composable
fun DayCloseScreen(state: DayCloseUiState, viewModel: DayCloseViewModel, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Close ${state.dateKey}") }
        item {
            AppCard(title = "Next action for each unfinished item") {
                Text(
                    text = "Nothing rolls over by itself. Choose Move, Schedule, Shrink to " +
                        "Minimum or Drop for every item below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = state.targetDate,
                    onValueChange = { viewModel.setTargetDate(it) },
                    label = { Text("Target date for Move / Schedule (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                state.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (state.leftovers.isEmpty()) {
            item {
                AppCard {
                    EmptyState("Nothing left open. The day is settled.")
                }
            }
        } else {
            items(state.leftovers) { item -> LeftoverCard(item, viewModel) }
        }
        item { OutlinedButton(onClick = onBack) { Text("Back to Today") } }
    }
}

@Composable
private fun LeftoverCard(item: Leftover, viewModel: DayCloseViewModel) {
    AppCard {
        Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(
            text = "${item.dateKey} · ${item.kind}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.repeated) {
            // Visible, not shaming: "carried N times" is information the owner asked for.
            Text(
                text = "Carried ${item.carriedBefore} times already",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (item.carriedBefore == 1) {
            Text("Carried once already", style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.decide(item, LeftoverDecision.MOVE) }) { Text("Move") }
            OutlinedButton(onClick = { viewModel.decide(item, LeftoverDecision.SCHEDULE) }) { Text("Schedule") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.decide(item, LeftoverDecision.SHRINK_TO_MINIMUM) }) {
                Text("Shrink to Minimum")
            }
            OutlinedButton(onClick = { viewModel.decide(item, LeftoverDecision.DROP) }) { Text("Drop") }
        }
    }
}
