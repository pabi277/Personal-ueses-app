package io.github.pabi277.dayledger.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.reminders.PlanStatus
import io.github.pabi277.dayledger.reminders.ReminderPlan
import io.github.pabi277.dayledger.reminders.ReminderRepository
import java.time.ZoneId

/**
 * Phase 10.4 — the part that has to survive Android.
 *
 * The engine decides *when*; this class is the only thing that talks to `AlarmManager`.
 * Three properties matter:
 *
 * - **Rebuildable.** [syncAll] recomputes every alarm from persisted state, so a reboot, a
 *   time-zone change, an app update or a permission re-grant rebuilds rather than trying to
 *   remember what was scheduled before it was lost.
 * - **One alarm per occurrence.** The request code is derived from the occurrence id and the
 *   pending intent is replaced in place, so repeated syncs cannot multiply alarms.
 * - **Honest about timing.** When Android refuses an exact alarm the reminder is still
 *   scheduled with `setAndAllowWhileIdle`, which the system may delay by minutes or much
 *   longer. That is recorded in the state row and shown in the UI rather than being
 *   presented as punctual.
 */
class ReminderScheduler(
    private val context: Context,
    private val zone: ZoneId = ZoneId.systemDefault()
) {

    companion object {
        const val EXTRA_OCCURRENCE_ID = "occurrenceId"

        /** Recorded in `reminder_states.degraded` when exact alarms are unavailable. */
        const val DEGRADED_EXACT_ALARMS = "exact alarms not permitted; Android may delay this reminder"
    }

    private val db get() = DayLedgerDatabase.getInstance(context)

    private fun alarmManager(): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { alarmManager().canScheduleExactAlarms() }.getOrDefault(false)
        } else {
            true
        }

    /** Rebuild every alarm for today's open occurrences from persisted state. */
    suspend fun syncAll() {
        val repo = ReminderRepository(db, zone)
        repo.openToday().forEach { occurrence ->
            applyPlan(occurrence.id, repo.planFor(occurrence), repo)
        }
    }

    /** Recompute one occurrence after an action, a snooze or a fire. */
    suspend fun syncOne(occurrenceId: String) {
        val repo = ReminderRepository(db, zone)
        val occurrence = repo.occurrence(occurrenceId)
        if (occurrence == null) {
            cancel(occurrenceId)
            return
        }
        applyPlan(occurrenceId, repo.planFor(occurrence), repo)
    }

    private suspend fun applyPlan(
        occurrenceId: String,
        plan: ReminderPlan,
        repo: ReminderRepository
    ) {
        repo.recordPlan(occurrenceId, plan)
        val at = plan.atMillis
        if (plan.status == PlanStatus.STOPPED || at == null) {
            cancel(occurrenceId)
            return
        }
        val exact = setAlarm(occurrenceId, at)
        repo.recordDegraded(occurrenceId, if (exact) "" else DEGRADED_EXACT_ALARMS)
    }

    /**
     * Schedule [occurrenceId] for [atMillis].
     *
     * @return true when this was installed as an exact alarm, false when it was degraded to
     *   an inexact idle-safe alarm that Android may delay.
     */
    fun setAlarm(occurrenceId: String, atMillis: Long): Boolean {
        val pi = pendingIntent(occurrenceId)
        val am = alarmManager()
        val exact = canScheduleExactAlarms()
        return try {
            if (exact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            }
            exact
        } catch (e: SecurityException) {
            // Permission revoked between the check and the call: degrade rather than crash.
            runCatching { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi) }
            false
        }
    }

    fun cancel(occurrenceId: String) {
        runCatching { alarmManager().cancel(pendingIntent(occurrenceId)) }
    }

    private fun pendingIntent(occurrenceId: String): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_OCCURRENCE_ID, occurrenceId)
            // Distinct data keeps two occurrences with colliding hashCodes apart.
            data = android.net.Uri.parse("dayledger://reminder/$occurrenceId")
        }
        return PendingIntent.getBroadcast(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
