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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.LimitsUiState
import io.github.pabi277.dayledger.LimitsViewModel
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.limits.LimitRules
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle

@Composable
fun LimitsScreen(state: LimitsUiState, viewModel: LimitsViewModel) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Limits & always-allowed") }
        item {
            AppCard(title = "Focus") {
                Text(
                    "Start a focus session to temporarily block distracting apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { }) { Text("Start focus session (50 min)") }
            }
        }
        state.message?.let { text ->
            item {
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        item { io.github.pabi277.dayledger.ui.components.SectionTitle("Per-app limits") }
        item {
            Text(
                text = "A per-app limit overrides the category budget for that app. Clear it " +
                    "to fall back to the category. Always-allowed apps are never blocked, and " +
                    "an active earning session is never interrupted.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { io.github.pabi277.dayledger.ui.components.SectionTitle("Apps") }
        if (state.apps.isEmpty()) {
            item { EmptyState("No apps found yet.") }
        } else {
            items(state.apps) { app ->
                val budget = state.categories.firstOrNull { it.name == app.category }?.budgetMin
                AppCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.titleMedium)
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = app.alwaysAllowed, onCheckedChange = { viewModel.toggleAlwaysAllowed(app.packageName, it) })
                    }
                    AppLimitRow(app = app, categoryBudgetMin = budget, minutesToday = state.minutesToday[app.packageName] ?: 0, viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * F2: the per-app limit field that never existed.
 *
 * The line under it states which limit the blocker will actually apply, using the same
 * [LimitRules.effectiveLimit] the service calls — so the screen cannot promise one number
 * while enforcement uses another.
 */
@Composable
private fun AppLimitRow(
    app: AppInfoEntity,
    categoryBudgetMin: Int?,
    minutesToday: Int,
    viewModel: LimitsViewModel
) {
    var text by remember(app.packageName, app.limitMin) {
        mutableStateOf(app.limitMin?.toString() ?: "")
    }
    val effective = LimitRules.effectiveLimit(app.limitMin, categoryBudgetMin)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { entered -> text = entered.filter { it.isDigit() }.take(4) },
                label = { Text("Per-app limit (min)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { viewModel.setPerAppLimit(app.packageName, text.toIntOrNull()) }) {
                Text("Save")
            }
            OutlinedButton(onClick = {
                text = ""
                viewModel.setPerAppLimit(app.packageName, null)
            }) { Text("Clear") }
        }
        val status = when {
            app.alwaysAllowed -> "Always allowed — never blocked"
            effective == null -> "No limit (no per-app limit, no category budget)"
            else -> "Enforced: ${LimitRules.describe(effective.first, effective.second)}" +
                if (app.category.isNotBlank()) " · category ${app.category}" else ""
        }
        Text(
            text = "$status · ${minutesToday}m used today",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal
        )
    }
}
