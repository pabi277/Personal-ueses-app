package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.data.AppUsageEntity
import io.github.pabi277.dayledger.data.CategoryEntity
import io.github.pabi277.dayledger.data.HourlyUsageEntity
import io.github.pabi277.dayledger.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TodayUiState(
    val hasUsagePermission: Boolean = false,
    val totalMinutes: Int = 0,
    val apps: List<AppUsageEntity> = emptyList(),
    val appInfo: Map<String, AppInfoEntity> = emptyMap(),
    val categories: List<CategoryEntity> = emptyList(),
    val hourly: List<Int> = List(24) { 0 },
    val asOf: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class TodayViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UsageRepository(app)
    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        refreshPermission()
    }

    fun refreshPermission() {
        _state.value = _state.value.copy(hasUsagePermission = repo.hasUsagePermission())
    }

    fun loadToday() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val today = LocalDate.now().toString()
                val apps = repo.usageForDate(today)
                val info = repo.appInfoAll().associateBy { it.packageName }
                val cats = repo.categoriesAll()
                val hourlyEntities = repo.hourlyForDate(today)
                val hourly = IntArray(24)
                for (h in hourlyEntities) if (h.hour in 0..23) hourly[h.hour] = h.minutes
                val total = repo.totalForDate(today)
                _state.value = _state.value.copy(
                    totalMinutes = total,
                    apps = apps,
                    appInfo = info,
                    categories = cats,
                    hourly = hourly.toList(),
                    asOf = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                if (repo.hasUsagePermission()) {
                    repo.ingestRecentDays(7)
                }
                refreshPermission()
                loadToday()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
