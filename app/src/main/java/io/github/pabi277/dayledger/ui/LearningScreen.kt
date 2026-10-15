package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import io.github.pabi277.dayledger.LearningUiState
import io.github.pabi277.dayledger.LearningViewModel
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

@Composable
fun LearningScreen(state: LearningUiState, viewModel: LearningViewModel) {
    var minutes by remember { mutableStateOf("30") }
    var text by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Learning") }
        item {
            AppCard(title = "Streak") {
                Text(
                    "${state.streak} days",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Keep the streak alive by logging a short session.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            AppCard(title = "Log learning") {
                OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Minutes") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("What you learned") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.logLearning(minutes.toIntOrNull() ?: 0, text) }) { Text("Log learning") }
            }
        }
        item { SectionTitle("History") }
        if (state.entries.isEmpty()) {
            item { EmptyState("No learning entries yet.") }
        } else {
            items(state.entries) { e ->
                AppCard {
                    Text("${e.minutes}m — ${e.text}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}