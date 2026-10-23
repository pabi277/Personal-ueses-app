package io.github.pabi277.dayledger.reminders

import io.github.pabi277.dayledger.data.checklist.ChecklistValues
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Phase 10 — the persistent reminder engine, kept as pure rules.
 *
 * Everything about *when* a checklist reminder may fire lives here, with no Android types
 * and no database access, so the awkward cases (quiet hours that wrap midnight, a cap
 * reached at 21:00, a phone that was off all afternoon) are unit-testable.
 *
 * The contract from `docs/REQUIREMENTS.md` ("Persistent reminder policy"):
 *
 * - Lifecycle `SCHEDULED → DUE ↔ SNOOZED → FULL | MINIMUM | SKIPPED`; a closed occurrence
 *   never fires again.
 * - **Core:** every 20 minutes, at most four sound/vibration fires per occurrence, subject
 *   to a separate daily Core cap; after that it stays visible silently.
 * - **Standard:** an initial reminder and one repeat 45–60 minutes later (50 here).
 * - **Flexible:** one gentle reminder.
 * - Quiet hours are a **no-fire window**: compute the next eligible time, never burst the
 *   repeats that were missed.
 * - Global Pause preserves due state and resumes at the chosen time.
 * - Daily caps and Pause never produce an alert storm.
 *
 * One deliberate rule that is not in the requirements document but follows from "never
 * burst": when a computed fire time is already in the past (the phone was off, the app was
 * killed, Android delayed an alarm), [ReminderRules.plan] clamps it to *now* and returns a
 * single fire. Being three hours late does not mean twelve reminders arrive at once.
 */
enum class ReminderProfile(val key: String, val label: String) {
    CORE("core", "Core"),
    STANDARD("standard", "Standard"),
    FLEXIBLE("flexible", "Flexible");

    companion object {
        fun fromKey(key: String): ReminderProfile =
            entries.firstOrNull { it.key == key } ?: STANDARD
    }
}

/** The approved starting cadence for one profile, before any owner edit. */
data class ProfileDefaults(
    /** Minutes between fires, or `null` when the profile fires once. */
    val intervalMin: Int?,
    /** Sound/vibration fires allowed for one occurrence. */
    val maxAudible: Int
)

object ReminderProfiles {

    fun defaults(profile: ReminderProfile): ProfileDefaults = when (profile) {
        ReminderProfile.CORE -> ProfileDefaults(intervalMin = 20, maxAudible = 4)
        // "initial plus one repeat after 45–60 minutes": 50 sits in the middle.
        ReminderProfile.STANDARD -> ProfileDefaults(intervalMin = 50, maxAudible = 2)
        ReminderProfile.FLEXIBLE -> ProfileDefaults(intervalMin = null, maxAudible = 1)
    }

    /**
     * Less Often halves the noise of one profile only: the interval doubles and the audible
     * budget halves, never below one. It cannot touch another profile (DL15).
     */
    fun effective(profile: ReminderProfile, lessOften: Boolean): ProfileDefaults {
        val base = defaults(profile)
        if (!lessOften) return base
        return ProfileDefaults(
            intervalMin = base.intervalMin?.times(2),
            maxAudible = maxOf(1, base.maxAudible / 2)
        )
    }
}

enum class PlanStatus {
    /** A fire is owed at [ReminderPlan.atMillis]. */
    SCHEDULED,

    /** Nothing now; the next eligible moment is [ReminderPlan.atMillis]. */
    WAITING,

    /** This occurrence will not fire again. */
    STOPPED
}

enum class WaitReason {
    NONE,
    CLOSED,
    DAY_ENDED,
    SNOOZED,
    PAUSED,
    QUIET_HOURS,
    CORE_DAILY_CAP,
    GENERAL_DAILY_CAP,
    AUDIBLE_BUDGET
}

data class ReminderPlan(
    val status: PlanStatus,
    /** Fire (or resume) time. `null` when [status] is [PlanStatus.STOPPED]. */
    val atMillis: Long?,
    val audible: Boolean,
    val reason: WaitReason
) {
    val stopped: Boolean get() = status == PlanStatus.STOPPED
}

