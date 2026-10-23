package io.github.pabi277.dayledger.limits

/**
 * F2 — per-app limits, and the one precedence rule that decides whether an app is blocked.
 *
 * Owner decision 2026-10-23: **a per-app limit overrides the category budget**; the category
 * budget applies only to apps with no explicit per-app limit. Everything else is settled
 * here, in one place, so an app can never be blocked for a reason the owner cannot see.
 *
 * Precedence, in order — first match wins:
 *
 * 1. **Always-allowed** → never blocked. R16 and DL24 require that a checklist or a limit can
 *    never lock an essential app. A number typed into Limits must not be able to override
 *    that, so this is checked before any limit is even considered.
 * 2. **Active earning session on an Earning app** → never blocked. Blocking mid-offerwall would
 *    destroy the work that is being paid for; this is the existing protection, kept.
 * 3. **Per-app `limitMin`, if set** → the effective limit. This is the owner's rule.
 * 4. **Category budget, otherwise** → the effective limit.
 * 5. **No limit at all** → allowed.
 *
 * The blocker previously read **only** the category budget and ignored `limitMin` entirely,
 * which is why per-app limits were unimplemented rather than merely unpersisted. Nothing in
 * that path is kept: this object is now the single decision point.
 */
enum class LimitSource {
    /** The per-app `limitMin` the owner set on the Limits screen. */
    APP_LIMIT,

    /** The category budget, used only when the app has no per-app limit. */
    CATEGORY_BUDGET
}

enum class AllowReason {
    ALWAYS_ALLOWED,
    EARNING_SESSION,
    NO_LIMIT
}

data class LimitDecision(
    val shouldBlock: Boolean,
    /** Which limit applied, when one did. */
    val source: LimitSource? = null,
    /** The number that was exceeded — what the block screen must show. */
    val effectiveLimitMin: Int? = null,
    /** Why the app was allowed, when it was. */
    val allowedBecause: AllowReason? = null,
    val minutesUsed: Int = 0
) {
    companion object {
        fun allow(minutesUsed: Int, reason: AllowReason) =
            LimitDecision(shouldBlock = false, allowedBecause = reason, minutesUsed = minutesUsed)

        fun block(minutesUsed: Int, source: LimitSource, limitMin: Int) =
            LimitDecision(
                shouldBlock = true,
                source = source,
                effectiveLimitMin = limitMin,
                minutesUsed = minutesUsed
            )
    }
}

object LimitRules {

    /** The category whose budget yields to an active earning session. */
    const val EARNING_CATEGORY = "Earning"

    /**
     * Resolve which limit applies to an app, if any. Exposed separately because the Limits
     * screen shows the owner the same answer the blocker will act on.
     */
    fun effectiveLimit(appLimitMin: Int?, categoryBudgetMin: Int?): Pair<LimitSource, Int>? {
        // A per-app limit is explicit, so it always wins over the category default.
        if (appLimitMin != null && appLimitMin > 0) return LimitSource.APP_LIMIT to appLimitMin
        if (categoryBudgetMin != null && categoryBudgetMin > 0) {
            return LimitSource.CATEGORY_BUDGET to categoryBudgetMin
        }
        return null
    }

    fun evaluate(
        minutesUsed: Int,
        appLimitMin: Int?,
        categoryBudgetMin: Int?,
        category: String,
        alwaysAllowed: Boolean,
        earningSessionActive: Boolean
    ): LimitDecision {
        if (alwaysAllowed) return LimitDecision.allow(minutesUsed, AllowReason.ALWAYS_ALLOWED)
        if (earningSessionActive && category == EARNING_CATEGORY) {
            return LimitDecision.allow(minutesUsed, AllowReason.EARNING_SESSION)
        }
        val (source, limit) = effectiveLimit(appLimitMin, categoryBudgetMin)
            ?: return LimitDecision.allow(minutesUsed, AllowReason.NO_LIMIT)
        return if (minutesUsed >= limit) {
            LimitDecision.block(minutesUsed, source, limit)
        } else {
            LimitDecision(
                shouldBlock = false,
                source = source,
                effectiveLimitMin = limit,
                minutesUsed = minutesUsed
            )
        }
    }

    /** Plain-language label for the Limits screen and the block screen. */
    fun describe(source: LimitSource, limitMin: Int): String = when (source) {
        LimitSource.APP_LIMIT -> "per-app limit ${limitMin}m"
        LimitSource.CATEGORY_BUDGET -> "category budget ${limitMin}m"
    }
}
