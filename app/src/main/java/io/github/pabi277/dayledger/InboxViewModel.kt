package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.checklist.ChecklistRepository
import io.github.pabi277.dayledger.data.checklist.InboxItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class InboxUiState(
    val items: List<InboxItemEntity> = emptyList(),
    val message: String? = null,
    val justCapturedId: String? = null
)

/**
 * Phase 9 part 9.1 — title-first capture.
 *
 * A capture is saved the moment the title is submitted, with no classification step. What
 * the inbox holds is deliberately *not* a checklist item: an undated capture has no
 * deadline, so nothing here can invent one.
 */
class InboxViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChecklistRepository(DayLedgerDatabase.getInstance(app))
    private val _state = MutableStateFlow(InboxUiState())
    val state: StateFlow<InboxUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(items = repo.inbox())
        }
    }

    fun capture(title: String, domain: String = "", note: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val id = repo.capture(title, domain, note)
            _state.value = _state.value.copy(justCapturedId = id, message = "Captured")
            load()
        }
    }

    fun undoLastCapture() {
        val id = _state.value.justCapturedId ?: return
        viewModelScope.launch {
            repo.undoCapture(id)
            _state.value = _state.value.copy(justCapturedId = null, message = "Capture undone")
            load()
        }
    }

    fun schedule(id: String, target: LocalDate) {
        viewModelScope.launch {
            repo.scheduleInboxItem(id, target)
            _state.value = _state.value.copy(message = "Scheduled for $target")
            load()
        }
    }

    fun scheduleToday(id: String) = schedule(id, LocalDate.now())

    fun scheduleTomorrow(id: String) = schedule(id, LocalDate.now().plusDays(1))

    fun scheduleOn(id: String, dateText: String): Boolean {
        val parsed = runCatching { LocalDate.parse(dateText.trim()) }.getOrNull() ?: return false
        schedule(id, parsed)
        return true
    }

    fun discard(id: String) {
        viewModelScope.launch {
            repo.discardInboxItem(id)
            load()
        }
    }
}
