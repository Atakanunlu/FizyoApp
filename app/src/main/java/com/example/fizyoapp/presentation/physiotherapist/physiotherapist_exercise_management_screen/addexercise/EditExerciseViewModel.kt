package com.example.fizyoapp.presentation.physiotherapist.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.exercisemanagescreen.ExerciseRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseDifficulty
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EditExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(EditExerciseState())
    val state: StateFlow<EditExerciseState> = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: ""
    private var originalExercise: Exercise? = null

    init {
        if (exerciseId.isNotEmpty()) {
            loadExercise(exerciseId)
        }
    }

    fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            exerciseRepository.getExerciseById(exerciseId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val exercise = result.data
                        originalExercise = exercise

                        // Hata ayıklama
                        println("EditVM - Media URLs: ${exercise.mediaUrls}")
                        println("EditVM - Media Types Original: ${exercise.mediaType}")

                        // mediaTypes'ı URL'lere göre eşleştirin ve kontrol edin
                        val mediaTypes = mutableMapOf<String, ExerciseType>()
                        exercise.mediaUrls.forEach { url ->
                            // URL'yi mediaType haritasında kontrol et veya tahmin et
                            val type = exercise.mediaType[url] ?:
                            if (url.contains("video") || url.contains(".mp4") ||
                                url.contains(".mov") || url.contains(".avi") ||
                                url.contains(".webm")) {
                                ExerciseType.VIDEO
                            } else {
                                ExerciseType.IMAGE
                            }
                            mediaTypes[url] = type
                        }

                        // Son hata ayıklama
                        println("EditVM - Processed Media Types: $mediaTypes")

                        _state.update {
                            it.copy(
                                exerciseId = exercise.id,
                                title = exercise.title,
                                description = exercise.description,
                                category = exercise.category,
                                instructions = exercise.instructions,
                                difficulty = exercise.difficulty,
                                mediaUris = exercise.mediaUrls,
                                mediaTypes = mediaTypes,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                errorMessage = result.message ?: "Egzersiz yüklenemedi",
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

    fun onEvent(event: EditExerciseEvent) {
        when (event) {
            is EditExerciseEvent.TitleChanged -> {
                _state.update {
                    it.copy(
                        title = event.title,
                        titleError = if (event.title.isBlank()) "Başlık boş olamaz" else null
                    )
                }
            }
            is EditExerciseEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is EditExerciseEvent.CategoryChanged -> {
                _state.update { it.copy(category = event.category) }
            }
            is EditExerciseEvent.InstructionsChanged -> {
                _state.update { it.copy(instructions = event.instructions) }
            }
            is EditExerciseEvent.DifficultyChanged -> {
                _state.update { it.copy(difficulty = event.difficulty) }
            }
            is EditExerciseEvent.AddMedia -> {
                // Video türünü daha güvenilir şekilde belirleme
                val isVideo = event.type == "video" ||
                        event.uri.contains("video") ||
                        event.uri.contains(".mp4") ||
                        event.uri.contains(".mov") ||
                        event.uri.contains(".avi") ||
                        event.uri.contains(".webm")

                val mediaType = if (isVideo) ExerciseType.VIDEO else ExerciseType.IMAGE
                println("EditVM - Adding media: ${event.uri}, Type: $mediaType")

                val updatedMediaUris = _state.value.mediaUris.toMutableList()
                updatedMediaUris.add(event.uri)

                // MediaTypes'ı güncelle
                val updatedMediaTypes = _state.value.mediaTypes.toMutableMap()
                updatedMediaTypes[event.uri] = mediaType

                _state.update { it.copy(
                    mediaUris = updatedMediaUris,
                    mediaTypes = updatedMediaTypes
                ) }
            }
            is EditExerciseEvent.RemoveMedia -> {
                val updatedMediaUris = _state.value.mediaUris.toMutableList()
                updatedMediaUris.remove(event.uri)

                // MediaTypes'dan da kaldır
                val updatedMediaTypes = _state.value.mediaTypes.toMutableMap()
                updatedMediaTypes.remove(event.uri)

                _state.update { it.copy(
                    mediaUris = updatedMediaUris,
                    mediaTypes = updatedMediaTypes
                ) }
            }
            is EditExerciseEvent.UpdateExercise -> {
                updateExercise()
            }
            else -> {}
        }
    }

    private fun updateExercise() {
        val state = _state.value

        if (state.title.isBlank()) {
            _state.update { it.copy(titleError = "Başlık boş olamaz") }
            return
        }

        if (state.category.isBlank()) {
            sendUiEvent(UiEvent.ShowError("Lütfen bir kategori seçin"))
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Güncellenmiş Exercise nesnesini oluştur
            val updatedExercise = Exercise(
                id = state.exerciseId,
                physiotherapistId = originalExercise?.physiotherapistId ?: "",
                title = state.title,
                description = state.description,
                category = state.category,
                instructions = state.instructions,
                difficulty = state.difficulty,
                mediaUrls = state.mediaUris,
                mediaType = state.mediaTypes,
                duration = originalExercise?.duration ?: 0,
                repetitions = originalExercise?.repetitions ?: 0,
                sets = originalExercise?.sets ?: 0,
                createdAt = originalExercise?.createdAt ?: Date(),
                updatedAt = Date()
            )

            // Tür bilgilerini kontrol et
            println("Updating exercise with mediaTypes: ${state.mediaTypes.map { "${it.key}: ${it.value}" }}")

            exerciseRepository.updateExercise(updatedExercise).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        sendUiEvent(UiEvent.ShowSuccess("Egzersiz başarıyla güncellendi"))
                        sendUiEvent(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                errorMessage = result.message ?: "Egzersiz güncellenemedi",
                                isLoading = false
                            )
                        }
                        sendUiEvent(UiEvent.ShowError(result.message ?: "Egzersiz güncellenemedi"))
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
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
        object NavigateBack : UiEvent()
    }
}

data class EditExerciseState(
    val exerciseId: String = "",
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val category: String = "",
    val instructions: String = "",
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.MEDIUM,
    val mediaUris: List<String> = emptyList(),
    val mediaTypes: Map<String, ExerciseType> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class EditExerciseEvent {
    data class TitleChanged(val title: String) : EditExerciseEvent()
    data class DescriptionChanged(val description: String) : EditExerciseEvent()
    data class CategoryChanged(val category: String) : EditExerciseEvent()
    data class InstructionsChanged(val instructions: String) : EditExerciseEvent()
    data class DifficultyChanged(val difficulty: ExerciseDifficulty) : EditExerciseEvent()
    data class AddMedia(val uri: String, val type: String) : EditExerciseEvent()
    data class RemoveMedia(val uri: String) : EditExerciseEvent()
    object UpdateExercise : EditExerciseEvent()
}