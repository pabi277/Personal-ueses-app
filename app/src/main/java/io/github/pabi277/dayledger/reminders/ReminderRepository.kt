package io.github.pabi277.dayledger.reminders

import androidx.room.withTransaction
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.SettingEntity
import io.github.pabi277.dayledger.data.checklist.ChecklistOccurrenceEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Database side of the Phase 10 reminder engine.
 *
 * [ReminderRules] decides, this class gathers. Everything it hands the engine comes from
 * persisted state, so a reboot, an app update or a killed process resumes exactly where the
 * schedule left off instead of restarting a reminder from scratch.
 */
class ReminderRepository(
    private val db: DayLedgerDatabase,
    private val zone: ZoneId = ZoneId.systemDefault()
) {

    private val occurrenceDao = db.checklistOccurrenceDao()
    private val stateDao = db.reminderStateDao()
    private val eventDao = db.reminderEventDao()
    private val settingDao = db.settingDao()

    companion object {
        // Settings keys. Nothing here overrides an owner edit; these are starting defaults.
        const val KEY_QUIET_FROM = "reminder_quiet_from"
        const val KEY_QUIET_TO = "reminder_quiet_to"
        const val KEY_CORE_CAP = "reminder_core_cap"
        const val KEY_GENERAL_CAP = "reminder_general_cap"
        const val KEY_PAUSE_UNTIL = "reminder_pause_until"
        const val KEY_LESS_OFTEN = "reminder_less_often"

        const val DEFAULT_QUIET_FROM = "22:00"
        const val DEFAULT_QUIET_TO = "07:00"
        const val DEFAULT_CORE_CAP = 6
        const val DEFAULT_GENERAL_CAP = 6

        /** Tolerance when deciding whether a scheduled alarm is due now. */
        const val DUE_TOLERANCE_MILLIS = 2_000L
    }

    // ------------------------------------------------------------- policy

    suspend fun policy(): ReminderPolicy {
        val quietFrom = parseTime(settingDao.get(KEY_QUIET_FROM) ?: DEFAULT_QUIET_FROM)
        val quietTo = parseTime(settingDao.get(KEY_QUIET_TO) ?: DEFAULT_QUIET_TO)
        val pause = settingDao.get(KEY_PAUSE_UNTIL)?.toLongOrNull()?.takeIf { it > 0 }
        val lessOften = (settingDao.get(KEY_LESS_OFTEN) ?: "")
            .split(",")
            .mapNotNull { key -> ReminderProfile.entries.firstOrNull { it.key == key.trim() } }
            .toSet()
        return ReminderPolicy(
            quietFrom = quietFrom,
            quietTo = quietTo,
            pausedUntil = pause,
            coreDailyCap = settingDao.get(KEY_CORE_CAP)?.toIntOrNull() ?: DEFAULT_CORE_CAP,
            generalDailyCap = settingDao.get(KEY_GENERAL_CAP)?.toIntOrNull() ?: DEFAULT_GENERAL_CAP,
            lessOften = lessOften
        )
    }

    suspend fun setQuietHours(from: String, to: String) {
        val f = normaliseTime(from) ?: return
        val t = normaliseTime(to) ?: return
        settingDao.upsert(SettingEntity(KEY_QUIET_FROM, f))
        settingDao.upsert(SettingEntity(KEY_QUIET_TO, t))
    }

    suspend fun setCoreCap(minutes: Int) {
        settingDao.upsert(SettingEntity(KEY_CORE_CAP, minutes.coerceIn(0, 50).toString()))
    }

    suspend fun setGeneralCap(count: Int) {
        settingDao.upsert(SettingEntity(KEY_GENERAL_CAP, count.coerceIn(0, 50).toString()))
    }

    /** Global Pause until [untilMillis]; `null` resumes immediately. */
    suspend fun setPauseUntil(untilMillis: Long?) {
        settingDao.upsert(SettingEntity(KEY_PAUSE_UNTIL, (untilMillis ?: 0L).toString()))
    }

    suspend fun setLessOften(profiles: Set<ReminderProfile>) {
        settingDao.upsert(
            SettingEntity(KEY_LESS_OFTEN, profiles.sortedBy { it.key }.joinToString(",") { it.key })
        )
    }

    // -------------------------------------------------------------- state

    suspend fun stateFor(occurrenceId: String): ReminderStateEntity? = stateDao.get(occurrenceId)

    /**
     * State for [occurrenceId], created on first use. A brand-new occurrence inherits the
     * profile the owner chose for its domain, else Standard. Core is never assumed: only an
     * explicit choice or the Top 3 marker promotes an item.
     */
    suspend fun ensureState(occurrence: ChecklistOccurrenceEntity): ReminderStateEntity {
        stateDao.get(occurrence.id)?.let { return it }
        val created = ReminderStateEntity(
            occurrenceId = occurrence.id,
            profile = defaultProfile(occurrence).key
        )
        stateDao.upsert(created)
        return created
    }

    private suspend fun defaultProfile(occurrence: ChecklistOccurrenceEntity): ReminderProfile =
        ReminderProfile.fromKey(
            settingDao.get(domainProfileKey(occurrence.domain)) ?: ReminderProfile.STANDARD.key
        )

    fun domainProfileKey(domain: String): String = "reminder_profile_domain_${domain.ifBlank { "any" }}"

    suspend fun setProfile(occurrenceId: String, profile: ReminderProfile) {
        stateDao.setProfile(occurrenceId, profile.key, System.currentTimeMillis())
    }

    suspend fun setDomainProfile(domain: String, profile: ReminderProfile) {
        settingDao.upsert(SettingEntity(domainProfileKey(domain), profile.key))
    }

    // --------------------------------------------------------------- plan

    /** Build the engine input for one occurrence from persisted state. */
    suspend fun inputFor(
        occurrence: ChecklistOccurrenceEntity,
        now: Long = System.currentTimeMillis()
    ): ReminderInput {
        val state = ensureState(occurrence)
        val todayKey = LocalDate.now(zone).toString()
        return ReminderInput(
            now = now,
            zone = zone,
            status = occurrence.status,
            dateKey = occurrence.dateKey,
            windowStartMin = occurrence.windowStartMin,
            snoozeUntil = occurrence.snoozeUntil,
            profile = ReminderProfile.fromKey(state.profile),
            audibleFires = state.audibleFires,
            lastFireAt = state.lastFireAt,
            anchorMillis = state.nextFireAt,
            coreFiresToday = eventDao.audibleCoreFires(todayKey),
            generalFiresToday = eventDao.audibleGeneralFires(todayKey),
            policy = policy()
        )
    }

    suspend fun planFor(
        occurrence: ChecklistOccurrenceEntity,
        now: Long = System.currentTimeMillis()
    ): ReminderPlan = ReminderRules.plan(inputFor(occurrence, now))

    /** Persist the time the engine decided on, so the health panel can show it. */
    suspend fun recordPlan(occurrenceId: String, plan: ReminderPlan) {
        stateDao.setNextFire(occurrenceId, plan.atMillis)
    }

    /**
     * Record one posted fire and advance the occurrence's counters.
     *
     * Called only after the notification is actually posted, so the log never claims a
     * reminder that the owner did not receive.
     */
    suspend fun recordFire(
        occurrence: ChecklistOccurrenceEntity,
        profile: ReminderProfile,
        audible: Boolean,
        now: Long = System.currentTimeMillis()
    ) = db.withTransaction {
        val state = ensureState(occurrence)
        stateDao.upsert(
            state.copy(
                audibleFires = state.audibleFires + if (audible) 1 else 0,
                totalFires = state.totalFires + 1,
                lastFireAt = now,
                updatedAt = now
            )
        )
        eventDao.append(
            ReminderEventEntity(
                occurrenceId = occurrence.id,
                dateKey = occurrence.dateKey,
                kind = ReminderEventKind.FIRE,
                profile = profile.key,
                audible = audible,
                at = now
            )
        )
    }

    suspend fun record(
        occurrenceId: String,
        dateKey: String,
        kind: String,
        detail: String = "",
        profile: ReminderProfile? = null,
        audible: Boolean = false,
        now: Long = System.currentTimeMillis()
    ) {
        eventDao.append(
            ReminderEventEntity(
                occurrenceId = occurrenceId,
                dateKey = dateKey,
                kind = kind,
                profile = profile?.key ?: "",
                audible = audible,
                detail = detail,
                at = now
            )
        )
    }

    suspend fun eventsFor(occurrenceId: String) = eventDao.forOccurrence(occurrenceId)

    /**
     * Remember whether the last scheduling attempt could use an exact alarm. Empty string
     * means "fine"; anything else is shown to the owner verbatim.
     */
    suspend fun recordDegraded(occurrenceId: String, degraded: String) {
        val state = stateDao.get(occurrenceId) ?: return
        if (state.degraded == degraded) return
        stateDao.setDegraded(occurrenceId, degraded, System.currentTimeMillis())
        if (degraded.isNotEmpty()) {
            eventDao.append(
                ReminderEventEntity(
                    occurrenceId = occurrenceId,
                    dateKey = state.occurrenceId.substringAfterLast(':'),
                    kind = ReminderEventKind.DEGRADED,
                    detail = degraded
                )
            )
        }
    }

    /** Every open occurrence today whose reminder state currently says "degraded". */
    suspend fun degradedCount(): Int {
        val openIds = openToday().map { it.id }.toSet()
        return stateDao.all().count { it.degraded.isNotEmpty() && it.occurrenceId in openIds }
    }

    suspend fun soonestFireAt(): Long? = stateDao.soonest()

    // ------------------------------------------------------------ queries

    /** Occurrences still open today; these are the ones an alarm may exist for. */
    suspend fun openToday(): List<ChecklistOccurrenceEntity> =
        occurrenceDao.forDate(LocalDate.now(zone).toString())
            .filter { !io.github.pabi277.dayledger.data.checklist.ChecklistValues.isClosed(it.status) }

    suspend fun occurrence(id: String): ChecklistOccurrenceEntity? = occurrenceDao.get(id)

    // -------------------------------------------------------------- time

    private fun parseTime(text: String): LocalTime? = normaliseTime(text)?.let { LocalTime.parse(it) }

    /** Accepts `7:05` as well as `07:05`; rejects anything else instead of guessing. */
    fun normaliseTime(text: String): String? {
        val parts = text.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return "%02d:%02d".format(h, m)
    }
}
