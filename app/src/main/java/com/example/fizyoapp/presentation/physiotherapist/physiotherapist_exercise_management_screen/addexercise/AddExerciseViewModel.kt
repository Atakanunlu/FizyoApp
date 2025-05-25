package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexercise
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseDifficulty
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddExerciseState())
    val state: StateFlow<AddExerciseState> = _state.asStateFlow()
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private var currentUser: User? = null

    init {
        _state.value = _state.value.copy(isLoading = false)
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                getCurrentUserUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            currentUser = result.data
                            _state.value = _state.value.copy(isLoading = false)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(isLoading = false)
                            _uiEvent.send(UiEvent.ShowError("Kullanıcı bilgisi alınamadı"))
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowError("Kullanıcı bilgisi alınamadı: ${e.message}"))
            }
        }
    }

    fun onEvent(event: AddExerciseEvent) {
        when (event) {
            is AddExerciseEvent.TitleChanged -> {
                _state.value = _state.value.copy(
                    title = event.title,
                    titleError = if (event.title.isBlank()) "Egzersiz adı boş olamaz" else null
                )
            }
            is AddExerciseEvent.CategoryChanged -> {
                _state.value = _state.value.copy(
                    category = event.category
                )
            }
            is AddExerciseEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(
                    description = event.description
                )
            }
            is AddExerciseEvent.InstructionsChanged -> {
                _state.value = _state.value.copy(
                    instructions = event.instructions
                )
            }
            is AddExerciseEvent.SetsChanged -> {
                val sets = event.sets.filter { it.isDigit() }
                _state.value = _state.value.copy(
                    sets = sets
                )
            }
            is AddExerciseEvent.RepetitionsChanged -> {
                val repetitions = event.repetitions.filter { it.isDigit() }
                _state.value = _state.value.copy(
                    repetitions = repetitions
                )
            }
            is AddExerciseEvent.DurationChanged -> {
                val duration = event.duration.filter { it.isDigit() }
                _state.value = _state.value.copy(
                    duration = duration
                )
            }
            is AddExerciseEvent.DifficultyChanged -> {
                _state.value = _state.value.copy(
                    difficulty = event.difficulty
                )
            }
            is AddExerciseEvent.AddMedia -> {
                _state.value = _state.value.copy(
                    mediaUris = _state.value.mediaUris + event.uri
                )
            }
            is AddExerciseEvent.RemoveMedia -> {
                _state.value = _state.value.copy(
                    mediaUris = _state.value.mediaUris.filter { it != event.uri }
                )
            }
            is AddExerciseEvent.SaveExercise -> saveExercise()
        }
    }

    private fun saveExercise() {
        val userId = currentUser?.id ?: run {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowError("Kullanıcı bilgisi alınamadı"))
            }
            return
        }

        val title = _state.value.title
        if (title.isBlank()) {
            _state.value = _state.value.copy(
                titleError = "Egzersiz adı boş olamaz",
                isLoading = false
            )
            return
        }

        val category = _state.value.category
        if (category.isBlank()) {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowError("Lütfen bir kategori seçin"))
            }
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val mediaUrls = mutableListOf<String>()
                val mediaTypes = mutableMapOf<String, ExerciseType>()

                try {
                    for (mediaUri in _state.value.mediaUris) {
                        val uri = Uri.parse(mediaUri)
                        val fileName = "exercise_media_${UUID.randomUUID()}"
                        exerciseRepository.uploadExerciseMedia(uri, userId, fileName).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    val downloadUrl = result.data
                                    mediaUrls.add(downloadUrl)
                                    val mediaType = when {
                                        mediaUri.contains("image") -> ExerciseType.IMAGE
                                        mediaUri.contains("video") -> ExerciseType.VIDEO
                                        else -> ExerciseType.IMAGE
                                    }
                                    mediaTypes[downloadUrl] = mediaType
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(isLoading = false)
                                    _uiEvent.send(UiEvent.ShowError("Medya yüklenemedi: ${result.message}"))
                                    return@collect
                                }
                                is Resource.Loading -> {}
                            }
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowError("Medya yükleme hatası: ${e.message}"))
                    return@launch
                }

                val exercise = Exercise(
                    physiotherapistId = userId,
                    title = title,
                    description = _state.value.description,
                    category = category,
                    mediaUrls = mediaUrls,
                    mediaType = mediaTypes,
                    instructions = _state.value.instructions,
                    difficulty = _state.value.difficulty,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                try {
                    exerciseRepository.createExercise(exercise).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(isLoading = false)
                                _uiEvent.send(UiEvent.NavigateBack())
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(isLoading = false)
                                _uiEvent.send(UiEvent.ShowError("Egzersiz kaydedilemedi: ${result.message}"))
                            }
                            is Resource.Loading -> {}
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowError("Egzersiz kaydetme hatası: ${e.message}"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowError("Beklenmeyen bir hata oluştu: ${e.message}"))
            }
        }
    }

    sealed class UiEvent {
        data class NavigateBack(val refresh: Boolean = true) : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}

data class AddExerciseState(
    val title: String = "",
    val titleError: String? = null,
    val category: String = "",
    val description: String = "",
    val instructions: String = "",
    val sets: String = "",
    val repetitions: String = "",
    val duration: String = "",
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.MEDIUM,
    val mediaUris: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false
)

sealed class AddExerciseEvent {
    data class TitleChanged(val title: String) : AddExerciseEvent()
    data class CategoryChanged(val category: String) : AddExerciseEvent()
    data class DescriptionChanged(val description: String) : AddExerciseEvent()
    data class InstructionsChanged(val instructions: String) : AddExerciseEvent()
    data class SetsChanged(val sets: String) : AddExerciseEvent()
    data class RepetitionsChanged(val repetitions: String) : AddExerciseEvent()
    data class DurationChanged(val duration: String) : AddExerciseEvent()
    data class DifficultyChanged(val difficulty: ExerciseDifficulty) : AddExerciseEvent()
    data class AddMedia(val uri: String, val type: String) : AddExerciseEvent()
    data class RemoveMedia(val uri: String) : AddExerciseEvent()
    data object SaveExercise : AddExerciseEvent()
}