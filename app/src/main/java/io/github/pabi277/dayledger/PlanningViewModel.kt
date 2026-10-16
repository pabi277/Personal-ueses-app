package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.BlockEntity
import io.github.pabi277.dayledger.data.PlanningRepository
import io.github.pabi277.dayledger.data.TemplateEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanningUiState(
    val dateKey: String = "",
    val blocks: List<BlockEntity> = emptyList(),
    val templates: List<TemplateEntity> = emptyList(),
    val isLoading: Boolean = false
)

class PlanningViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlanningRepository(app)
    private val _state = MutableStateFlow(PlanningUiState())
    val state: StateFlow<PlanningUiState> = _state.asStateFlow()

    fun load(dateKey: String = "") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.seedTemplatesIfNeeded()
            val dk = if (dateKey.isEmpty()) repo.todayKey() else dateKey
            _state.value = PlanningUiState(
                dateKey = dk,
                blocks = repo.blocksForDate(dk),
                templates = repo.templates(),
                isLoading = false
            )
        }
    }

    fun applyTemplate(templateId: String) {
        viewModelScope.launch {
            val dk = _state.value.dateKey
            repo.applyTemplate(dk, templateId)
            load(dk)
        }
    }

    fun addBlock(startMin: Int, endMin: Int, title: String) {
        viewModelScope.launch {
            val dk = _state.value.dateKey
            repo.addBlock(dk, startMin, endMin, title)
            load(dk)
        }
    }

    fun shiftDay(minutes: Int) {
        viewModelScope.launch {
            repo.shiftDay(_state.value.dateKey, minutes)
            load(_state.value.dateKey)
        }
    }

    fun deleteBlock(id: Long) {
        viewModelScope.launch {
            repo.deleteBlock(id)
            load(_state.value.dateKey)
        }
    }
}
