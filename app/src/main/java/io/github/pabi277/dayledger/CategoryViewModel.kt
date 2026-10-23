package io.github.pabi277.dayledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pabi277.dayledger.data.AppCategorySuggestion
import io.github.pabi277.dayledger.data.AppInfoEntity
import io.github.pabi277.dayledger.data.CategoryEntity
import io.github.pabi277.dayledger.data.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val apps: List<AppInfoEntity> = emptyList(),
    /** Unclassified apps with a defensible suggestion. Never applied without a tap. */
    val suggestions: List<AppCategorySuggestion> = emptyList(),
    val isLoading: Boolean = false
)

class CategoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UsageRepository(app)
    private val _state = MutableStateFlow(CategoryUiState())
    val state: StateFlow<CategoryUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.seedCategoriesIfNeeded()
            _state.value = CategoryUiState(
                categories = repo.categoriesAll(),
                apps = repo.appInfoAll(),
                suggestions = repo.pendingCategorySuggestions(),
                isLoading = false
            )
        }
    }

    fun updateBudget(id: String, budgetMin: Int, kind: String) {
        viewModelScope.launch {
            repo.updateCategory(id, budgetMin, kind)
            load()
        }
    }

    fun assignApp(packageName: String, category: String) {
        viewModelScope.launch {
            repo.assignAppCategory(packageName, category)
            load()
        }
    }

    /** One tap accepts a suggestion. The write path is the ordinary assignment path. */
    fun acceptSuggestion(suggestion: AppCategorySuggestion) {
        viewModelScope.launch {
            repo.acceptCategorySuggestion(suggestion)
            load()
        }
    }

    fun dismissSuggestion(suggestion: AppCategorySuggestion) {
        viewModelScope.launch {
            repo.dismissCategorySuggestion(suggestion.packageName)
            load()
        }
    }
}