/** Everything the engine needs to know, gathered by [ReminderRepository]. */
data class ReminderInput(
    val now: Long,
    val zone: ZoneId,
    /** Occurrence status, e.g. `pending`, `later`, `full`. */
    val status: String,
    /** The occurrence's own date, `yyyy-MM-dd`. */
    val dateKey: String,
    /** Optional window start, minutes from midnight, so a reminder can wait for its window. */
    val windowStartMin: Int? = null,
    val snoozeUntil: Long? = null,
    val profile: ReminderProfile = ReminderProfile.STANDARD,
    val audibleFires: Int = 0,
    val lastFireAt: Long? = null,
    /**
     * The persisted `nextFireAt` for this occurrence, if the scheduler has already decided
     * one. It anchors the *first* fire so re-planning (every launch, every action, every
     * retry) reproduces the same time instead of sliding it forward to "now".
     */
    val anchorMillis: Long? = null,
    val coreFiresToday: Int = 0,
    val generalFiresToday: Int = 0,
    val policy: ReminderPolicy = ReminderPolicy()
)

/** Owner-controlled settings. Every field is editable; nothing here is a hard limit. */
data class ReminderPolicy(
    val quietFrom: LocalTime? = null,
    val quietTo: LocalTime? = null,
    /** Epoch millis of a global Pause, or `null`. */
    val pausedUntil: Long? = null,
    val coreDailyCap: Int = 6,
    val generalDailyCap: Int = 6,
    /** Profiles the owner has turned down with "Less often". */
    val lessOften: Set<ReminderProfile> = emptySet()
)

object ReminderRules {

    /**
     * How long a newly tracked item waits before its first reminder.
     *
     * Without this, the first sync after a launch would compute "now" and every pending
     * item would alert the moment the app opened. Five minutes is long enough to finish
     * what you opened the app for, short enough that the item is not forgotten.
     */
    const val FIRST_FIRE_GRACE_MILLIS = 5L * 60_000L

    fun plan(input: ReminderInput): ReminderPlan {
        val zone = input.zone
        val today = Instant.ofEpochMilli(input.now).atZone(zone).toLocalDate()
        val ownDate = runCatching { LocalDate.parse(input.dateKey) }.getOrNull()

        // A closed occurrence is finished: Done, Minimum, Skip, carried or dropped.
        if (ChecklistValues.isClosed(input.status)) return stop(WaitReason.CLOSED)
        if (ownDate == null || ownDate.isBefore(today)) return stop(WaitReason.DAY_ENDED)

        input.snoozeUntil?.takeIf { it > input.now }?.let {
            return wait(it, WaitReason.SNOOZED)
        }
        input.policy.pausedUntil?.takeIf { it > input.now }?.let {
            return wait(it, WaitReason.PAUSED)
        }

        val defaults = ReminderProfiles.effective(
            input.profile,
            input.profile in input.policy.lessOften
        )

        // Separate daily caps for Core and for everything else (DL15).
        val isCore = input.profile == ReminderProfile.CORE
        val cap = if (isCore) input.policy.coreDailyCap else input.policy.generalDailyCap
        val used = if (isCore) input.coreFiresToday else input.generalFiresToday
        if (cap > 0 && used >= cap) {
            return wait(
                nextMidnight(input.now, zone),
                if (isCore) WaitReason.CORE_DAILY_CAP else WaitReason.GENERAL_DAILY_CAP
            )
        }

        // The occurrence has used its sound/vibration budget: it stays visible silently.
        if (input.audibleFires >= defaults.maxAudible) return stop(WaitReason.AUDIBLE_BUDGET)

        val windowStart = input.windowStartMin?.let { startOfMinute(ownDate, it, zone) }
        var candidate: Long = when {
            input.lastFireAt != null && defaults.intervalMin != null ->
                input.lastFireAt!! + defaults.intervalMin!! * 60_000L
            // A single-fire profile that has already fired once.
            input.lastFireAt != null -> return stop(WaitReason.AUDIBLE_BUDGET)
            // Anchor first: a reminder that was already decided must not be re-decided
            // closer to "now" every time something re-plans it.
            input.anchorMillis != null -> input.anchorMillis!!
            windowStart != null && windowStart > input.now -> windowStart
            // Just-created item: a short grace so opening the app does not pop a reminder
            // in the same second, and so a first sync cannot fire a batch of them at once.
            else -> input.now + FIRST_FIRE_GRACE_MILLIS
        }

        // Never replay a backlog: one late fire, not a burst.
        if (candidate < input.now) candidate = input.now

        val candidateTime = Instant.ofEpochMilli(candidate).atZone(zone)
        if (inQuietHours(candidateTime.toLocalTime(), input.policy)) {
            return wait(
                nextQuietEnd(candidate, zone, input.policy),
                WaitReason.QUIET_HOURS
            )
        }
        // Past the occurrence's own day: stop rather than fire tomorrow for yesterday.
        if (candidateTime.toLocalDate().isAfter(ownDate)) return stop(WaitReason.DAY_ENDED)

        return ReminderPlan(
            status = PlanStatus.SCHEDULED,
            atMillis = candidate,
            audible = true,
            reason = WaitReason.NONE
        )
    }

