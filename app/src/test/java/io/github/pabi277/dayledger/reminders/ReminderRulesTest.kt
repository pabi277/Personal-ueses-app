package io.github.pabi277.dayledger.reminders

import io.github.pabi277.dayledger.data.checklist.ChecklistValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Phase 10 rules, tested without Android.
 *
 * Every case here is a "Done means" clause from the roadmap or a DL12/DL15/DL16 acceptance
 * test: Core repeats only within its profile, quiet hours never fire, caps are separate and
 * reset at midnight, Pause preserves due state, and a backlog is one fire — never a burst.
 */
class ReminderRulesTest {

    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")
    private val day = LocalDate.of(2026, 10, 23)

    private fun at(time: String): Long =
        LocalDateTime.parse("${day}T$time").atZone(zone).toInstant().toEpochMilli()

    private fun base(
        now: String = "09:00",
        status: String = ChecklistValues.STATUS_PENDING,
        profile: ReminderProfile = ReminderProfile.CORE,
        audibleFires: Int = 0,
        lastFireAt: Long? = null,
        anchorMillis: Long? = null,
        dateKey: String = day.toString(),
        windowStartMin: Int? = null,
        snoozeUntil: Long? = null,
        coreFires: Int = 0,
        generalFires: Int = 0,
        policy: ReminderPolicy = ReminderPolicy()
    ) = ReminderInput(
        now = at(now),
        zone = zone,
        status = status,
        dateKey = dateKey,
        windowStartMin = windowStartMin,
        snoozeUntil = snoozeUntil,
        profile = profile,
        audibleFires = audibleFires,
        lastFireAt = lastFireAt,
        anchorMillis = anchorMillis,
        coreFiresToday = coreFires,
        generalFiresToday = generalFires,
        policy = policy
    )

    private fun minutes(from: String, to: String): Long = (at(to) - at(from)) / 60_000L

    // ------------------------------------------------------------- cadence

    @Test
    fun `a newly tracked item waits a short grace before its first reminder`() {
        val plan = ReminderRules.plan(base(now = "09:00"))
        assertEquals(PlanStatus.SCHEDULED, plan.status)
        assertEquals(at("09:00") + ReminderRules.FIRST_FIRE_GRACE_MILLIS, plan.atMillis)
        assertTrue(plan.audible)
    }

    /**
     * The anchor is what keeps a first fire from sliding forward. Without it, every launch,
     * action or retry would recompute "now + grace" and the reminder could never arrive.
     */
    @Test
    fun `re-planning a pending first fire reproduces the same time`() {
        val first = ReminderRules.plan(base(now = "09:00"))
        val again = ReminderRules.plan(base(now = "09:03", anchorMillis = first.atMillis))
        assertEquals(first.atMillis, again.atMillis)
        assertEquals(PlanStatus.SCHEDULED, again.status)
    }

    @Test
    fun `an anchor that has arrived is due now rather than postponed again`() {
        val plan = ReminderRules.plan(
            base(now = "09:05", anchorMillis = at("09:05"))
        )
        assertEquals(PlanStatus.SCHEDULED, plan.status)
        assertEquals(at("09:05"), plan.atMillis)
    }

    @Test
    fun `a late launch fires one reminder, not one per missed repeat`() {
        // Generated this morning, app first opened at 21:00: a single fire is due.
        val plan = ReminderRules.plan(base(now = "21:00", anchorMillis = at("09:05")))
        assertEquals(PlanStatus.SCHEDULED, plan.status)
        assertEquals(at("21:00"), plan.atMillis)
    }

    @Test
    fun `core repeats every twenty minutes`() {
        val plan = ReminderRules.plan(
            base(now = "09:05", lastFireAt = at("09:00"), audibleFires = 1)
        )
        assertEquals(PlanStatus.SCHEDULED, plan.status)
        assertEquals(20L, minutes("09:00", "09:20"))
        assertEquals(at("09:20"), plan.atMillis)
    }

