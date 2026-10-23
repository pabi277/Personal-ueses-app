package io.github.pabi277.dayledger.limits

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * F2 — per-app limits and the precedence rule.
 *
 * Owner decision 2026-10-23: a per-app limit overrides the category budget; the category
 * budget applies only to apps with no explicit per-app limit. Always-allowed and an active
 * earning session outrank every limit, because R16/DL24 must not be overridable by a number.
 *
 * Every package name and figure here is fictional.
 */
class LimitRulesTest {

    private fun evaluate(
        minutesUsed: Int = 60,
        appLimitMin: Int? = null,
        categoryBudgetMin: Int? = 120,
        category: String = "Study",
        alwaysAllowed: Boolean = false,
        earningSessionActive: Boolean = false
    ) = LimitRules.evaluate(
        minutesUsed = minutesUsed,
        appLimitMin = appLimitMin,
        categoryBudgetMin = categoryBudgetMin,
        category = category,
        alwaysAllowed = alwaysAllowed,
        earningSessionActive = earningSessionActive
    )

    // ----------------------------------------------------------- the owner's rule

    @Test
    fun `a per-app limit overrides the category budget`() {
        // Category allows 120, the owner set 30 for this app, 60 used: blocked.
        val decision = evaluate(minutesUsed = 60, appLimitMin = 30, categoryBudgetMin = 120)
        assertTrue(decision.shouldBlock)
        assertEquals(LimitSource.APP_LIMIT, decision.source)
        assertEquals(30, decision.effectiveLimitMin)
    }

    @Test
    fun `a per-app limit can be more generous than the category budget`() {
        // Category says 30, the owner set 90: 60 used is fine, because the explicit
        // per-app choice wins in both directions — not just the stricter one.
        val decision = evaluate(minutesUsed = 60, appLimitMin = 90, categoryBudgetMin = 30)
        assertFalse(decision.shouldBlock)
        assertEquals(LimitSource.APP_LIMIT, decision.source)
        assertEquals(90, decision.effectiveLimitMin)
    }

    @Test
    fun `without a per-app limit the category budget applies`() {
        val decision = evaluate(minutesUsed = 130, appLimitMin = null, categoryBudgetMin = 120)
        assertTrue(decision.shouldBlock)
        assertEquals(LimitSource.CATEGORY_BUDGET, decision.source)
        assertEquals(120, decision.effectiveLimitMin)
    }

    @Test
    fun `the limit is reached at exactly the limit, not one minute later`() {
        assertTrue(evaluate(minutesUsed = 30, appLimitMin = 30).shouldBlock)
        assertFalse(evaluate(minutesUsed = 29, appLimitMin = 30).shouldBlock)
    }

    // ------------------------------------------------------------ protections

    @Test
    fun `an always-allowed app is never blocked, even with a per-app limit`() {
        val decision = evaluate(minutesUsed = 600, appLimitMin = 5, alwaysAllowed = true)
        assertFalse(decision.shouldBlock)
        assertEquals(AllowReason.ALWAYS_ALLOWED, decision.allowedBecause)
        // Letting a number override this would break R16/DL24.
        assertNull(decision.source)
    }

    @Test
    fun `an active earning session protects an earning app`() {
        val decision = evaluate(
            minutesUsed = 600,
            appLimitMin = 5,
            category = LimitRules.EARNING_CATEGORY,
            earningSessionActive = true
        )
        assertFalse(decision.shouldBlock)
        assertEquals(AllowReason.EARNING_SESSION, decision.allowedBecause)
    }

    @Test
    fun `an earning session does not protect a non-earning app`() {
        val decision = evaluate(
            minutesUsed = 600,
            appLimitMin = 5,
            category = "Social/Waste",
            earningSessionActive = true
        )
        assertTrue(decision.shouldBlock)
        assertEquals(LimitSource.APP_LIMIT, decision.source)
    }

    @Test
    fun `with no limit at all nothing is blocked`() {
        val decision = evaluate(minutesUsed = 999, appLimitMin = null, categoryBudgetMin = null)
        assertFalse(decision.shouldBlock)
        assertEquals(AllowReason.NO_LIMIT, decision.allowedBecause)
        assertNull(decision.effectiveLimitMin)
    }

    // --------------------------------------------------------- effective limit

    @Test
    fun `a zero or negative per-app limit means no per-app limit`() {
        // A stored 0 must never be read as "block after zero minutes".
        assertEquals(
            LimitSource.CATEGORY_BUDGET to 120,
            LimitRules.effectiveLimit(0, 120)
        )
        assertEquals(
            LimitSource.CATEGORY_BUDGET to 120,
            LimitRules.effectiveLimit(-5, 120)
        )
        assertNull(LimitRules.effectiveLimit(null, null))
        assertNull(LimitRules.effectiveLimit(0, 0))
    }

    @Test
    fun `the effective limit is what both the screen and the blocker report`() {
        assertEquals(LimitSource.APP_LIMIT to 45, LimitRules.effectiveLimit(45, 120))
        assertEquals(LimitSource.CATEGORY_BUDGET to 120, LimitRules.effectiveLimit(null, 120))
        assertEquals("per-app limit 45m", LimitRules.describe(LimitSource.APP_LIMIT, 45))
        assertEquals("category budget 120m", LimitRules.describe(LimitSource.CATEGORY_BUDGET, 120))
    }

    @Test
    fun `a half-configured app still blocks on the one limit it has`() {
        // Per-app limit only, no category budget.
        assertTrue(evaluate(minutesUsed = 10, appLimitMin = 5, categoryBudgetMin = null).shouldBlock)
        // Category budget only, no per-app limit.
        assertTrue(evaluate(minutesUsed = 10, appLimitMin = null, categoryBudgetMin = 5).shouldBlock)
    }
}
