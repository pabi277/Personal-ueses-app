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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.RemindersUiState
import io.github.pabi277.dayledger.RemindersViewModel
import io.github.pabi277.dayledger.reminders.ReminderProfile
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Phase 10.3 — controls, and an honest health panel.
 *
 * The panel exists because the requirements forbid pretending: when Android will not give
 * exact alarms, or notifications are switched off, the screen says so instead of showing a
 * tidy "next reminder at 19:00" that may never arrive.
 */
@Composable
fun RemindersScreen(state: RemindersUiState, viewModel: RemindersViewModel) {
    var quietFrom by remember(state.quietFrom) { mutableStateOf(state.quietFrom) }
    var quietTo by remember(state.quietTo) { mutableStateOf(state.quietTo) }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Reminders") }

        state.message?.let { message ->
            item {
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        state.error?.let { error ->
            item {
                Text(
                    "Something went wrong: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            AppCard(title = "Right now") {
                if (state.pausedUntil != null) {
                    Text(
                        "Paused until ${clock(state.pausedUntil)}. Nothing is completed or " +
                            "skipped while paused — items keep their due state.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "Reminders are on.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    state.nextFireAt?.let { "Next reminder: ${clock(it)}" }
                        ?: "Nothing scheduled in the next day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.pauseFor(60) }) { Text("Pause 1 h") }
                    OutlinedButton(onClick = { viewModel.pauseUntilTomorrow() }) { Text("Until tomorrow") }
                    if (state.pausedUntil != null) {
                        Button(onClick = { viewModel.resumeNow() }) { Text("Resume now") }
                    }
                }
            }
        }

        item {
            AppCard(title = "How Android is treating us") {
                Text(
                    if (state.notificationsAllowed) "Notifications: allowed."
                    else "Notifications: blocked. Reminders cannot appear until this is allowed " +
                        "in Android's app settings.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    if (state.exactAlarms) {
                        "Exact alarms: allowed. Reminders are scheduled to the minute."
                    } else {
                        "Exact alarms: not allowed. Reminders are still scheduled, but Android " +
                            "may deliver them late — sometimes by much more than a few minutes. " +
                            "That is Android's battery policy, not a setting in this app."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.exactAlarms) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error
                )
                if (state.degradedCount > 0) {
                    Text(
                        "${state.degradedCount} reminder(s) today are using approximate timing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SectionTitle("Quiet hours") }
        item {
            Text(
                "No new alert, sound or vibration in this window. Anything that comes due is " +
                    "held for the next allowed time — it is not replayed all at once.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quietFrom,
                        onValueChange = { quietFrom = it },
                        label = { Text("From") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quietTo,
                        onValueChange = { quietTo = it },
                        label = { Text("To") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(onClick = { viewModel.saveQuietHours(quietFrom, quietTo) }) {
                    Text("Save quiet hours")
                }
                Text(
                    "A window like 22:00 to 07:00 wraps midnight. Setting both times the same " +
                        "switches quiet hours off.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("Daily caps") }
        item {
            Text(
                "Two separate caps, counted only for checklist reminders. The usage reminders " +
                    "have their own cap and are not affected by these.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            AppCard(title = "Core") {
                CapRow(
                    value = state.coreCap,
                    onChange = { viewModel.setCoreCap(it) }
                )
            }
        }
        item {
            AppCard(title = "Everything else") {
                CapRow(
                    value = state.generalCap,
                    onChange = { viewModel.setGeneralCap(it) }
                )
            }
        }

        item { SectionTitle("Less often") }
        item {
            Text(
                "Doubles the gap and halves the sounds for that profile only. The other two " +
                    "keep their cadence.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(ReminderProfile.entries.toList()) { profile ->
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(profile.label, style = MaterialTheme.typography.titleMedium)
                        Text(
                            cadence(profile),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = profile.key in state.lessOften,
                        onCheckedChange = { viewModel.toggleLessOften(profile) }
                    )
                }
            }
        }

        item { SectionTitle("Today's items") }
        if (state.items.isEmpty()) {
            item { EmptyState("Nothing open today, so nothing will ring.") }
        } else {
            items(state.items) { item ->
                AppCard {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        buildString {
                            append(
                                item.nextFireAt?.let { "next ${clock(it)}" } ?: "not scheduled"
                            )
                            if (item.audibleFires > 0) append(" · ${item.audibleFires} sounded")
                            if (item.degraded.isNotEmpty()) append(" · approximate timing")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ReminderProfile.entries.forEach { profile ->
                            TextButton(
                                onClick = { viewModel.setProfile(item.occurrenceId, profile) }
                            ) {
                                Text(
                                    profile.label,
                                    color = if (profile.key == item.profile) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.load() }
}

@Composable
private fun CapRow(value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = { onChange((value - 1).coerceAtLeast(0)) }) { Text("–") }
        Text("$value a day", style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = { onChange((value + 1).coerceAtMost(24)) }) { Text("+") }
    }
}

private fun cadence(profile: ReminderProfile): String = when (profile) {
    ReminderProfile.CORE -> "Every 20 min, up to 4 sounds, then silent but visible"
    ReminderProfile.STANDARD -> "One reminder, one repeat about 50 min later"
    ReminderProfile.FLEXIBLE -> "One gentle reminder"
}

private fun clock(epochMillis: Long?): String {
    if (epochMillis == null) return "not scheduled"
    val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
}
