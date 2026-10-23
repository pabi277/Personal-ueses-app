package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.BlockEntity
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistGeneration
import io.github.pabi277.dayledger.data.checklist.ChecklistOccurrenceEntity
import io.github.pabi277.dayledger.data.checklist.ChecklistRepository
import io.github.pabi277.dayledger.data.checklist.ChecklistValues
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
    /** templateId -> number of blocks it would apply. Shown so an empty template is obvious. */
    val templateItemCounts: Map<String, Int> = emptyMap(),
    /** Phase 9: dated checklist items for this date (habit, task and carried rows). */
    val checklistItems: List<ChecklistOccurrenceEntity> = emptyList(),
    val top3: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class PlanningViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlanningRepository(app)
    private val checklist = ChecklistRepository(DayLedgerDatabase.getInstance(app))
    private val _state = MutableStateFlow(PlanningUiState())
    val state: StateFlow<PlanningUiState> = _state.asStateFlow()

    fun load(dateKey: String = "") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.seedTemplatesIfNeeded()
            val dk = if (dateKey.isEmpty()) repo.todayKey() else dateKey
            val date = runCatching { java.time.LocalDate.parse(dk) }.getOrNull()
            if (date != null) checklist.ensureGenerated(listOf(date))
            _state.value = PlanningUiState(
                dateKey = dk,
                blocks = repo.blocksForDate(dk),
                templates = repo.templates(),
                templateItemCounts = repo.templateItemCounts(),
                checklistItems = checklist.itemsForDate(dk),
                top3 = checklist.top3(dk),
                isLoading = false
            )
        }
    }

    /** Phase 9: Top 3 is per date and editable; a fourth add is simply refused. */
    fun addToTop3(occurrenceId: String) {
        viewModelScope.launch {
            checklist.addToTop3(_state.value.dateKey, occurrenceId)
            load(_state.value.dateKey)
        }
    }

    fun removeFromTop3(occurrenceId: String) {
        viewModelScope.launch {
            checklist.removeFromTop3(_state.value.dateKey, occurrenceId)
            load(_state.value.dateKey)
        }
    }

    /** Owner-initiated removal of a template and its blocks. */
    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            repo.deleteTemplate(templateId)
            load(_state.value.dateKey)
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
