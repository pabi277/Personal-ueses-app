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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.HabitsUiState
import io.github.pabi277.dayledger.HabitsViewModel
import io.github.pabi277.dayledger.data.checklist.ChecklistGeneration
import io.github.pabi277.dayledger.data.checklist.HabitRuleEntity
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

private val WEEKDAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

/**
 * Habits — create edit-lite habit rules and one-off tasks (Phase 8 part 8.4).
 *
 * Deliberately no starter routine: an empty list is the honest starting state, and the
 * weekday chips are the only thing that decides which dates generate.
 */
@Composable
fun HabitsScreen(state: HabitsUiState, viewModel: HabitsViewModel) {
    var title by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var cadence by remember { mutableStateOf("daily") }
    var minimum by remember { mutableStateOf("") }
    var taskTitle by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Habits") }
        item {
            AppCard(title = "New habit") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { cadence = "daily"; selectedDays = emptySet() }) {
                        Text(if (cadence == "daily") "● Daily" else "Daily")
                    }
                    OutlinedButton(onClick = { cadence = "weekdays" }) {
                        Text(if (cadence == "weekdays") "● Chosen days" else "Chosen days")
                    }
                    OutlinedButton(onClick = { cadence = "weekly"; selectedDays = selectedDays.take(1).toSet() }) {
                        Text(if (cadence == "weekly") "● Weekly" else "Weekly")
                    }
                }
                if (cadence != "daily") {
                    // Chunked into rows of four on purpose: seven chips in one Row overflow
                    // a 360dp screen, which clipped Saturday and left Sunday unreachable
                    // (owner device screenshot, 2026-10-23). UI Design v2 §2 requires
                    // content to reflow rather than clip.
                    (1..7).toList().chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { day ->
                                OutlinedButton(onClick = {
                                    selectedDays = if (cadence == "weekly") {
                                        setOf(day)
                                    } else if (day in selectedDays) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                }) {
                                    val label = WEEKDAY_LABELS[day - 1]
                                    Text(if (day in selectedDays) "● $label" else label)
                                }
                            }
                        }
                    }
                    Text(
                        text = "Monday to Sunday. Weekly keeps one day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = minimum,
                    onValueChange = { minimum = it },
                    label = { Text("Minimum version (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    viewModel.addHabit(
                        title = title,
                        cadence = cadence,
                        weekdayCsv = selectedDays.sorted().joinToString(","),
                        windowStartMin = null,
                        windowEndMin = null,
                        minimumLabel = minimum
                    )
                    title = ""
                    minimum = ""
                }) { Text("Add habit") }
            }
        }
        item { SectionTitle("Your habits") }
        if (state.habits.isEmpty()) {
            item { AppCard { EmptyState("No habits yet. A habit is a rule; it creates one occurrence on each scheduled day.") } }
        } else {
            items(state.habits) { habit -> HabitCard(habit, viewModel) }
        }
        item { SectionTitle("One-off tasks") }
        item {
            AppCard(title = "New task for today") {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    viewModel.addTask(taskTitle)
                    taskTitle = ""
                }) { Text("Add task") }
                if (state.tasks.isEmpty()) {
                    EmptyState("No one-off tasks. A task runs once, on its due date.")
                } else {
                    state.tasks.forEach { task ->
                        Text("${task.title} · ${task.dueDate}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        state.error?.let { message ->
            item { Text("Error: $message", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun HabitCard(habit: HabitRuleEntity, viewModel: HabitsViewModel) {
    AppCard {
        Text(habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        val schedule = when (habit.cadence) {
            "weekdays" -> "Chosen days: " + (
                ChecklistGeneration.weekdayNames(ChecklistGeneration.parseWeekdays(habit.weekdays))
                    .ifBlank { "none yet" }
                )
            "weekly" -> "Weekly on: " + (
                ChecklistGeneration.weekdayNames(ChecklistGeneration.parseWeekdays(habit.weekdays))
                    .ifBlank { "no day yet" }
                )
            else -> "Daily"
        }
        Text(schedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!habit.minimumLabel.isNullOrBlank()) {
            Text("Minimum: ${habit.minimumLabel}", style = MaterialTheme.typography.bodySmall)
        }
        if (habit.paused) Text("Paused", style = MaterialTheme.typography.bodySmall)
        if (habit.archived) Text("Archived", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.pause(habit.id, !habit.paused) }) {
                Text(if (habit.paused) "Resume" else "Pause")
            }
            OutlinedButton(onClick = { viewModel.archive(habit.id) }) { Text("Archive") }
        }
    }
}
