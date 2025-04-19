
package com.example.fizyoapp.presentation.user.ornekegzersizler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.GetExerciseCategoriesUseCase
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.PopulateDatabaseUseCase
import com.example.fizyoapp.presentation.user.ornekegzersizler.ExercisesExamplesEvent
import com.example.fizyoapp.presentation.user.ornekegzersizler.ExercisesExamplesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    init {
        viewModelScope.launch {
            populateDatabaseUseCase()
            loadCategories()
        }
    }

    fun onEvent(event: ExercisesExamplesEvent) {
        when (event) {
            is ExercisesExamplesEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategoryId = event.category.id)
            }}
            is ExercisesExamplesEvent.LoadCategories -> {
                loadCategories()
            }
            is ExercisesExamplesEvent.CategoryNavigationHandled -> {
                // Navigasyon gerçekleştikten sonra seçilen kategori ID'sini temizliyoruz
                _state.update { it.copy(selectedCategoryId = null) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getExerciseCategoriesUseCase().collect { categories ->
                    _state.update { it.copy(categories = categories, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}