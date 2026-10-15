package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LimitsUiState(
    val apps: List<AppInfoEntity> = emptyList(),
    val isLoading: Boolean = false
)

class LimitsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UsageRepository(app)
    private val _state = MutableStateFlow(LimitsUiState())
    val state: StateFlow<LimitsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            _state.value = LimitsUiState(apps = repo.appInfoAll(), isLoading = false)
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
