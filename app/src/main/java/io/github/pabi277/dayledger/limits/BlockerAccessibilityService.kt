package io.github.pabi277.dayledger.limits

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import io.github.pabi277.dayledger.BlockScreenActivity
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockerAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        CoroutineScope(Dispatchers.IO).launch {
            val db = DayLedgerDatabase.getInstance(this@BlockerAccessibilityService)
            val info = db.appInfoDao().get(pkg) ?: return@launch
            if (info.alwaysAllowed) return@launch
            val earningSession = db.settingDao().get("earning_session_active") == "true"
            val category = info.category
            if (earningSession && category == "Earning") return@launch
            val cat = db.categoryDao().all().firstOrNull { it.name == category }
            val budget = cat?.budgetMin ?: return@launch
            val today = java.time.LocalDate.now().toString()
            val usage = db.usageDao().forDate(today).firstOrNull { it.packageName == pkg } ?: return@launch
            if (usage.foregroundMinutes < budget) return@launch
            // over limit: show block screen
            val intent = Intent(this@BlockerAccessibilityService, BlockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("package", pkg)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
