package io.github.pabi277.dayledger.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ruleId = intent.getStringExtra("ruleId") ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DayLedgerDatabase.getInstance(context)
                val rule = db.notificationRuleDao().get(ruleId) ?: return@launch
                val repo = NotificationRepository(context)
                if (repo.fireIfAllowed(rule, rule.templateText)) {
                    repo.scheduleDaily(rule)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
