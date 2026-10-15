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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.CategoryUiState
import io.github.pabi277.dayledger.CategoryViewModel
import io.github.pabi277.dayledger.data.AppInfoEntity

@Composable
fun CategoriesScreen(state: CategoryUiState, viewModel: CategoryViewModel) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Categories & budgets", style = MaterialTheme.typography.headlineMedium) }
        items(state.categories) { c ->
            var budget by remember(c.id, c.budgetMin) { mutableStateOf(c.budgetMin.toString()) }
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(c.name, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget (min)") }, modifier = Modifier.weight(1f))
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
        }
        item { Text("Assign apps", style = MaterialTheme.typography.titleMedium) }
        items(state.apps) { app ->
            AppCategoryCard(app, state, viewModel)
        }
    }
}

@Composable
fun AppCategoryCard(app: AppInfoEntity, state: CategoryUiState, viewModel: CategoryViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(app.label, style = MaterialTheme.typography.bodyLarge)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            Text("Category: ${app.category}", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                state.categories.forEach { c ->
                    Button(onClick = { viewModel.assignApp(app.packageName, c.name) }) { Text(c.name.take(8)) }
                }
            }
        }
    }
}
