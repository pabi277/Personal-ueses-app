package io.github.pabi277.dayledger.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistRepository
import io.github.pabi277.dayledger.data.checklist.ChecklistValues
import io.github.pabi277.dayledger.reminders.ReminderActions
import io.github.pabi277.dayledger.reminders.ReminderEventKind
import io.github.pabi277.dayledger.reminders.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Done / Minimum / Later / Skip from a notification.
 *
 * **Idempotent by construction.** The status is read first; a tap on an occurrence that is
 * already settled writes a `stale` event and nothing else. Two rapid taps, or a replay of
 * the same intent after a reschedule, therefore produce at most one completion log — which
 * is DL13's acceptance test. `ChecklistRepository.act` makes the same guarantee one layer
 * down, so this is defence in depth rather than the only guard.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(ReminderScheduler.EXTRA_OCCURRENCE_ID) ?: return
        val action = intent.action ?: return
        val checklistAction = ReminderActions.fromIntentAction(action) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DayLedgerDatabase.getInstance(context)
                val repo = ReminderRepository(db)
                val occurrence = repo.occurrence(occurrenceId) ?: return@launch

                if (ChecklistValues.isClosed(occurrence.status)) {
                    repo.record(
                        occurrenceId = occurrenceId,
                        dateKey = occurrence.dateKey,
                        kind = ReminderEventKind.STALE,
                        detail = "ignored ${action.substringAfterLast('.')}: already ${occurrence.status}"
                    )
                    ReminderNotifier(context).cancel(occurrenceId)
                    ReminderScheduler(context).cancel(occurrenceId)
                    return@launch
                }

                ChecklistRepository(db).act(occurrenceId, checklistAction)
                repo.record(
                    occurrenceId = occurrenceId,
                    dateKey = occurrence.dateKey,
                    kind = ReminderEventKind.ACTION,
                    detail = checklistAction
                )

                if (ReminderActions.keepsReminder(action)) {
                    // Later is a snooze: the occurrence is still open, so the reminder
                    // returns when the snooze expires instead of ending here.
                    ReminderNotifier(context).cancel(occurrenceId)
                    ReminderScheduler(context).syncOne(occurrenceId)
                } else {
                    ReminderNotifier(context).cancel(occurrenceId)
                    ReminderScheduler(context).cancel(occurrenceId)
                    repo.recordPlan(
                        occurrenceId,
                        io.github.pabi277.dayledger.reminders.ReminderPlan(
                            status = io.github.pabi277.dayledger.reminders.PlanStatus.STOPPED,
                            atMillis = null,
                            audible = false,
                            reason = io.github.pabi277.dayledger.reminders.WaitReason.CLOSED
                        )
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
