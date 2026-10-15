package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.WeeklyReportUiState
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.ScreenTitle

@Composable
fun WeeklyReportScreen(state: WeeklyReportUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle("Weekly report")
        AppCard(title = "Summary") {
            Text(state.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}