package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistGeneration
import io.github.pabi277.dayledger.data.checklist.ChecklistRepository
import io.github.pabi277.dayledger.data.checklist.ChecklistValues
import io.github.pabi277.dayledger.data.checklist.TodayBuckets
import io.github.pabi277.dayledger.data.checklist.TodayItem
import io.github.pabi277.dayledger.data.checklist.TodayRow
import io.github.pabi277.dayledger.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class ChecklistUiState(
    val dateKey: String = LocalDate.now().toString(),
    val items: List<TodayItem> = emptyList(),
    val remaining: Int = 0,
    val fullCount: Int = 0,
    val minimumCount: Int = 0,
    val skippedCount: Int = 0,
    /** Undated captures waiting in the inbox (Phase 9). */
    val inboxCount: Int = 0,
    val lastCaptureId: String? = null,
    val message: String? = null,
    /** The row whose optional-detail sheet is open, if any. */
    val evidenceFor: String? = null,
    /** The occurrence most recently acted on, so its row can offer Undo. */
    val lastActionId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Phase 8 checklist state for Today.
 *
 * Generation runs on every load, which is exactly what makes restart/reboot safe: the
 * insert is conflict-ignoring on a deterministic id, so a second pass adds nothing.
 */
class ChecklistViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChecklistRepository(DayLedgerDatabase.getInstance(app))
    private val db = DayLedgerDatabase.getInstance(app)
    private val _state = MutableStateFlow(ChecklistUiState())
    val state: StateFlow<ChecklistUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch { refresh() }
    }

    private suspend fun refresh() {
        val today = LocalDate.now()
        _state.value = _state.value.copy(dateKey = today.toString(), isLoading = true, error = null)
        try {
            repo.ensureGeneratedFromToday()
            val rows = repo.forDate(today.toString()).map {
                TodayRow(
                    id = it.id,
                    title = it.title,
                    domain = it.domain,
                    status = it.status,
                    windowStartMin = it.windowStartMin,
                    windowEndMin = it.windowEndMin,
                    snoozeUntil = it.snoozeUntil
                )
            }
            val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
            val (full, minimum) = TodayBuckets.completedCounts(rows)
            _state.value = _state.value.copy(
                items = TodayBuckets.bucket(rows, nowMin, System.currentTimeMillis()),
                remaining = TodayBuckets.remaining(rows),
                fullCount = full,
                minimumCount = minimum,
                skippedCount = TodayBuckets.skippedCount(rows),
                inboxCount = db.inboxDao().count(),
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    fun done(id: String) = act(id, ChecklistValues.ACTION_FULL)

    fun minimum(id: String) = act(id, ChecklistValues.ACTION_MINIMUM)

    fun later(id: String) = act(id, ChecklistValues.ACTION_LATER)

    fun skip(id: String) = act(id, ChecklistValues.ACTION_SKIP)

    private fun act(id: String, action: String) {
        viewModelScope.launch {
            try {
                repo.act(id, action)
                // Phase 10: a tap on the screen and a tap on the notification must leave the
                // same state behind, so the schedule is recomputed here too.
                runCatching { ReminderScheduler(getApplication()).syncOne(id) }
                _state.value = _state.value.copy(
                    lastActionId = id,
                    // Done and Minimum offer the optional-detail sheet; Skip and Later do not.
                    evidenceFor = if (action == ChecklistValues.ACTION_FULL ||
                        action == ChecklistValues.ACTION_MINIMUM
                    ) id else _state.value.evidenceFor
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
            refresh()
        }
    }

    fun undo(id: String) {
        viewModelScope.launch {
            try {
                repo.undo(id)
                runCatching { ReminderScheduler(getApplication()).syncOne(id) }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
            _state.value = _state.value.copy(evidenceFor = null)
            refresh()
        }
    }

    fun dismissEvidence() {
        // Dismissing keeps the completion: detail is always optional.
        _state.value = _state.value.copy(evidenceFor = null)
    }

    fun saveEvidence(occurrenceId: String, minutes: Int?, topic: String, note: String) {
        viewModelScope.launch {
            try {
                repo.attachEvidence(occurrenceId, minutes = minutes, topic = topic, note = note)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
            _state.value = _state.value.copy(evidenceFor = null)
            refresh()
        }
    }

    /**
     * Phase 9: capture is title-first and lands in the Inbox, not on today.
     *
     * Phase 8 put the capture straight onto today's list, which quietly invented a deadline
     * for something the owner had not scheduled. An undated capture now waits in the inbox
     * until it is given a date, and the confirmation can be undone in one tap.
     */
    fun quickCapture(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            try {
                val id = repo.capture(clean)
                _state.value = _state.value.copy(
                    lastCaptureId = id,
                    message = "Captured to Inbox"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
            refresh()
        }
    }

    fun undoCapture() {
        val id = _state.value.lastCaptureId ?: return
        viewModelScope.launch {
            try {
                repo.undoCapture(id)
                _state.value = _state.value.copy(lastCaptureId = null, message = "Capture undone")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
            refresh()
        }
    }
}
