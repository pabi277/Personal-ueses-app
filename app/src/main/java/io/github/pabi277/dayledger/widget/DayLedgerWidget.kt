package io.github.pabi277.dayledger.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.text.Text
import io.github.pabi277.dayledger.data.UsageRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DayLedgerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = UsageRepository(context)
        val today = LocalDate.now().toString()
        // Same rules as Today: screen-on foreground time, launcher/system UI/dialer excluded.
        val apps = repo.screenUsageForDate(today)
        val total = apps.sumOf { it.foregroundMinutes }
        val info = repo.appInfoAll().associateBy { it.packageName }
        val topWaste = apps.filter { info[it.packageName]?.category == "Social/Waste" }
            .maxByOrNull { it.foregroundMinutes }
        val topWasteLabel = topWaste?.let { info[it.packageName]?.label ?: it.packageName } ?: "—"
        val asOf = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        provideContent {
            GlanceTheme {
                Column {
                    Text("Day Ledger")
                    Text("Total: ${total / 60}h ${total % 60}m")
                    Text("Top waste: $topWasteLabel")
                    Text("as of $asOf")
                }
            }
        }
    }
}
