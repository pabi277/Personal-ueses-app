package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.TodayUiState
import io.github.pabi277.dayledger.data.AppUsageEntity
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.KeyValueRow
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

fun formatMinutes(m: Int): String {
    val h = m / 60
    val min = m % 60
    return if (h > 0) "${h}h ${min}m" else "${min}m"
}

@Composable
fun TodayScreen(state: TodayUiState, onRefresh: () -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Today") }
        item {
            AppCard(title = "Device usage") {
                Text(
                    text = formatMinutes(state.totalMinutes),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Foreground time · as of ${state.asOf}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onRefresh, enabled = !state.isLoading) { Text("Refresh") }
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
        item {
            AppCard(title = "Hourly pattern") {
                HourlyStrip(state.hourly)
                Text(
                    text = "Each bar is one hour of the day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { SectionTitle("Categories") }
        item {
            AppCard {
                if (state.categories.isEmpty()) {
                    EmptyState("No categories yet. Add them in More → Categories.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.categories.forEach { c ->
                            val mins = state.apps.filter { state.appInfo[it.packageName]?.category == c.name }
                                .sumOf { it.foregroundMinutes }
                            KeyValueRow(c.name, "${formatMinutes(mins)} / ${c.budgetMin}m")
                        }
                    }
                }
            }
        }
        item { SectionTitle("Apps") }
        if (state.apps.isEmpty()) {
            item { EmptyState("No usage data for today yet.") }
        } else {
            items(state.apps) { u ->
                AppRow(u, state.appInfo[u.packageName]?.category ?: "Unclassified")
            }
        }
    }
}

@Composable
fun HourlyStrip(hours: List<Int>) {
    val max = (hours.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().height(40.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        hours.forEach { m ->
            val intensity = m.toFloat() / max.toFloat()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f + 0.85f * intensity), MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
fun AppRow(u: AppUsageEntity, category: String) {
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(u.packageName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatMinutes(u.foregroundMinutes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}