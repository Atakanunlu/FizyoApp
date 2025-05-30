package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.note.notefile.UploadNoteDocumentUseCase
import com.example.fizyoapp.domain.usecase.note.notefile.UploadNoteImageUseCase
import com.example.fizyoapp.domain.usecase.note.CreateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val uploadNoteImageUseCase: UploadNoteImageUseCase,
    private val uploadNoteDocumentUseCase: UploadNoteDocumentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddNoteState())
    val state: StateFlow<AddNoteState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _state.value = _state.value.copy(
                                physiotherapistId = user.id,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Kullanıcı bilgileri alınamadı",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun onEvent(event: AddNoteEvent) {
        when (event) {
            is AddNoteEvent.PatientNameChanged -> {
                _state.value = _state.value.copy(patientName = event.patientName)
            }
            is AddNoteEvent.TitleChanged -> {
                _state.value = _state.value.copy(title = event.title)
            }
            is AddNoteEvent.ContentChanged -> {
                _state.value = _state.value.copy(content = event.content)
            }
            is AddNoteEvent.ColorChanged -> {
                _state.value = _state.value.copy(noteColor = event.color)
            }
            is AddNoteEvent.AddImage -> {
                _state.value = _state.value.copy(
                    imageUris = _state.value.imageUris + event.uri,
                    showImagePicker = false
                )
            }
            is AddNoteEvent.RemoveImage -> {
                _state.value = _state.value.copy(
                    imageUris = _state.value.imageUris.filterIndexed { index, _ -> index != event.index }
                )
            }
            is AddNoteEvent.AddDocument -> {
                _state.value = _state.value.copy(
                    documentUris = _state.value.documentUris + event.uri,
                    showDocumentPicker = false
                )
            }
            is AddNoteEvent.RemoveDocument -> {
                _state.value = _state.value.copy(
                    documentUris = _state.value.documentUris.filterIndexed { index, _ -> index != event.index }
                )
            }
            is AddNoteEvent.SaveNote -> {
                saveNote()
            }
            is AddNoteEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateBack(needsRefresh = false))
                }
            }
            is AddNoteEvent.ShowImagePicker -> {
                _state.value = _state.value.copy(showImagePicker = true)
            }
            is AddNoteEvent.ShowDocumentPicker -> {
                _state.value = _state.value.copy(showDocumentPicker = true)
            }
        }
    }

    private fun saveNote() {
        val currentState = _state.value
        when {
            currentState.patientName.isBlank() -> {
                _state.value = _state.value.copy(error = "Hasta adı boş olamaz")
                return
            }
            currentState.title.isBlank() -> {
                _state.value = _state.value.copy(error = "Başlık boş olamaz")
                return
            }
            currentState.content.isBlank() -> {
                _state.value = _state.value.copy(error = "Not içeriği boş olamaz")
                return
            }
        }
        val physiotherapistId = currentState.physiotherapistId ?: return
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val imageUrls = mutableListOf<String>()
                for (imageUri in currentState.imageUris) {
                    uploadNoteImageUseCase(imageUri, "notes/$physiotherapistId/images").collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                imageUrls.add(result.data)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Görsel yüklenirken hata oluştu: ${result.message}",
                                    isLoading = false,
                                    showImagePicker = false
                                )
                                return@collect
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
                val documentUrls = mutableListOf<String>()
                for (documentUri in currentState.documentUris) {
                    uploadNoteDocumentUseCase(documentUri, "notes/$physiotherapistId/documents").collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                documentUrls.add(result.data)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Belge yüklenirken hata oluştu: ${result.message}",
                                    isLoading = false,
                                    showDocumentPicker = false
                                )
                                return@collect
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
                val now = Date()
                val note = Note(
                    physiotherapistId = physiotherapistId,
                    patientName = currentState.patientName,
                    title = currentState.title,
                    content = currentState.content,
                    creationDate = now,
                    updateDate = now,
                    color = currentState.noteColor,
                    updates = emptyList(),
                    images = imageUrls,
                    documents = documentUrls
                )
                createNoteUseCase(note).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                showImagePicker = false,
                                showDocumentPicker = false
                            )
                            _uiEvent.send(UiEvent.NavigateBack(needsRefresh = true))
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = "Not oluşturulurken bir hata oluştu",
                                isLoading = false,
                                showImagePicker = false,
                                showDocumentPicker = false
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Not oluşturulurken bir hata oluştu: ${e.message}",
                    isLoading = false,
                    showImagePicker = false,
                    showDocumentPicker = false
                )
            }
        }
    }

    sealed class UiEvent {
        data class NavigateBack(val needsRefresh: Boolean) : UiEvent()
    }
}