package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistRepository
import io.github.pabi277.dayledger.data.checklist.ChecklistTaskEntity
import io.github.pabi277.dayledger.data.checklist.HabitRuleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitsUiState(
    val habits: List<HabitRuleEntity> = emptyList(),
    val tasks: List<ChecklistTaskEntity> = emptyList(),
    val error: String? = null
)

/**
 * Habits — the editable side of Phase 8 part 8.4.
 *
 * Only generic, empty starting content: nothing here seeds a personal routine. A habit is
 * created by the owner; the weekday picker decides which dates generate.
 */
class HabitsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChecklistRepository(DayLedgerDatabase.getInstance(app))
    private val _state = MutableStateFlow(HabitsUiState())
    val state: StateFlow<HabitsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = HabitsUiState(habits = repo.habits(), tasks = repo.tasks())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    /**
     * @param weekdayCsv ISO weekdays 1..7, used only for weekdays/weekly cadence.
     */
    fun addHabit(
        title: String,
        cadence: String,
        weekdayCsv: String,
        windowStartMin: Int?,
        windowEndMin: Int?,
        minimumLabel: String?
    ) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            val rule = HabitRuleEntity(
                id = "habit-" + System.currentTimeMillis().toString(36) + "-" + (0..999).random(),
                title = clean,
                cadence = cadence,
                weekdays = weekdayCsv,
                windowStartMin = windowStartMin,
                windowEndMin = windowEndMin,
                minimumLabel = minimumLabel?.trim()?.takeIf { it.isNotEmpty() }
            )
            try {
                repo.upsertHabit(rule)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun pause(id: String, paused: Boolean) {
        viewModelScope.launch {
            try {
                repo.setHabitPaused(id, paused)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun archive(id: String) {
        viewModelScope.launch {
            try {
                repo.archiveHabit(id)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun addTask(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            try {
                repo.addTask(clean, LocalDate.now())
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
