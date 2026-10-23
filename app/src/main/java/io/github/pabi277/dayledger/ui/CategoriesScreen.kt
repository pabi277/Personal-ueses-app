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
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.CategoryUiState
import io.github.pabi277.dayledger.CategoryViewModel
import io.github.pabi277.dayledger.data.AppCategorySuggestion
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

@Composable
fun CategoriesScreen(state: CategoryUiState, viewModel: CategoryViewModel) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Categories & budgets") }
        if (state.categories.isEmpty()) {
            item { EmptyState("No categories yet.") }
        } else {
            items(state.categories) { c ->
                var budget by remember(c.id, c.budgetMin) { mutableStateOf(c.budgetMin.toString()) }
                AppCard(title = c.name) {
                    Text(
                        text = "Budget and ceiling/floor rule for this category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = budget,
                            onValueChange = { budget = it },
                            label = { Text("Budget (min)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = {
                            val b = budget.toIntOrNull() ?: c.budgetMin
                            val newKind = if (c.kind == "ceiling") "floor" else "ceiling"
                            viewModel.updateBudget(c.id, b, newKind)
                        }) { Text(c.kind.replaceFirstChar { it.uppercase() }) }
                        Button(onClick = {
                            val b = budget.toIntOrNull() ?: c.budgetMin
                            viewModel.updateBudget(c.id, b, c.kind)
                        }) { Text("Save") }
                    }
                }
            }
        }
        if (state.suggestions.isNotEmpty()) {
            item { SectionTitle("Suggested categories") }
            item {
                Text(
                    text = "One tap to confirm. Nothing is filled in silently, and an app you " +
                        "have already categorised is never suggested again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(state.suggestions) { suggestion ->
                SuggestionCard(suggestion, viewModel)
            }
        }
        item { SectionTitle("Assign apps") }
        if (state.apps.isEmpty()) {
            item { EmptyState("No apps found yet.") }
        } else {
            items(state.apps) { app ->
                AppCategoryCard(app, state, viewModel)
            }
        }
    }
}

@Composable
fun SuggestionCard(suggestion: AppCategorySuggestion, viewModel: CategoryViewModel) {
    AppCard {
        Text(suggestion.label, style = MaterialTheme.typography.titleMedium)
        Text(
            suggestion.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Suggested: ${suggestion.category} — ${suggestion.reason}",
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.acceptSuggestion(suggestion) }) {
                Text("Use ${suggestion.category}")
            }
            OutlinedButton(onClick = { viewModel.dismissSuggestion(suggestion) }) {
                Text("Not now")
            }
        }
    }
}

@Composable
fun AppCategoryCard(app: AppInfoEntity, state: CategoryUiState, viewModel: CategoryViewModel) {
    AppCard {
        Text(app.label, style = MaterialTheme.typography.titleMedium)
        Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Category: ${app.category}", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.categories.forEach { c ->
                OutlinedButton(onClick = { viewModel.assignApp(app.packageName, c.name) }) {
                    Text(c.name.take(10))
                }
            }
        }
    }
}