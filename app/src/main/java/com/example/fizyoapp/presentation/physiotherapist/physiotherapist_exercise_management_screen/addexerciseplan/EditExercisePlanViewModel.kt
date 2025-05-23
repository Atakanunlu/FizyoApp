package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan



import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercise.ExercisePlan
import com.example.fizyoapp.domain.model.exercise.ExercisePlanItem
import com.example.fizyoapp.domain.model.exercise.ExercisePlanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EditExercisePlanViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EditExercisePlanState())
    val state: StateFlow<EditExercisePlanState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // planId parametresini navArgs'dan al
    private val planId: String = savedStateHandle.get<String>("planId") ?: ""

    fun loadExercisePlan(planId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            exerciseRepository.getExercisePlanById(planId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val plan = result.data
                        if (plan != null) {
                            // Hasta adını getir
                            loadPatientName(plan.patientId)

                            _state.update {
                                it.copy(
                                    plan = plan,
                                    title = plan.title,
                                    description = plan.description,
                                    startDate = plan.startDate,
                                    endDate = plan.endDate,
                                    frequency = plan.frequency,
                                    notes = plan.notes ?: "",
                                    exercises = plan.exercises,
                                    selectedStatus = plan.status,
                                    isLoading = false
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    errorMessage = "Plan bulunamadı",
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                errorMessage = result.message ?: "Plan yüklenirken bir hata oluştu",
                                isLoading = false
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

    private fun loadPatientName(patientId: String) {
        viewModelScope.launch {
            userProfileRepository.getUserProfile(patientId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val profile = result.data
                        if (profile != null && profile.firstName.isNotEmpty() && profile.lastName.isNotEmpty()) {
                            _state.update {
                                it.copy(patientName = "${profile.firstName} ${profile.lastName}")
                            }
                        } else {
                            // Profil yoksa e-posta adresini göster
                            fallbackToEmailName(patientId)
                        }
                    }
                    else -> {
                        // Hata durumunda e-posta adresini göster
                        fallbackToEmailName(patientId)
                    }
                }
            }
        }
    }

    private suspend fun fallbackToEmailName(patientId: String) {
        try {
            // User koleksiyonundan e-posta adresini al
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("user")
                .document(patientId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.getString("email") ?: "Kullanıcı"
                        _state.update { it.copy(patientName = email) }
                    } else {
                        _state.update { it.copy(patientName = "Hasta #${patientId.takeLast(5)}") }
                    }
                }
                .addOnFailureListener {
                    _state.update { it.copy(patientName = "Hasta #${patientId.takeLast(5)}") }
                }
        } catch (e: Exception) {
            _state.update { it.copy(patientName = "Hasta #${patientId.takeLast(5)}") }
        }
    }

    fun onTitleChanged(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(description = value) }
    }

    fun onStartDateChanged(value: Date) {
        _state.update { it.copy(startDate = value) }
    }

    fun onEndDateChanged(value: Date) {
        _state.update { it.copy(endDate = value) }
    }

    fun onFrequencyChanged(value: String) {
        _state.update { it.copy(frequency = value) }
    }

    fun onNotesChanged(value: String) {
        _state.update { it.copy(notes = value) }
    }

    fun updateExerciseDetails(exerciseId: String, sets: String, reps: String, duration: String, notes: String) {
        val updatedExercises = _state.value.exercises.map { item ->
            if (item.exerciseId == exerciseId) {
                item.copy(
                    sets = sets.toIntOrNull() ?: 0,
                    repetitions = reps.toIntOrNull() ?: 0,
                    duration = duration.toIntOrNull() ?: 0,
                    notes = notes
                    // mediaUrls korunuyor, copy kullandığımız için değiştirilmeyen özellikler aynı kalır
                )
            } else {
                item
            }
        }

        _state.update { it.copy(exercises = updatedExercises) }
    }

    fun toggleStatusSelectionDialog() {
        _state.update { it.copy(showStatusSelectionDialog = !it.showStatusSelectionDialog) }
    }

    fun onStatusSelected(status: ExercisePlanStatus) {
        _state.update { it.copy(selectedStatus = status) }
    }

    fun updatePlanStatus() {
        val plan = _state.value.plan ?: return
        val updatedPlan = plan.copy(status = _state.value.selectedStatus)

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            exerciseRepository.updateExercisePlan(updatedPlan).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                plan = updatedPlan,
                                isSaving = false
                            )
                        }
                        sendUiEvent(UiEvent.ShowSuccess("Plan durumu güncellendi"))
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isSaving = false) }
                        sendUiEvent(
                            UiEvent.ShowError(
                                result.message ?: "Plan durumu güncellenemedi"
                            )
                        )
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }

    fun saveExercisePlan() {
        val currentPlan = _state.value.plan ?: return

        // Tüm alanları kontrol et
        if (_state.value.title.isBlank()) {
            sendUiEvent(UiEvent.ShowError("Plan başlığı boş olamaz"))
            return
        }

        val updatedPlan = currentPlan.copy(
            title = _state.value.title,
            description = _state.value.description,
            startDate = _state.value.startDate,
            endDate = _state.value.endDate,
            frequency = _state.value.frequency,
            notes = _state.value.notes,
            exercises = _state.value.exercises,
            status = _state.value.selectedStatus,
            updatedAt = Date()
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            exerciseRepository.updateExercisePlan(updatedPlan).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                plan = updatedPlan,
                                isSaving = false
                            )
                        }
                        sendUiEvent(UiEvent.ShowSuccess("Plan başarıyla güncellendi"))
                        sendUiEvent(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isSaving = false) }
                        sendUiEvent(UiEvent.ShowError(result.message ?: "Plan güncellenemedi"))
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class ShowSuccess(val message: String) : UiEvent()
        data object NavigateBack : UiEvent()
    }
}

data class EditExercisePlanState(
    val plan: ExercisePlan? = null,
    val title: String = "",
    val description: String = "",
    val patientName: String = "Yükleniyor...",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val frequency: String = "",
    val notes: String = "",
    val exercises: List<ExercisePlanItem> = emptyList(),
    val selectedStatus: ExercisePlanStatus = ExercisePlanStatus.ACTIVE,
    val showStatusSelectionDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)