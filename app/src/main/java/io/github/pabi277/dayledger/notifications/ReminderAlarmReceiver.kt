package io.github.pabi277.dayledger.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistValues
import io.github.pabi277.dayledger.reminders.PlanStatus
import io.github.pabi277.dayledger.reminders.ReminderEventKind
import io.github.pabi277.dayledger.reminders.ReminderProfile
import io.github.pabi277.dayledger.reminders.ReminderProfiles
import io.github.pabi277.dayledger.reminders.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * One alarm fired. Re-derive everything from the database before doing anything: the alarm
 * may be minutes late, the item may have been completed from the app in the meantime, or
 * the owner may have paused reminders.
 *
 * The order below matters. Plan → post → record → re-plan. A fire is recorded only after
 * the notification was actually posted, so the log never claims a reminder the owner did
 * not receive, and the next alarm is computed from counters that already include this fire.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(ReminderScheduler.EXTRA_OCCURRENCE_ID) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DayLedgerDatabase.getInstance(context)
                val repo = ReminderRepository(db)
                val scheduler = ReminderScheduler(context)
                val occurrence = repo.occurrence(occurrenceId)
                if (occurrence == null) {
                    scheduler.cancel(occurrenceId)
                    return@launch
                }
                if (ChecklistValues.isClosed(occurrence.status)) {
                    repo.record(
                        occurrenceId = occurrenceId,
                        dateKey = occurrence.dateKey,
                        kind = ReminderEventKind.STALE,
                        detail = "fire skipped: already ${occurrence.status}"
                    )
                    scheduler.cancel(occurrenceId)
                    ReminderNotifier(context).cancel(occurrenceId)
                    return@launch
                }

                val now = System.currentTimeMillis()
                val plan = repo.planFor(occurrence, now)
                val at = plan.atMillis
                val dueNow = plan.status == PlanStatus.SCHEDULED &&
                    at != null && at <= now + ReminderRepository.DUE_TOLERANCE_MILLIS

                if (!dueNow) {
                    // Paused, in quiet hours, capped or rescheduled: follow the new plan.
                    if (at != null) {
                        repo.recordPlan(occurrenceId, plan)
                        scheduler.setAlarm(occurrenceId, at)
                    } else {
                        repo.record(
                            occurrenceId = occurrenceId,
                            dateKey = occurrence.dateKey,
                            kind = ReminderEventKind.STOPPED,
                            detail = plan.reason.name
                        )
                        scheduler.cancel(occurrenceId)
                    }
                    return@launch
                }

                val state = repo.stateFor(occurrenceId)
                val profile = ReminderProfile.fromKey(state?.profile ?: ReminderProfile.STANDARD.key)
                val defaults = ReminderProfiles.effective(
                    profile,
                    profile in repo.policy().lessOften
                )
                val fireNumber = (state?.audibleFires ?: 0) + 1
                val lastAudible = fireNumber >= defaults.maxAudible

                val posted = ReminderNotifier(context).post(
                    occurrence = occurrence,
                    profile = profile,
                    audible = plan.audible,
                    ongoing = lastAudible,
                    fireNumber = fireNumber,
                    maxAudible = defaults.maxAudible
                )
                if (!posted) {
                    repo.record(
                        occurrenceId = occurrenceId,
                        dateKey = occurrence.dateKey,
                        kind = ReminderEventKind.DEGRADED,
                        detail = "notification not delivered: notifications disabled"
                    )
                    // Retry later instead of silently dropping the item.
                    scheduler.setAlarm(occurrenceId, now + 10 * 60_000L)
                    return@launch
                }

                repo.recordFire(occurrence, profile, plan.audible, now)
                scheduler.syncOne(occurrenceId)
            } finally {
                pending.finish()
            }
        }
    }
}
