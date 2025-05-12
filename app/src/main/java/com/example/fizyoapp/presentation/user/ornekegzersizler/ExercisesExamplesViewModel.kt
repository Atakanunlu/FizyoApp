package com.example.fizyoapp.presentation.user.ornekegzersizler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.GetExerciseCategoriesUseCase
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.PopulateDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesExamplesViewModel @Inject constructor(
    private val getExerciseCategoriesUseCase: GetExerciseCategoriesUseCase,
    private val populateDatabaseUseCase: PopulateDatabaseUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ExercisesExamplesState())
    val state: StateFlow<ExercisesExamplesState> = _state.asStateFlow()
    private var categoriesJob: Job? = null

    init {
        viewModelScope.launch {
            populateDatabaseUseCase()
            startCollectingCategories()
        }
    }

    fun onEvent(event: ExercisesExamplesEvent) {
        when (event) {
            is ExercisesExamplesEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategoryId = event.category.id) }
            }
            is ExercisesExamplesEvent.LoadCategories -> {
                startCollectingCategories()
            }
            is ExercisesExamplesEvent.CategoryNavigationHandled -> {
                _state.update { it.copy(selectedCategoryId = null) }
            }
        }
    }

    private fun startCollectingCategories() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getExerciseCategoriesUseCase()
                .catch { e ->
                    _state.update {
                        it.copy(
                            error = "Veri akışı hatası: ${e.message}",
                            isLoading = false
                        )
                    }
                }
                .collect { categories ->
                    _state.update {
                        it.copy(
                            categories = categories,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        categoriesJob?.cancel()
        super.onCleared()
    }
}