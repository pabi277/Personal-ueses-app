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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

fun formatMinutes(m: Int): String {
    val h = m / 60
    val min = m % 60
    return if (h > 0) "${h}h ${min}m" else "${min}m"
}

@Composable
fun TodayScreen(state: TodayUiState, onRefresh: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)
                    Text(formatMinutes(state.totalMinutes), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("device-reported foreground time · as of ${state.asOf}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefresh, enabled = !state.isLoading) { Text("Refresh") }
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            state.error?.let { Text("Error: $it", color = Color.Red) }
        }
        item {
            Text("Hourly (device-reported)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            HourlyStrip(state.hourly)
        }
        item {
            Text("Categories", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.categories.forEach { c ->
                        val mins = state.apps.filter { state.appInfo[it.packageName]?.category == c.name }
                            .sumOf { it.foregroundMinutes }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(Color(c.color), CircleShape))
                                Text(c.name)
                            }
                            Text("${formatMinutes(mins)} / ${c.budgetMin}m")
                        }
                    }
                }
            }
        }
        item {
            Text("Apps", style = MaterialTheme.typography.titleMedium)
        }
        items(state.apps) { u ->
            AppRow(u, state.appInfo[u.packageName]?.category ?: "Unclassified")
        }
    }
}

@Composable
fun HourlyStrip(hours: List<Int>) {
    val max = (hours.maxOrNull() ?: 1).coerceAtLeast(1)
    Card {
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            hours.forEach { m ->
                val intensity = m.toFloat() / max.toFloat()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f + 0.85f * intensity))
                )
            }
        }
    }
}

@Composable
fun AppRow(u: AppUsageEntity, category: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(u.packageName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatMinutes(u.foregroundMinutes), fontWeight = FontWeight.SemiBold)
        }
    }
}
