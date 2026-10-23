package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistRepository
import io.github.pabi277.dayledger.data.checklist.Leftover
import io.github.pabi277.dayledger.data.checklist.LeftoverDecision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayCloseUiState(
    val dateKey: String = LocalDate.now().toString(),
    val leftovers: List<Leftover> = emptyList(),
    /** Used by Move and Schedule. Defaults to tomorrow, which is the common case. */
    val targetDate: String = LocalDate.now().plusDays(1).toString(),
    val message: String? = null,
    val isLoading: Boolean = false
)

/**
 * Phase 9 part 9.4 — an explicit decision for every unfinished item.
 *
 * Nothing on this screen happens automatically: an item leaves the list only because the
 * owner picked Move, Schedule, Shrink to Minimum or Drop. That is the whole point — the
 * old behaviour of letting a day roll over silently is what DL9 forbids.
 */
class DayCloseViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChecklistRepository(DayLedgerDatabase.getInstance(app))
    private val _state = MutableStateFlow(DayCloseUiState())
    val state: StateFlow<DayCloseUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val key = today.toString()
            _state.value = _state.value.copy(dateKey = key, isLoading = true)
            try {
                _state.value = _state.value.copy(
                    leftovers = repo.leftovers(key),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, message = e.message)
            }
        }
    }

    fun setTargetDate(text: String) {
        _state.value = _state.value.copy(targetDate = text)
    }

    fun decide(item: Leftover, decision: LeftoverDecision) {
        viewModelScope.launch {
            val target = runCatching { LocalDate.parse(_state.value.targetDate.trim()) }.getOrNull()
            if ((decision == LeftoverDecision.MOVE || decision == LeftoverDecision.SCHEDULE) &&
                target == null
            ) {
                _state.value = _state.value.copy(
                    message = "Pick a date as yyyy-MM-dd first."
                )
                return@launch
            }
            try {
                repo.decide(item.id, decision, target)
                _state.value = _state.value.copy(
                    message = when (decision) {
                        LeftoverDecision.MOVE -> "Moved to $target"
                        LeftoverDecision.SCHEDULE -> "Scheduled for $target"
                        LeftoverDecision.SHRINK_TO_MINIMUM -> "Counted as Minimum"
                        LeftoverDecision.DROP -> "Dropped"
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = e.message)
            }
            load()
        }
    }
}