    @Test
    fun `standard repeats once after fifty minutes`() {
        val plan = ReminderRules.plan(
            base(now = "09:05", profile = ReminderProfile.STANDARD, lastFireAt = at("09:00"), audibleFires = 1)
        )
        assertEquals(at("09:50"), plan.atMillis)
    }

    @Test
    fun `a flexible reminder fires once and then stops`() {
        val after = ReminderRules.plan(
            base(now = "09:05", profile = ReminderProfile.FLEXIBLE, lastFireAt = at("09:00"), audibleFires = 1)
        )
        assertEquals(PlanStatus.STOPPED, after.status)
        assertEquals(WaitReason.AUDIBLE_BUDGET, after.reason)
    }

    @Test
    fun `core stops sounding after four fires and stays silently visible`() {
        val plan = ReminderRules.plan(
            base(now = "10:00", lastFireAt = at("09:40"), audibleFires = 4)
        )
        assertEquals(PlanStatus.STOPPED, plan.status)
        assertEquals(WaitReason.AUDIBLE_BUDGET, plan.reason)
        assertNull(plan.atMillis)
    }

    @Test
    fun `standard stops after its one repeat`() {
        val plan = ReminderRules.plan(
            base(now = "10:00", profile = ReminderProfile.STANDARD, lastFireAt = at("09:50"), audibleFires = 2)
        )
        assertEquals(PlanStatus.STOPPED, plan.status)
    }

    /**
     * The phone was off from 09:00 to 13:00. Ten Core repeats were missed; exactly one fire
     * is due, now. A backlog must never arrive as a burst.
     */
    @Test
    fun `a long gap yields one late fire instead of a burst`() {
        val plan = ReminderRules.plan(
            base(now = "13:00", lastFireAt = at("09:00"), audibleFires = 1)
        )
        assertEquals(PlanStatus.SCHEDULED, plan.status)
        assertEquals(at("13:00"), plan.atMillis)
    }

    // --------------------------------------------------------- less often

    @Test
    fun `less often doubles only the selected profile's interval`() {
        val policy = ReminderPolicy(lessOften = setOf(ReminderProfile.CORE))
        val core = ReminderRules.plan(
            base(now = "09:05", profile = ReminderProfile.CORE, lastFireAt = at("09:00"), audibleFires = 1, policy = policy)
        )
        assertEquals(at("09:40"), core.atMillis)

        val standard = ReminderRules.plan(
            base(now = "09:05", profile = ReminderProfile.STANDARD, lastFireAt = at("09:00"), audibleFires = 1, policy = policy)
        )
        assertEquals(at("09:50"), standard.atMillis)
    }

    @Test
    fun `less often halves the core audible budget but never below one`() {
        val policy = ReminderPolicy(lessOften = setOf(ReminderProfile.CORE))
        // Two audible fires already used the halved budget of two.
        val plan = ReminderRules.plan(
            base(now = "10:00", audibleFires = 2, lastFireAt = at("09:40"), policy = policy)
        )
        assertEquals(PlanStatus.STOPPED, plan.status)

        val flexible = ReminderRules.plan(
            base(now = "10:00", profile = ReminderProfile.FLEXIBLE, audibleFires = 1, lastFireAt = at("09:40"),
                policy = ReminderPolicy(lessOften = setOf(ReminderProfile.FLEXIBLE)))
        )
        assertEquals(PlanStatus.STOPPED, flexible.status)
    }

    // -------------------------------------------------------- quiet hours

    @Test
    fun `quiet hours move the fire to the end of the window`() {
        val policy = ReminderPolicy(quietFrom = LocalTime.parse("13:00"), quietTo = LocalTime.parse("15:00"))
        val plan = ReminderRules.plan(base(now = "13:30", policy = policy))
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(WaitReason.QUIET_HOURS, plan.reason)
        assertEquals(at("15:00"), plan.atMillis)
    }

    @Test
    fun `quiet hours that wrap midnight resume the next morning`() {
        val policy = ReminderPolicy(quietFrom = LocalTime.parse("22:00"), quietTo = LocalTime.parse("07:00"))
        val plan = ReminderRules.plan(base(now = "23:30", policy = policy))
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(
            LocalDateTime.parse("2026-10-24T07:00").atZone(zone).toInstant().toEpochMilli(),
            plan.atMillis
        )
    }

