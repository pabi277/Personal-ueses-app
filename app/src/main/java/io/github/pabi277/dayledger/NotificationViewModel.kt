package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.NotificationRuleEntity
import io.github.pabi277.dayledger.data.UsageRepository
import io.github.pabi277.dayledger.notifications.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val rules: List<NotificationRuleEntity> = emptyList(),
    val dailyCap: Int = 6,
    val isLoading: Boolean = false
)

class NotificationViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = NotificationRepository(app)
    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.seedAndSchedule()
            load()
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            _state.value = NotificationUiState(
                rules = repo.rules(),
                dailyCap = repo.getDailyCap(),
                isLoading = false
            )
        }
    }

    fun toggle(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repo.setEnabled(id, enabled)
            load()
        }
    }

    fun updateText(id: String, text: String) {
        viewModelScope.launch {
            repo.updateText(id, text)
            load()
        }
    }

    fun updateSchedule(id: String, times: String, qf: String, qt: String) {
        viewModelScope.launch {
            repo.updateSchedule(id, times, qf, qt)
            load()
        }
    }
}
