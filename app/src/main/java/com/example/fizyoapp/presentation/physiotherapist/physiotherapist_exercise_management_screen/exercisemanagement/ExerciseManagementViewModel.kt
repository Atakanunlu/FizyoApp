// presentation/physiotherapist/exercise/ExerciseManagementViewModel.kt
package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.exercisemanagement

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.exercise.Exercise
import com.example.fizyoapp.domain.model.exercise.ExerciseDifficulty
import com.example.fizyoapp.domain.model.exercise.ExercisePlan
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ExerciseManagementViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ExerciseManagementState())
    val state: StateFlow<ExerciseManagementState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentUser: User? = null

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        currentUser = result.data
                        if (currentUser != null) {
                            loadExercises()
                            loadExercisePlans()
                        } else {
                            _state.value = _state.value.copy(
                                errorMessage = "Kullanıcı bilgisi alınamadı"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            errorMessage = result.message ?: "Kullanıcı bilgisi alınamadı"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: ExerciseManagementEvent) {
        when (event) {
            is ExerciseManagementEvent.LoadExercises -> loadExercises()
            is ExerciseManagementEvent.RefreshExercises -> loadExercises()
            is ExerciseManagementEvent.LoadExercisePlans -> loadExercisePlans()
            is ExerciseManagementEvent.RefreshExercisePlans -> loadExercisePlans()
            is ExerciseManagementEvent.FilterByCategory -> filterExercisesByCategory(event.category)
            is ExerciseManagementEvent.FilterByDifficulty -> filterExercisesByDifficulty(event.difficulty)
            is ExerciseManagementEvent.AddExercise -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToAddExercise)
                }
            }
            is ExerciseManagementEvent.CreateExercisePlan -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToCreatePlan)
                }
            }
            is ExerciseManagementEvent.BrowseExerciseCategories -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToExerciseCategories)
                }
            }
            is ExerciseManagementEvent.DeleteExercise -> {
                deleteExercise(event.exerciseId)
            }
            is ExerciseManagementEvent.DeleteExercisePlan -> {
                deleteExercisePlan(event.planId)
            }
            is ExerciseManagementEvent.ClearActionSuccess -> {
                _state.value = _state.value.copy(actionSuccess = null)
            }

            else -> {}
        }
    }

    private fun loadExercises() {
        val userId = currentUser?.id ?: return
        viewModelScope.launch {
            exerciseRepository.getExercisesByPhysiotherapist(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val loadedExercises = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            exercises = loadedExercises,
                            isLoading = false,
                            errorMessage = null
                        )
                        filterExercisesByCategory(_state.value.selectedCategory)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            errorMessage = result.message ?: "Egzersizler yüklenemedi",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    private fun loadExercisePlans() {
        val userId = currentUser?.id ?: return
        Log.d("ExerciseManagementVM", "Loading exercise plans for user: $userId")
        viewModelScope.launch {
            exerciseRepository.getExercisePlansByPhysiotherapist(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val plans = result.data ?: emptyList()
                        Log.d("ExerciseManagementVM", "Loaded ${plans.size} exercise plans: ${plans.map { it.id }}")
                        _state.update { it.copy(
                            exercisePlans = plans,
                            isLoading = false,
                            errorMessage = null
                        )}

                        // Planlar yüklendikten sonra hasta isimlerini yükle
                        loadPatientNames(plans)
                    }
                    is Resource.Error -> {
                        Log.e("ExerciseManagementVM", "Error loading exercise plans: ${result.message}")
                        _state.update { it.copy(
                            errorMessage = result.message ?: "Egzersiz planları yüklenemedi",
                            isLoading = false
                        )}
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    // Hasta isimlerini yükleme
    private fun loadPatientNames(plans: List<ExercisePlan>) {
        viewModelScope.launch {
            val patientIds = plans.map { it.patientId }.distinct()
            if (patientIds.isEmpty()) return@launch

            val patientNamesMap = mutableMapOf<String, String>()

            for (patientId in patientIds) {
                // Önce kullanıcı profilini almaya çalışalım
                userProfileRepository.getUserProfile(patientId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val profile = result.data
                            if (profile != null && profile.firstName.isNotEmpty() && profile.lastName.isNotEmpty()) {
                                patientNamesMap[patientId] = "${profile.firstName} ${profile.lastName}"
                            } else {
                                // Profil yoksa veya isim bilgileri yoksa e-posta adresini gösterelim
                                fallbackToEmailName(patientId, patientNamesMap)
                            }
                        }
                        else -> {
                            // Profil getirilemezse e-posta adresini gösterelim
                            fallbackToEmailName(patientId, patientNamesMap)
                        }
                    }
                }
            }

            _state.update { it.copy(patientNames = patientNamesMap) }
        }
    }

    // E-posta adresini alma yedek fonksiyonu
    private suspend fun fallbackToEmailName(patientId: String, patientNamesMap: MutableMap<String, String>) {
        // Firestore'dan doğrudan bilgileri alalım
        try {
            val userDoc = FirebaseFirestore.getInstance().collection("user").document(patientId).get().await()
            if (userDoc.exists()) {
                val email = userDoc.getString("email") ?: "Kullanıcı"
                patientNamesMap[patientId] = email
            } else {
                patientNamesMap[patientId] = "Kullanıcı #${patientId.takeLast(5)}"
            }
        } catch (e: Exception) {
            patientNamesMap[patientId] = "Kullanıcı #${patientId.takeLast(5)}"
        }
    }

    private fun filterExercisesByCategory(category: String) {
        _state.value = _state.value.copy(
            selectedCategory = category
        )
        // Tüm filtreleri uygula
        applyAllFilters()
    }

    private fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            exerciseRepository.deleteExercise(exerciseId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Egzersizi listeden kaldır
                        val updatedExercises = _state.value.exercises.filter { it.id != exerciseId }
                        // Filtrelenmiş listeyi de güncelle
                        val updatedFilteredExercises =
                            if (_state.value.selectedCategory.isNotEmpty()) {
                                updatedExercises.filter { it.category == _state.value.selectedCategory }
                            } else {
                                emptyList()
                            }
                        _state.value = _state.value.copy(
                            exercises = updatedExercises,
                            filteredExercises = updatedFilteredExercises,
                            isLoading = false,
                            actionSuccess = "Egzersiz başarıyla silindi"
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Egzersiz silinemedi"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun filterExercisesByDifficulty(difficulty: ExerciseDifficulty?) {
        _state.value = _state.value.copy(
            selectedDifficulty = difficulty
        )
        // Tüm filtreleri uygula
        applyAllFilters()
    }

    // Tüm filtreleri birlikte uygulayan metot
    private fun applyAllFilters() {
        // Başlangıçta tüm egzersizleri al
        var filtered = _state.value.exercises
        // Kategori filtresi varsa uygula
        if (_state.value.selectedCategory.isNotEmpty()) {
            filtered = filtered.filter { it.category == _state.value.selectedCategory }
        }
        // Zorluk seviyesi filtresi varsa uygula
        if (_state.value.selectedDifficulty != null) {
            filtered = filtered.filter { it.difficulty == _state.value.selectedDifficulty }
        }
        // Zorluk seviyesine göre sırala (kolaydan zora)
        filtered = filtered.sortedBy { it.difficulty.ordinal }
        _state.value = _state.value.copy(
            filteredExercises = filtered
        )
    }

    private fun deleteExercisePlan(planId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            exerciseRepository.deleteExercisePlan(planId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Update the list by removing the deleted plan
                        val updatedPlans = _state.value.exercisePlans.filter { it.id != planId }
                        _state.value = _state.value.copy(
                            exercisePlans = updatedPlans,
                            isLoading = false,
                            actionSuccess = "Egzersiz planı başarıyla silindi"
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Egzersiz planı silinemedi"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data object NavigateToAddExercise : UiEvent()
        data object NavigateToCreatePlan : UiEvent()
        data object NavigateToExerciseCategories : UiEvent()
    }
}

data class ExerciseManagementState(
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val exercisePlans: List<ExercisePlan> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: String = "",
    val selectedDifficulty: ExerciseDifficulty? = null,
    val actionSuccess: String? = null,
    val patientNames: Map<String, String> = emptyMap()
)

sealed class ExerciseManagementEvent {
    data object LoadExercises : ExerciseManagementEvent()
    data object LoadExercisePlans : ExerciseManagementEvent()
    data class FilterByCategory(val category: String) : ExerciseManagementEvent()
    data object AddExercise : ExerciseManagementEvent()
    data object CreateExercisePlan : ExerciseManagementEvent()
    data object BrowseExerciseCategories : ExerciseManagementEvent()
    data object RefreshExercises : ExerciseManagementEvent()
    data class DeleteExercise(val exerciseId: String) : ExerciseManagementEvent()
    data object ClearActionSuccess : ExerciseManagementEvent()
    data class FilterByDifficulty(val difficulty: ExerciseDifficulty?) : ExerciseManagementEvent()
    data object RefreshExercisePlans : ExerciseManagementEvent()
    data class DeleteExercisePlan(val planId: String) : ExerciseManagementEvent()
}