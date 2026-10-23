package io.github.pabi277.dayledger.limits

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import io.github.pabi277.dayledger.BlockScreenActivity
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.LimitEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Enforces limits when an app comes to the front.
 *
 * F2 (2026-10-23): this used to compare today's minutes against the **category budget only**,
 * so `app_info.limitMin` was ignored even when it had a value — the column was unwired in
 * both directions. The decision now comes from [LimitRules], where a per-app limit overrides
 * the category budget, and the reached limit is recorded against the number that was actually
 * exceeded instead of a placeholder `0`.
 *
 * The minutes compared are the stored, screen-on-clipped foreground minutes from the Phase 7
 * fix, which is the same figure Today displays — so an app blocks at the number the owner can
 * see, not at a different one.
 */
class BlockerAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        CoroutineScope(Dispatchers.IO).launch {
            val db = DayLedgerDatabase.getInstance(this@BlockerAccessibilityService)
            val info = db.appInfoDao().get(pkg) ?: return@launch

            val earningSession = db.settingDao().get("earning_session_active") == "true"
            val categoryBudget = db.categoryDao().all()
                .firstOrNull { it.name == info.category }?.budgetMin

            val today = LocalDate.now().toString()
            val minutesUsed = db.usageDao().forDate(today)
                .firstOrNull { it.packageName == pkg }?.foregroundMinutes ?: 0

            val decision = LimitRules.evaluate(
                minutesUsed = minutesUsed,
                appLimitMin = info.limitMin,
                categoryBudgetMin = categoryBudget,
                category = info.category,
                alwaysAllowed = info.alwaysAllowed,
                earningSessionActive = earningSession
            )
            if (!decision.shouldBlock) return@launch

            val limit = decision.effectiveLimitMin ?: return@launch

            // One block record per app per day: this service runs on every window change.
            if (db.limitEventDao().blockCountFor(today, pkg) == 0) {
                db.limitEventDao().insert(
                    LimitEventEntity(
                        dateKey = today,
                        packageName = pkg,
                        limitMin = limit,
                        reachedAt = System.currentTimeMillis(),
                        action = "block",
                        blockDurationMin = 0,
                        overrideType = "none"
                    )
                )
            }

            val intent = Intent(this@BlockerAccessibilityService, BlockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("package", pkg)
                // The block screen records the same limit that triggered it.
                putExtra("limitMin", limit)
                putExtra("limitSource", decision.source?.name ?: "")
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
