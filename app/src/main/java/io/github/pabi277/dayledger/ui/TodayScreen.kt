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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Today", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Total: ${formatMinutes(state.totalMinutes)}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text("device-reported foreground time, as of ${state.asOf}")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRefresh, enabled = !state.isLoading) { Text("Refresh") }
            if (state.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text("Error: $it", color = Color.Red)
            }
            Spacer(Modifier.height(16.dp))
            Text("Hourly (device-reported)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            HourlyStrip(state.hourly)
            Spacer(Modifier.height(16.dp))
            Text("Apps", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(state.apps) { u ->
            AppRow(u, state.appInfo[u.packageName]?.category ?: "Unclassified")
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("Categories", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.categories.forEach { c ->
                val mins = state.apps.filter { state.appInfo[it.packageName]?.category == c.name }
                    .sumOf { it.foregroundMinutes }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(c.name)
                    Text("${formatMinutes(mins)} / ${c.budgetMin}m (${c.kind})")
                }
            }
        }
    }
}

@Composable
fun HourlyStrip(hours: List<Int>) {
    val max = (hours.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        hours.forEach { m ->
            val intensity = m.toFloat() / max.toFloat()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(Color(0xFF2196F3).copy(alpha = 0.15f + 0.85f * intensity))
            )
        }
    }
}

@Composable
fun AppRow(u: AppUsageEntity, category: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(u.packageName, style = MaterialTheme.typography.bodyMedium)
            Text(category, style = MaterialTheme.typography.bodySmall)
        }
        Text(formatMinutes(u.foregroundMinutes))
    }
}
