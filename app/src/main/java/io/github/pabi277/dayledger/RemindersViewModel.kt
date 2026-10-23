package io.github.pabi277.dayledger

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.notifications.ReminderScheduler
import io.github.pabi277.dayledger.reminders.ReminderProfile
import io.github.pabi277.dayledger.reminders.ReminderRepository
import io.github.pabi277.dayledger.reminders.ReminderStateEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ReminderItemUi(
    val occurrenceId: String,
    val title: String,
    val domain: String,
    val profile: String,
    val nextFireAt: Long?,
    val audibleFires: Int,
    val degraded: String
)

data class RemindersUiState(
    val loaded: Boolean = false,
    val pausedUntil: Long? = null,
    val quietFrom: String = ReminderRepository.DEFAULT_QUIET_FROM,
    val quietTo: String = ReminderRepository.DEFAULT_QUIET_TO,
    val coreCap: Int = ReminderRepository.DEFAULT_CORE_CAP,
    val generalCap: Int = ReminderRepository.DEFAULT_GENERAL_CAP,
    val lessOften: List<String> = emptyList(),
    val items: List<ReminderItemUi> = emptyList(),
    val nextFireAt: Long? = null,
    /** False when Android will not give exact alarms, so timing may be late. */
    val exactAlarms: Boolean = true,
    val notificationsAllowed: Boolean = true,
    val degradedCount: Int = 0,
    val message: String? = null,
    val error: String? = null
)

/**
 * Phase 10.3 — the controls. Every mutation writes a setting, rebuilds the schedule from it
 * and reloads, so what the screen says is what the engine will do on the next alarm.
 */
class RemindersViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DayLedgerDatabase.getInstance(app)
    private val repo = ReminderRepository(db)
    private val scheduler = ReminderScheduler(app)

    private val _state = MutableStateFlow(RemindersUiState())
    val state: StateFlow<RemindersUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch { refresh() }
    }

    /** Called once at launch: rebuild alarms, then show the resulting state. */
    fun syncSchedules() {
        viewModelScope.launch {
            runCatching { scheduler.syncAll() }
            refresh()
        }
    }

    private suspend fun refresh() {
        try {
            val policy = repo.policy()
            val open = repo.openToday()
            val items = open.map { occurrence ->
                val state = repo.stateFor(occurrence.id)
                ReminderItemUi(
                    occurrenceId = occurrence.id,
                    title = occurrence.title,
                    domain = occurrence.domain,
                    profile = state?.profile ?: ReminderProfile.STANDARD.key,
                    nextFireAt = state?.nextFireAt,
                    audibleFires = state?.audibleFires ?: 0,
                    degraded = state?.degraded.orEmpty()
                )
            }
            _state.value = _state.value.copy(
                loaded = true,
                pausedUntil = policy.pausedUntil,
                quietFrom = formatTime(policy.quietFrom) ?: ReminderRepository.DEFAULT_QUIET_FROM,
                quietTo = formatTime(policy.quietTo) ?: ReminderRepository.DEFAULT_QUIET_TO,
                coreCap = policy.coreDailyCap,
                generalCap = policy.generalDailyCap,
                lessOften = policy.lessOften.map { it.key }.sorted(),
                items = items,
                nextFireAt = repo.soonestFireAt(),
                exactAlarms = scheduler.canScheduleExactAlarms(),
                notificationsAllowed = notificationsAllowed(),
                degradedCount = items.count { it.degraded.isNotEmpty() },
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(loaded = true, error = e.message)
        }
    }

    private fun notificationsAllowed(): Boolean =
        runCatching { NotificationManagerCompat.from(getApplication()).areNotificationsEnabled() }
            .getOrDefault(false)

    // ------------------------------------------------------------- pause

    fun pauseFor(minutes: Int) = mutate("Paused for $minutes min") {
        repo.setPauseUntil(System.currentTimeMillis() + minutes * 60_000L)
    }

    /** Until the next quiet-hours end, which is when reminders would resume anyway. */
    fun pauseUntilTomorrow() = mutate("Paused until tomorrow morning") {
        val zone = ZoneId.systemDefault()
        val policy = repo.policy()
        val tomorrow = LocalDate.now(zone).plusDays(1)
        val resume = tomorrow.atTime(policy.quietTo ?: LocalTime.parse("07:00"))
            .atZone(zone).toInstant().toEpochMilli()
        repo.setPauseUntil(resume)
    }

    fun resumeNow() = mutate("Reminders resumed") {
        repo.setPauseUntil(null)
    }

    // ------------------------------------------------------- quiet hours

    fun saveQuietHours(from: String, to: String) {
        viewModelScope.launch {
            val f = repo.normaliseTime(from)
            val t = repo.normaliseTime(to)
            if (f == null || t == null) {
                _state.value = _state.value.copy(message = "Use HH:MM, for example 22:00")
                return@launch
            }
            mutateNow("Quiet hours $f–$t") { repo.setQuietHours(f, t) }
        }
    }

    // -------------------------------------------------------------- caps

    fun setCoreCap(value: Int) = mutate("Core cap: $value a day") { repo.setCoreCap(value) }

    fun setGeneralCap(value: Int) = mutate("General cap: $value a day") { repo.setGeneralCap(value) }

    // ------------------------------------------------------- less often

    fun toggleLessOften(profile: ReminderProfile) {
        viewModelScope.launch {
            val current = _state.value.lessOften.map { ReminderProfile.fromKey(it) }.toSet()
            val next = if (profile in current) current - profile else current + profile
            mutateNow(
                if (profile in next) "${profile.label} reminders are less often now"
                else "${profile.label} reminders are back to normal"
            ) { repo.setLessOften(next) }
        }
    }

    /** Per-item profile, which is the per-item edit DL12 requires. */
    fun setProfile(occurrenceId: String, profile: ReminderProfile) =
        mutate("${profile.label} cadence for this item") {
            repo.setProfile(occurrenceId, profile)
        }

    // ------------------------------------------------------------ plumbing

    private fun mutate(message: String, block: suspend () -> Unit) {
        viewModelScope.launch { mutateNow(message, block) }
    }

    private suspend fun mutateNow(message: String, block: suspend () -> Unit) {
        try {
            block()
            runCatching { scheduler.syncAll() }
            refresh()
            _state.value = _state.value.copy(message = message)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message)
        }
    }

    private fun formatTime(time: LocalTime?): String? =
        time?.format(DateTimeFormatter.ofPattern("HH:mm"))
}
