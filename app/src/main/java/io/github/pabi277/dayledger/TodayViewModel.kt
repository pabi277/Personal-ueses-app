package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.data.AppUsageEntity
import io.github.pabi277.dayledger.data.CategoryEntity
import io.github.pabi277.dayledger.data.HourlyUsageEntity
import io.github.pabi277.dayledger.data.UsageRepository
import io.github.pabi277.dayledger.notifications.NotificationRepository
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


    private suspend fun fireWasteThresholdIfNeeded() {
        try {
            val nrepo = NotificationRepository(getApplication())
            val rules = nrepo.rules()
            val rule = rules.firstOrNull { it.id == "waste_threshold" } ?: return
            val today = java.time.LocalDate.now().toString()
            val wasteMins = _state.value.apps.filter {
                _state.value.appInfo[it.packageName]?.category == "Social/Waste"
            }.sumOf { it.foregroundMinutes }
            val wasteBudget = _state.value.categories.firstOrNull { it.name == "Social/Waste" }?.budgetMin ?: 45
            if (wasteMins > wasteBudget) {
                val text = rule.templateText
                    .replace("{waste}", wasteMins.toString())
                    .replace("{total}", _state.value.totalMinutes.toString())
                nrepo.fireIfAllowed(rule, text)
            }
        } catch (e: Exception) {
            // ignore notification failures
        }
    }

    fun loadToday() {
        viewModelScope.launch { loadTodaySync() }
    }

    private suspend fun loadTodaySync() {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val today = LocalDate.now().toString()
                // Screen-on foreground time, with the Digital Wellbeing exclusions
                // applied, and a total that is exactly the sum of the listed rows.
                val apps = repo.screenUsageForDate(today)
                val info = repo.appInfoAll().associateBy { it.packageName }
                val cats = repo.categoriesAll()
                val hourlyEntities = repo.hourlyForDate(today)
                val hourly = IntArray(24)
                for (h in hourlyEntities) if (h.hour in 0..23) hourly[h.hour] = h.minutes
                val total = apps.sumOf { it.foregroundMinutes }
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

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                if (repo.hasUsagePermission()) {
                    repo.ingestRecentDays(7)
                }
                refreshPermission()
                loadTodaySync()
                if (repo.hasUsagePermission()) {
                    fireWasteThresholdIfNeeded()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
