package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.LearningUiState
import io.github.pabi277.dayledger.LearningViewModel

@Composable
fun LearningScreen(state: LearningUiState, viewModel: LearningViewModel) {
    var minutes by remember { mutableStateOf("30") }
    var text by remember { mutableStateOf("") }
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Learning", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Streak: ${state.streak} days")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Minutes") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("What you learned") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.logLearning(minutes.toIntOrNull() ?: 0, text) }) { Text("Log learning") }
            Spacer(Modifier.height(12.dp))
            Text("History", style = MaterialTheme.typography.titleMedium)
        }
        items(state.entries) { e ->
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${e.minutes}m — ${e.text}")
            }
        }
    }
}
