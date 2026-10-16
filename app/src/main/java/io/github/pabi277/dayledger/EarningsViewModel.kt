package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.EarningEntryEntity
import io.github.pabi277.dayledger.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EarningsUiState(
    val entries: List<EarningEntryEntity> = emptyList(),
    val totalPaiseToday: Int = 0,
    val isLoading: Boolean = false
)

class EarningsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DayLedgerDatabase.getInstance(app)
    private val repo = UsageRepository(app)
    private val _state = MutableStateFlow(EarningsUiState())
    val state: StateFlow<EarningsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val entries = db.earningDao().all()
            val today = LocalDate.now().toString()
            val total = db.earningDao().totalForDate(today)
            // attribute minutes
            val attributed = entries.map { e ->
                if (e.attributedMinutes == 0 && e.appId != null) {
                    val mins = db.usageDao().forDate(e.dateKey).firstOrNull { it.packageName == e.appId }?.foregroundMinutes ?: 0
                    e.copy(attributedMinutes = mins)
                } else e
            }
            _state.value = EarningsUiState(entries = attributed, totalPaiseToday = total, isLoading = false)
        }
    }

    fun logEarning(source: String, taskType: String, appId: String?, amountPaise: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val mins = if (appId != null) db.usageDao().forDate(today).firstOrNull { it.packageName == appId }?.foregroundMinutes ?: 0 else 0
            db.earningDao().insert(EarningEntryEntity(dateKey = today, source = source, taskType = taskType, appId = appId, amountPaise = amountPaise, attributedMinutes = mins))
            load()
        }
    }
}
