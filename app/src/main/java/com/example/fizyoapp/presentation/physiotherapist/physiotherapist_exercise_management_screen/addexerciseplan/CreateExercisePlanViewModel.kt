package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.repository.exercisemanagescreen.PatientListItem
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercise.Exercise
import com.example.fizyoapp.domain.model.exercise.ExercisePlan
import com.example.fizyoapp.domain.model.exercise.ExercisePlanItem
import com.example.fizyoapp.domain.model.exercise.ExercisePlanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateExercisePlanViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val authRepository: AuthRepository,
    private val messagesRepository: MessagesRepository // Mesaj thread'lerini almak için ekledik
) : ViewModel() {

    private val _state = MutableStateFlow(CreateExercisePlanState())
    val state: StateFlow<CreateExercisePlanState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentUserId: String = ""

    init {
        _state.value = _state.value.copy(isLoading = false)
        getCurrentUser()
    }
    private fun getCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = false) } // Start with loading explicitly set to false

            try {
                authRepository.getCurrentUser().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data.user?.let { user ->
                                currentUserId = user.id
                                loadExercises(user.id)
                                loadPatientsFromMessages(user.id)
                                // Explicitly set isLoading to false after success
                                _state.update { it.copy(isLoading = false) }
                            }
                        }
                        is Resource.Error -> {
                            // Explicitly set isLoading to false after error
                            _state.update { it.copy(isLoading = false) }
                            sendUiEvent(
                                UiEvent.ShowError(
                                    result.message ?: "Kullanıcı bilgileri alınamadı"
                                )
                            )
                        }
                        is Resource.Loading -> {
                            // We want to track only specific loading operations, not the general isLoading
                            // Don't update isLoading here
                        }
                    }
                }
            } catch (e: Exception) {
                // Set isLoading to false in case of exception
                _state.update { it.copy(isLoading = false) }
                sendUiEvent(UiEvent.ShowError("Bir hata oluştu: ${e.message}"))
            }
        }
    }

    private fun loadExercises(physiotherapistId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingExercises = true) }
            exerciseRepository.getExercisesByPhysiotherapist(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                availableExercises = result.data,
                                isLoadingExercises = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoadingExercises = false,
                                error = result.message ?: "Egzersizler yüklenemedi"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoadingExercises = true) }
                    }
                }
            }
        }
    }

    // Mesaj konuşmalarından hastaları çek - MedicalReportScreen'deki gibi
    private fun loadPatientsFromMessages(physiotherapistId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPatients = true) }

            try {
                // Fizyoterapistin tüm mesaj konuşmalarını getir
                messagesRepository.getChatTreadsForUser(physiotherapistId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val threads = result.data
                            Log.d("CreateExercisePlanVM", "Konuşma sayısı: ${threads.size}")

                            // Hasta listesini oluştur
                            val patients = mutableListOf<PatientListItem>()

                            for (thread in threads) {
                                // Karşı taraf ID'sini bul (fizyoterapist olmayan)
                                val otherUserId = thread.participantIds.firstOrNull {
                                    it != physiotherapistId
                                } ?: continue

                                // Hasta adını ve profil fotoğrafını al
                                val patientName = thread.otherParticipantName
                                val patientPhoto = thread.otherParticipantPhotoUrl

                                // Hasta listesine ekle (daha önce eklenmemişse)
                                if (!patients.any { it.userId == otherUserId }) {
                                    patients.add(PatientListItem(
                                        userId = otherUserId,
                                        fullName = patientName,
                                        profilePhotoUrl = patientPhoto
                                    ))
                                    Log.d("CreateExercisePlanVM", "Hasta eklendi: $patientName, ID: $otherUserId")
                                }
                            }

                            Log.d("CreateExercisePlanVM", "Toplam hasta sayısı: ${patients.size}")
                            _state.update {
                                it.copy(
                                    patients = patients,
                                    isLoadingPatients = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            Log.e("CreateExercisePlanVM", "Hastalar yüklenirken hata: ${result.message}")
                            _state.update {
                                it.copy(
                                    isLoadingPatients = false,
                                    error = result.message ?: "Hastalar yüklenemedi"
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _state.update { it.copy(isLoadingPatients = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateExercisePlanVM", "Hastalar yüklenirken istisna", e)
                _state.update {
                    it.copy(
                        isLoadingPatients = false,
                        error = e.message ?: "Beklenmeyen bir hata oluştu"
                    )
                }
            }
        }
    }

    fun onEvent(event: CreateExercisePlanEvent) {
        when (event) {
            is CreateExercisePlanEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.title,
                        titleError = if (event.title.isBlank()) "Başlık boş olamaz" else null
                    )
                }
            }
            is CreateExercisePlanEvent.PatientSelected -> {
                _state.update { it.copy(selectedPatient = event.patient) }
            }
            is CreateExercisePlanEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is CreateExercisePlanEvent.StartDateChanged -> {
                _state.update { it.copy(startDate = event.date) }
            }
            is CreateExercisePlanEvent.EndDateChanged -> {
                _state.update { it.copy(endDate = event.date) }
            }
            is CreateExercisePlanEvent.FrequencyChanged -> {
                _state.update { it.copy(frequency = event.frequency) }
            }
            is CreateExercisePlanEvent.NotesChanged -> {
                _state.update { it.copy(notes = event.notes) }
            }
            is CreateExercisePlanEvent.AddExercise -> {
                val exerciseItem = ExercisePlanItem(
                    exerciseId = event.exercise.id,
                    exerciseTitle = event.exercise.title,
                    sets = 3, // Varsayılan değerler
                    repetitions = 10,
                    duration = 30,
                    notes = "",
                    mediaUrls = event.exercise.mediaUrls
                )
                _state.update {
                    it.copy(exercises = it.exercises + exerciseItem)
                }
            }
            is CreateExercisePlanEvent.RemoveExercise -> {
                _state.update {
                    it.copy(exercises = it.exercises.filterIndexed { index, _ -> index != event.index })
                }
            }
            is CreateExercisePlanEvent.UpdateExerciseDetails -> {
                val updatedExercises = _state.value.exercises.toMutableList()
                if (event.index < updatedExercises.size) {
                    val item = updatedExercises[event.index]
                    updatedExercises[event.index] = item.copy(
                        sets = event.sets.toIntOrNull() ?: 0,
                        repetitions = event.repetitions.toIntOrNull() ?: 0, // 'reps' yerine 'repetitions' olmalı
                        duration = event.duration.toIntOrNull() ?: 0,
                        notes = event.notes
                    )
                    _state.update { it.copy(exercises = updatedExercises) }
                }
            }
            is CreateExercisePlanEvent.SavePlan -> {
                savePlan()
            }
            is CreateExercisePlanEvent.ResetLoadingState -> {
                _state.update { it.copy(isLoading = false) }
                Log.d("CreateExercisePlanVM", "Loading state manually reset to FALSE")
            }
        }
    }
    private fun savePlan() {
        viewModelScope.launch {
            val currentState = _state.value

            // Basic validation
            if (currentState.title.isBlank()) {
                _state.update { it.copy(titleError = "Başlık boş olamaz") }
                return@launch
            }

            if (currentState.selectedPatient == null) {
                sendUiEvent(UiEvent.ShowError("Lütfen bir hasta seçin"))
                return@launch
            }

            if (currentState.exercises.isEmpty()) {
                sendUiEvent(UiEvent.ShowError("Lütfen en az bir egzersiz ekleyin"))
                return@launch
            }

            // Explicitly set loading state to true
            _state.update { it.copy(isLoading = true) }
            Log.d("CreateExercisePlanVM", "Starting save operation, setting isLoading = true")

            try {
                val plan = ExercisePlan(
                    physiotherapistId = currentUserId,
                    patientId = currentState.selectedPatient.userId,
                    title = currentState.title,
                    description = currentState.description,
                    exercises = currentState.exercises,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    frequency = currentState.frequency,
                    notes = currentState.notes,
                    status = ExercisePlanStatus.ACTIVE,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                // Save the plan
                exerciseRepository.createExercisePlan(plan).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("CreateExercisePlanVM", "Save successful, setting isLoading = false")
                            _state.update { it.copy(isLoading = false) }
                            sendUiEvent(UiEvent.NavigateBack)
                        }
                        is Resource.Error -> {
                            Log.d("CreateExercisePlanVM", "Save error: ${result.message}, setting isLoading = false")
                            _state.update { it.copy(isLoading = false) }
                            sendUiEvent(UiEvent.ShowError(result.message ?: "Plan kaydedilemedi"))
                        }
                        is Resource.Loading -> {
                            // Loading state already set at the beginning, don't need to update here
                            Log.d("CreateExercisePlanVM", "Save in progress, isLoading already true")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateExercisePlanVM", "Exception during save", e)
                _state.update { it.copy(isLoading = false) }
                sendUiEvent(UiEvent.ShowError("Bir hata oluştu: ${e.message}"))
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
        object NavigateBack : UiEvent()
    }
}

data class CreateExercisePlanState(
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val frequency: String = "",
    val notes: String = "",
    val selectedPatient: PatientListItem? = null,
    val patients: List<PatientListItem> = emptyList(),
    val exercises: List<ExercisePlanItem> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPatients: Boolean = false,
    val isLoadingExercises: Boolean = false,
    val error: String? = null  // Bu satırı ekleyelim

)

sealed class CreateExercisePlanEvent {
    data class TitleChanged(val title: String) : CreateExercisePlanEvent()
    data class DescriptionChanged(val description: String) : CreateExercisePlanEvent()
    data class StartDateChanged(val date: Date) : CreateExercisePlanEvent()
    data class EndDateChanged(val date: Date) : CreateExercisePlanEvent()
    data class FrequencyChanged(val frequency: String) : CreateExercisePlanEvent()
    data class NotesChanged(val notes: String) : CreateExercisePlanEvent()
    data class PatientSelected(val patient: PatientListItem) : CreateExercisePlanEvent()
    data class AddExercise(val exercise: Exercise) : CreateExercisePlanEvent()
    data class RemoveExercise(val index: Int) : CreateExercisePlanEvent()
    data object ResetLoadingState : CreateExercisePlanEvent()
    data class UpdateExerciseDetails(
        val index: Int,
        val sets: String,
        val repetitions: String,
        val duration: String,
        val notes: String
    ) : CreateExercisePlanEvent()
    data object SavePlan : CreateExercisePlanEvent()
}