package com.example.fizyoapp.presentation.user.egzersizlerim

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercise.ExercisePlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserExercisePlansViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserExercisePlansState())
    val state: StateFlow<UserExercisePlansState> = _state

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.user?.let { user ->
                            Log.d("UserExercisePlansVM", "Kullanıcı yüklendi: ${user.id}")
                            loadExercisePlans(user.id)
                        }
                    }
                    is Resource.Error -> {
                        Log.e("UserExercisePlansVM", "Kullanıcı yüklenirken hata: ${result.message}")
                        _state.update { it.copy(
                            isLoading = false,
                            error = result.message ?: "Kullanıcı bilgileri alınamadı"
                        ) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadExercisePlans(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            Log.d("UserExercisePlansVM", "Egzersiz planları yükleniyor: $userId")

            // Fizyoterapistin hastaya atadığı planları çek
            exerciseRepository.getExercisePlansByPatient(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val plans = result.data
                        Log.d("UserExercisePlansVM", "${plans.size} adet plan bulundu")

                        // Planları tarihe göre sırala (en yeni plan en üstte)
                        val sortedPlans = plans.sortedByDescending { it.createdAt }

                        _state.update { it.copy(
                            isLoading = false,
                            exercisePlans = sortedPlans,
                            error = null
                        ) }
                    }
                    is Resource.Error -> {
                        Log.e("UserExercisePlansVM", "Planlar yüklenirken hata: ${result.message}")
                        _state.update { it.copy(
                            isLoading = false,
                            error = result.message ?: "Egzersiz planları yüklenemedi"
                        ) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    // Planı yenileme işlevi - gerekirse kullanılabilir
    fun refreshPlans() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    result.data.user?.let { user ->
                        loadExercisePlans(user.id)
                    }
                }
            }
        }
    }
}

data class UserExercisePlansState(
    val exercisePlans: List<ExercisePlan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)