package io.github.pabi277.dayledger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.ChecklistUiState
import io.github.pabi277.dayledger.ChecklistViewModel
import io.github.pabi277.dayledger.TodayUiState
import io.github.pabi277.dayledger.data.AppUsageEntity
import io.github.pabi277.dayledger.data.checklist.ChecklistValues
import io.github.pabi277.dayledger.data.checklist.TodayItem
import io.github.pabi277.dayledger.data.checklist.TodayRow
import io.github.pabi277.dayledger.data.checklist.TodaySection
import io.github.pabi277.dayledger.ui.components.AppCard
import io.github.pabi277.dayledger.ui.components.EmptyState
import io.github.pabi277.dayledger.ui.components.ScreenTitle
import io.github.pabi277.dayledger.ui.components.SectionTitle

fun formatMinutes(m: Int): String {
    val h = m / 60
    val min = m % 60
    return if (h > 0) "${h}h ${min}m" else "${min}m"
}

private fun windowLabel(row: TodayRow): String? {
    val start = row.windowStartMin ?: return null
    val end = row.windowEndMin ?: start
    fun fmt(min: Int) = "%02d:%02d".format(min / 60, min % 60)
    return if (start == end) fmt(start) else "${fmt(start)}–${fmt(end)}"
}

private fun sectionTitle(section: TodaySection): String = when (section) {
    TodaySection.OVERDUE -> "Overdue"
    TodaySection.NOW -> "Now"
    TodaySection.NEXT -> "Next"
    TodaySection.DUE_TODAY -> "Due today"
    TodaySection.ANYTIME -> "Anytime"
    TodaySection.COMPLETED -> "Done"
    TodaySection.SKIPPED -> "Skipped"
}

/**
 * Today — the Phase 8 Command Center Checklist (`docs/UI_DESIGN.md` §3).
 *
 * The checklist is the hero; device-reported usage is a lower, collapsible "Phone
 * evidence" card that never marks anything done (DL22).
 */
@Composable
fun TodayScreen(
    checklist: ChecklistUiState,
    usage: TodayUiState,
    checklistVm: ChecklistViewModel,
    onRefreshUsage: () -> Unit
) {
    var capture by remember { mutableStateOf("") }
    var showEvidenceShown by remember { mutableStateOf(false) }

    checklist.evidenceFor?.let { id ->
        EvidenceDialog(
            title = checklist.items.firstOrNull { it.row.id == id }?.row?.title ?: "Item",
            onDismiss = { checklistVm.dismissEvidence() },
            onSave = { minutes, topic, note -> checklistVm.saveEvidence(id, minutes, topic, note) }
        )
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // UI Design v2: the visible label ships with the Phase 8 front door. The
            // package and database identity are unchanged.
            Text(
                text = "Daily Logger",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            ScreenTitle("Today")
        }
        item {
            AppCard {
                val summary = buildString {
                    append(if (checklist.remaining == 0) "Nothing left today" else "${checklist.remaining} left")
                    if (checklist.fullCount > 0) append(" · ${checklist.fullCount} full")
                    if (checklist.minimumCount > 0) append(" · ${checklist.minimumCount} minimum")
                    if (checklist.skippedCount > 0) append(" · ${checklist.skippedCount} skipped")
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                checklist.error?.let {
                    Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = capture,
                    onValueChange = { capture = it },
                    label = { Text("Quick capture") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    checklistVm.quickCapture(capture)
                    capture = ""
                }) { Text("Add") }
            }
        }

        val sections = TodaySection.entries.filter { section ->
            checklist.items.any { it.section == section }
        }
        if (sections.isEmpty()) {
            item { AppCard { EmptyState("Nothing scheduled today. Add a habit in Habits, or capture a task above.") } }
        }
        sections.forEach { section ->
            item { SectionTitle(sectionTitle(section)) }
            items(checklist.items.filter { it.section == section }) { item ->
                ChecklistRow(
                    item = item,
                    canUndo = checklist.lastActionId == item.row.id,
                    onDone = { checklistVm.done(item.row.id) },
                    onMinimum = { checklistVm.minimum(item.row.id) },
                    onLater = { checklistVm.later(item.row.id) },
                    onSkip = { checklistVm.skip(item.row.id) },
                    onUndo = { checklistVm.undo(item.row.id) }
                )
            }
        }

        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Phone evidence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = {
                        showEvidenceShown = !showEvidenceShown
                        if (showEvidenceShown) onRefreshUsage()
                    }) { Text(if (showEvidenceShown) "Hide" else "Show") }
                }
                Text(
                    text = "Device-reported usage. It is evidence, never a completion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showEvidenceShown) {
                    Text(
                        text = formatMinutes(usage.totalMinutes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Foreground time · as of ${usage.asOf}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    usage.apps.take(5).forEach { u ->
                        AppRow(
                            u = u,
                            label = usage.appInfo[u.packageName]?.label ?: u.packageName,
                            category = usage.appInfo[u.packageName]?.category ?: "Unclassified"
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = onRefreshUsage, enabled = !usage.isLoading) { Text("Refresh") }
                        if (usage.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistRow(
    item: TodayItem,
    canUndo: Boolean,
    onDone: () -> Unit,
    onMinimum: () -> Unit,
    onLater: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit
) {
    val row = item.row
    val settled = ChecklistValues.isClosed(row.status)
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                val meta = buildList {
                    if (row.domain.isNotBlank()) add(row.domain)
                    windowLabel(row)?.let { add(it) }
                    when (row.status) {
                        ChecklistValues.STATUS_FULL -> add("Full")
                        ChecklistValues.STATUS_MINIMUM -> add("Minimum")
                        ChecklistValues.STATUS_SKIPPED -> add("Skipped, not done")
                        ChecklistValues.STATUS_LATER -> add("Later")
                    }
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (!settled) {
            // Done is first and needs no form. Everything else is secondary.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDone) { Text("Done") }
                OutlinedButton(onClick = onMinimum) { Text("Minimum") }
                OutlinedButton(onClick = onLater) { Text("Later") }
                OutlinedButton(onClick = onSkip) { Text("Skip") }
            }
        }
        if (canUndo || settled) {
            TextButton(onClick = onUndo) { Text("Undo") }
        }
    }
}

@Composable
fun EvidenceDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (Int?, String, String) -> Unit
) {
    var minutes by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Optional detail. Dismissing keeps the completion.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Minutes") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(minutes.toIntOrNull(), topic, note) }) { Text("Save") }
        },
        dismissButton = {
            // An obvious "no detail" path: the completion already happened.
            TextButton(onClick = onDismiss) { Text("No detail") }
        }
    )
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
fun AppRow(u: AppUsageEntity, label: String, category: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(formatMinutes(u.foregroundMinutes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
