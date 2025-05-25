package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.exercisemanagement

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisesexample.ExerciseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(CategoryExercisesState())
    val state: StateFlow<CategoryExercisesState> = _state.asStateFlow()

    private val categoryId: String = savedStateHandle["categoryId"] ?: ""
    private val categoryName: String = savedStateHandle["categoryName"] ?: "Egzersizler"

    init {
        if (categoryId.isNotEmpty()) {
            _state.update { it.copy(
                category = ExerciseCategory(
                    id = categoryId,
                    name = categoryName
                )
            )}
            loadExercises()
        } else {
            _state.update { it.copy(error = "Kategori bulunamadı") }
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Burası repository'de eksik olan metod yerine mevcut bir metod kullanarak çözüldü
                val physiotherapistId = "" // Gerçek uygulamada kullanıcı ID'sini almalısınız

                exerciseRepository.getExercisesByPhysiotherapist(physiotherapistId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Kategoriye göre filtreleme
                            val filteredExercises = result.data.filter { it.category == categoryName }

                            Log.d("CategoryExercisesVM", "Loaded ${filteredExercises.size} exercises")
                            _state.update { it.copy(
                                exercises = filteredExercises,
                                isLoading = false
                            ) }
                        }
                        is Resource.Error -> {
                            Log.e("CategoryExercisesVM", "Error loading exercises: ${result.message}")
                            _state.update { it.copy(
                                error = result.message,
                                isLoading = false
                            ) }
                        }
                        is Resource.Loading -> {
                            _state.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CategoryExercisesVM", "Exception in loadExercises", e)
                _state.update { it.copy(
                    error = "Beklenmeyen bir hata oluştu: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}

data class CategoryExercisesState(
    val category: ExerciseCategory? = null,
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)