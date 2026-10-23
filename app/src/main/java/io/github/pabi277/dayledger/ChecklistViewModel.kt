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

    fun quickCapture(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            try {
                repo.addTask(clean, LocalDate.now())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
            refresh()
        }
    }
}
