package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeeklyReportUiState(
    val text: String = "",
    val isLoading: Boolean = false
)

class WeeklyReportViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DayLedgerDatabase.getInstance(app)
    private val usageRepo = UsageRepository(app)
    private val _state = MutableStateFlow(WeeklyReportUiState())
    val state: StateFlow<WeeklyReportUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val sb = StringBuilder()
            var totalMin = 0
            var totalPaise = 0
            var learnMin = 0
            val overrides = db.limitEventDao().forDate(LocalDate.now().toString()).count { it.overrideType != "none" }
            for (i in 0..6) {
                val d = LocalDate.now().minusDays(i.toLong()).toString()
                totalMin += usageRepo.screenMinutesForDate(d)
                totalPaise += db.earningDao().totalForDate(d)
                learnMin += db.learningDao().forDate(d).sumOf { it.minutes }
            }
            sb.append("Last 7 days\n")
            sb.append("Total screen time: ${totalMin / 60}h ${totalMin % 60}m\n")
            sb.append("Earnings: ₹${totalPaise / 100}.${"%02d".format(totalPaise % 100)}\n")
            sb.append("Learning minutes: $learnMin\n")
            sb.append("Overrides today: $overrides\n")
            sb.append("Suggestion: protect your top waste hour next week.\n")
            _state.value = WeeklyReportUiState(text = sb.toString(), isLoading = false)
        }
    }
}
