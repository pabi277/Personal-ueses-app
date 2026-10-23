package io.github.pabi277.dayledger.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Rebuilds every schedule after an event that invalidates it (Phase 10.4).
 *
 * `AlarmManager` alarms do not survive a reboot, and a clock or time-zone change moves
 * every wall-clock time the engine computed. Rather than trying to repair individual
 * alarms, this cancels nothing and simply re-derives each occurrence's next fire from
 * persisted state — the same code path as a normal launch, so there is one implementation
 * to trust instead of three.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in REBUILD_ACTIONS) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationRepository(context).seedAndSchedule()
                ReminderScheduler(context).syncAll()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        val REBUILD_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}
