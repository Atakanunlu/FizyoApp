package com.example.fizyoapp.presentation.user.egzersizlerim

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisePlanDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisePlanDetailState())
    val state: StateFlow<ExercisePlanDetailState> = _state

    private val planId: String = savedStateHandle.get<String>("planId") ?: ""

    init {
        if (planId.isNotEmpty()) {
            loadExercisePlan(planId)
        }
    }

    fun loadExercisePlan(planId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            Log.d("ExercisePlanDetailVM", "Loading plan: $planId")

            exerciseRepository.getExercisePlanById(planId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val plan = result.data
                        if (plan != null) {
                            Log.d("ExercisePlanDetailVM", "Plan loaded: ${plan.title}, exercises: ${plan.exercises.size}")
                            _state.update {
                                it.copy(
                                    plan = plan,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        } else {
                            Log.e("ExercisePlanDetailVM", "Plan not found")
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Plan bulunamadı"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Log.e("ExercisePlanDetailVM", "Error loading plan: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Plan yüklenirken bir hata oluştu"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}

data class ExercisePlanDetailState(
    val plan: ExercisePlan? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)