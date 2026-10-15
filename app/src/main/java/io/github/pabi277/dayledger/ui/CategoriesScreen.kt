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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.CategoryUiState
import io.github.pabi277.dayledger.CategoryViewModel

@Composable
fun CategoriesScreen(state: CategoryUiState, viewModel: CategoryViewModel) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Categories & budgets", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
        }
        items(state.categories) { c ->
            var budget by remember(c.id, c.budgetMin) { mutableStateOf(c.budgetMin.toString()) }
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(c.name, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Budget min") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        val b = budget.toIntOrNull() ?: c.budgetMin
                        val newKind = if (c.kind == "ceiling") "floor" else "ceiling"
                        viewModel.updateBudget(c.id, b, newKind)
                    }) { Text(c.kind) }
                    Button(onClick = {
                        val b = budget.toIntOrNull() ?: c.budgetMin
                        viewModel.updateBudget(c.id, b, c.kind)
                    }) { Text("Save") }
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("Assign apps", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(state.apps) { app ->
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(app.label, style = MaterialTheme.typography.bodyMedium)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                Text("Category: ${app.category}", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.categories.forEach { c ->
                        Button(onClick = { viewModel.assignApp(app.packageName, c.name) }) {
                            Text(c.name.take(6))
                        }
                    }
                }
            }
        }
    }
}