    @Test
    fun `a fire that would land inside quiet hours is moved out of them`() {
        // 09:00 + 20 min = 09:20, but quiet hours start at 09:10.
        val policy = ReminderPolicy(quietFrom = LocalTime.parse("09:10"), quietTo = LocalTime.parse("09:30"))
        val plan = ReminderRules.plan(
            base(now = "09:00", lastFireAt = at("09:00"), audibleFires = 1, policy = policy)
        )
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(at("09:30"), plan.atMillis)
    }

    @Test
    fun `a zero length quiet window means quiet hours are off`() {
        val policy = ReminderPolicy(quietFrom = LocalTime.parse("09:00"), quietTo = LocalTime.parse("09:00"))
        assertFalse(ReminderRules.inQuietHours(LocalTime.parse("09:00"), policy))
        val plan = ReminderRules.plan(base(now = "09:00", policy = policy))
        assertEquals(PlanStatus.SCHEDULED, plan.status)
    }

    @Test
    fun `quiet hours are inclusive at the start and exclusive at the end`() {
        val policy = ReminderPolicy(quietFrom = LocalTime.parse("22:00"), quietTo = LocalTime.parse("07:00"))
        assertTrue(ReminderRules.inQuietHours(LocalTime.parse("22:00"), policy))
        assertTrue(ReminderRules.inQuietHours(LocalTime.parse("03:00"), policy))
        assertFalse(ReminderRules.inQuietHours(LocalTime.parse("07:00"), policy))
        assertFalse(ReminderRules.inQuietHours(LocalTime.parse("21:59"), policy))
    }

    // -------------------------------------------------------------- pause

    @Test
    fun `pause preserves due state and waits for the resume time`() {
        val resume = at("11:00")
        val plan = ReminderRules.plan(base(now = "09:00", policy = ReminderPolicy(pausedUntil = resume)))
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(WaitReason.PAUSED, plan.reason)
        assertEquals(resume, plan.atMillis)
        // Still pending: pausing never completes or skips the item.
        assertFalse(ChecklistValues.isClosed(ChecklistValues.STATUS_PENDING))
    }

    @Test
    fun `an expired pause is ignored`() {
        val plan = ReminderRules.plan(base(now = "09:00", policy = ReminderPolicy(pausedUntil = at("08:00"))))
        assertEquals(PlanStatus.SCHEDULED, plan.status)
    }

    // ---------------------------------------------------------------- caps

    @Test
    fun `the core daily cap holds the next fire until midnight`() {
        val plan = ReminderRules.plan(base(now = "21:00", coreFires = 6, policy = ReminderPolicy(coreDailyCap = 6)))
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(WaitReason.CORE_DAILY_CAP, plan.reason)
        assertEquals(
            LocalDateTime.parse("2026-10-24T00:00").atZone(zone).toInstant().toEpochMilli(),
            plan.atMillis
        )
    }

    @Test
    fun `the general cap is counted separately from the core cap`() {
        // Six Core fires do not stop a Standard reminder.
        val plan = ReminderRules.plan(
            base(now = "21:00", profile = ReminderProfile.STANDARD, coreFires = 6, generalFires = 0)
        )
        assertEquals(PlanStatus.SCHEDULED, plan.status)
    }

    @Test
    fun `the general cap stops non-core reminders`() {
        val plan = ReminderRules.plan(
            base(now = "21:00", profile = ReminderProfile.STANDARD, generalFires = 6,
                policy = ReminderPolicy(generalDailyCap = 6))
        )
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(WaitReason.GENERAL_DAILY_CAP, plan.reason)
    }

    // ------------------------------------------------------------ lifecycle

