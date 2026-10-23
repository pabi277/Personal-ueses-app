package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.data.CategoryEntity
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LimitsUiState(
    val apps: List<AppInfoEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    /** packageName -> screen-on minutes stored for today. */
    val minutesToday: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val message: String? = null
)

class LimitsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UsageRepository(app)
    private val _state = MutableStateFlow(LimitsUiState())
    val state: StateFlow<LimitsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val today = java.time.LocalDate.now().toString()
            val minutes = repo.usageForDate(today).associate { it.packageName to it.foregroundMinutes }
            _state.value = LimitsUiState(
                apps = repo.appInfoAll(),
                categories = repo.categoriesAll(),
                minutesToday = minutes,
                isLoading = false
            )
        }
    }

    /**
     * F2 write path. `null` clears the per-app limit so the category budget applies again.
     * A non-positive number is treated as "clear", never as "allow zero minutes".
     */
    fun setPerAppLimit(pkg: String, minutes: Int?) {
        viewModelScope.launch {
            val clean = minutes?.takeIf { it > 0 }
            val db = DayLedgerDatabase.getInstance(getApplication())
            db.appInfoDao().setPerAppLimit(pkg, clean)
            _state.value = _state.value.copy(
                message = if (clean == null) {
                    "Per-app limit cleared — the category budget applies"
                } else {
                    "Per-app limit set to ${clean}m — it overrides the category budget"
                }
            )
            load()
        }
    }

    fun toggleAlwaysAllowed(pkg: String, allowed: Boolean) {
        viewModelScope.launch {
            val db = io.github.pabi277.dayledger.data.DayLedgerDatabase.getInstance(getApplication())
            db.appInfoDao().upsertAll(listOf(
                _state.value.apps.first { it.packageName == pkg }.copy(alwaysAllowed = allowed)
            ))
            load()
        }
    }
}
