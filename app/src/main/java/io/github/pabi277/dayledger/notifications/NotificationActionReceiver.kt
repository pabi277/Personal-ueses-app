package io.github.pabi277.dayledger.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationRepository.ACTION_LESS_OFTEN) return
        val ruleId = intent.getStringExtra("ruleId") ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DayLedgerDatabase.getInstance(context).notificationRuleDao().setEnabled(ruleId, false)
            } finally {
                pending.finish()
            }
        }
    }
}