    /** True during quiet hours. A zero-length window (`from == to`) means "not configured". */
    fun inQuietHours(time: LocalTime, policy: ReminderPolicy): Boolean {
        val from = policy.quietFrom ?: return false
        val to = policy.quietTo ?: return false
        if (from == to) return false
        return if (from < to) {
            time >= from && time < to
        } else {
            // Wraps midnight, e.g. 22:00 → 07:00.
            time >= from || time < to
        }
    }

    /**
     * The first moment at or after [nowMillis] that is outside quiet hours. Returns
     * [nowMillis] unchanged when there is no quiet window or [nowMillis] is already clear.
     */
    fun nextQuietEnd(nowMillis: Long, zone: ZoneId, policy: ReminderPolicy): Long {
        val from = policy.quietFrom ?: return nowMillis
        val to = policy.quietTo ?: return nowMillis
        if (from == to) return nowMillis
        val nowZdt = Instant.ofEpochMilli(nowMillis).atZone(zone)
        if (!inQuietHours(nowZdt.toLocalTime(), policy)) return nowMillis
        val todayEnd = nowZdt.toLocalDate().atTime(to).atZone(zone).toInstant().toEpochMilli()
        return if (todayEnd > nowMillis) {
            todayEnd
        } else {
            nowZdt.toLocalDate().plusDays(1).atTime(to).atZone(zone).toInstant().toEpochMilli()
        }
    }

    /** Local midnight that starts the next day after [nowMillis]. Caps reset here. */
    fun nextMidnight(nowMillis: Long, zone: ZoneId): Long {
        val zdt = Instant.ofEpochMilli(nowMillis).atZone(zone)
        return zdt.toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun startOfMinute(date: LocalDate, minuteOfDay: Int, zone: ZoneId): Long =
        date.atStartOfDay(zone).plusMinutes(minuteOfDay.toLong()).toInstant().toEpochMilli()

    private fun wait(at: Long, reason: WaitReason) =
        ReminderPlan(PlanStatus.WAITING, at, audible = true, reason = reason)

    private fun stop(reason: WaitReason) =
        ReminderPlan(PlanStatus.STOPPED, null, audible = false, reason = reason)
}

/**
 * Notification actions, mapped onto the checklist vocabulary.
 *
 * These are the same transitions the Today screen writes, so a reminder tap and a screen
 * tap land in the same append-only `completion_logs` audit trail. Idempotency is enforced
 * by the repository ([io.github.pabi277.dayledger.data.checklist.ChecklistRepository.act]
 * ignores an action on a closed occurrence) and recorded by the caller, so a stale
 * notification can never complete the same item twice.
 */
object ReminderActions {
    const val ACTION_DONE = "io.github.pabi277.dayledger.reminders.DONE"
    const val ACTION_MINIMUM = "io.github.pabi277.dayledger.reminders.MINIMUM"
    const val ACTION_LATER = "io.github.pabi277.dayledger.reminders.LATER"
    const val ACTION_SKIP = "io.github.pabi277.dayledger.reminders.SKIP"
    const val ACTION_OPEN = "io.github.pabi277.dayledger.reminders.OPEN"

    fun fromIntentAction(action: String?): String? = when (action) {
        ACTION_DONE -> ChecklistValues.ACTION_FULL
        ACTION_MINIMUM -> ChecklistValues.ACTION_MINIMUM
        ACTION_LATER -> ChecklistValues.ACTION_LATER
        ACTION_SKIP -> ChecklistValues.ACTION_SKIP
        else -> null
    }

    /**
     * `Later` is a snooze, so it keeps the occurrence alive and the reminder returns after
     * the snooze. Every other action settles the item and cancels its remaining fires.
     */
    fun keepsReminder(action: String?): Boolean = action == ACTION_LATER
}