    @Test
    fun `a settled occurrence never fires again`() {
        listOf(
            ChecklistValues.STATUS_FULL,
            ChecklistValues.STATUS_MINIMUM,
            ChecklistValues.STATUS_SKIPPED,
            ChecklistValues.STATUS_CARRIED,
            ChecklistValues.STATUS_DROPPED
        ).forEach { status ->
            val plan = ReminderRules.plan(base(status = status))
            assertEquals("status $status must stop", PlanStatus.STOPPED, plan.status)
            assertEquals(WaitReason.CLOSED, plan.reason)
        }
    }

    @Test
    fun `yesterday's unfinished item does not ring today`() {
        val plan = ReminderRules.plan(base(dateKey = day.minusDays(1).toString()))
        assertEquals(PlanStatus.STOPPED, plan.status)
        assertEquals(WaitReason.DAY_ENDED, plan.reason)
    }

    @Test
    fun `snooze waits for the snooze time`() {
        val snooze = at("09:30")
        val plan = ReminderRules.plan(base(now = "09:00", snoozeUntil = snooze))
        assertEquals(PlanStatus.WAITING, plan.status)
        assertEquals(WaitReason.SNOOZED, plan.reason)
        assertEquals(snooze, plan.atMillis)
    }

    @Test
    fun `a window start defers the first fire but not a later one`() {
        val first = ReminderRules.plan(base(now = "08:00", windowStartMin = 9 * 60))
        assertEquals(at("09:00"), first.atMillis)

        val later = ReminderRules.plan(
            base(now = "09:05", windowStartMin = 9 * 60, lastFireAt = at("09:00"), audibleFires = 1)
        )
        assertEquals(at("09:20"), later.atMillis)
    }

    @Test
    fun `a reminder cannot fire after its own day ends`() {
        // 23:55 + 20 min crosses midnight; the occurrence belongs to the day that ends.
        val plan = ReminderRules.plan(
            base(now = "23:55", lastFireAt = at("23:45"), audibleFires = 1)
        )
        assertEquals(PlanStatus.STOPPED, plan.status)
        assertEquals(WaitReason.DAY_ENDED, plan.reason)
    }

    // -------------------------------------------------------------- profile

    @Test
    fun `profile defaults match the approved starting values`() {
        assertEquals(ProfileDefaults(20, 4), ReminderProfiles.defaults(ReminderProfile.CORE))
        assertEquals(ProfileDefaults(50, 2), ReminderProfiles.defaults(ReminderProfile.STANDARD))
        assertEquals(ProfileDefaults(null, 1), ReminderProfiles.defaults(ReminderProfile.FLEXIBLE))
    }

    @Test
    fun `an unknown stored profile falls back to standard instead of throwing`() {
        assertEquals(ReminderProfile.STANDARD, ReminderProfile.fromKey("nonsense"))
        assertEquals(ReminderProfile.CORE, ReminderProfile.fromKey("core"))
    }

    // -------------------------------------------------------------- actions

    @Test
    fun `notification actions map onto the checklist vocabulary`() {
        assertEquals(
            ChecklistValues.ACTION_FULL,
            ReminderActions.fromIntentAction(ReminderActions.ACTION_DONE)
        )
        assertEquals(
            ChecklistValues.ACTION_MINIMUM,
            ReminderActions.fromIntentAction(ReminderActions.ACTION_MINIMUM)
        )
        assertEquals(
            ChecklistValues.ACTION_LATER,
            ReminderActions.fromIntentAction(ReminderActions.ACTION_LATER)
        )
        assertEquals(
            ChecklistValues.ACTION_SKIP,
            ReminderActions.fromIntentAction(ReminderActions.ACTION_SKIP)
        )
        assertNull(ReminderActions.fromIntentAction("something.else"))
    }

    @Test
    fun `later keeps the reminder alive and every other action ends it`() {
        assertTrue(ReminderActions.keepsReminder(ReminderActions.ACTION_LATER))
        assertFalse(ReminderActions.keepsReminder(ReminderActions.ACTION_DONE))
        assertFalse(ReminderActions.keepsReminder(ReminderActions.ACTION_MINIMUM))
        assertFalse(ReminderActions.keepsReminder(ReminderActions.ACTION_SKIP))
        assertFalse(ReminderActions.keepsReminder(null))
    }
}
