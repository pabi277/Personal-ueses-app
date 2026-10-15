package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.LearningEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class LearningUiState(
    val entries: List<LearningEntryEntity> = emptyList(),
    val streak: Int = 0,
    val isLoading: Boolean = false
)

class LearningViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DayLedgerDatabase.getInstance(app)
    private val _state = MutableStateFlow(LearningUiState())
    val state: StateFlow<LearningUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val entries = db.learningDao().all()
            val days = db.learningDao().daysWithLearning()
            var streak = 0
            var cursor = LocalDate.now()
            while (days.contains(cursor.toString())) {
                streak++
                cursor = cursor.minusDays(1)
            }
            _state.value = LearningUiState(entries = entries, streak = streak, isLoading = false)
        }
    }

    fun logLearning(minutes: Int, text: String) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            db.learningDao().insert(LearningEntryEntity(dateKey = today, minutes = minutes, text = text))
            load()
        }
    }
}
