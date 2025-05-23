package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.NoteUpdate
import com.example.fizyoapp.domain.usecase.note.notefile.UploadNoteDocumentUseCase
import com.example.fizyoapp.domain.usecase.note.notefile.UploadNoteImageUseCase
import com.example.fizyoapp.domain.usecase.note.AddDocumentToNoteUseCase
import com.example.fizyoapp.domain.usecase.note.AddDocumentToNoteUpdateUseCase
import com.example.fizyoapp.domain.usecase.note.AddImageToNoteUseCase
import com.example.fizyoapp.domain.usecase.note.AddImageToNoteUpdateUseCase
import com.example.fizyoapp.domain.usecase.note.AddUpdateToNoteUseCase
import com.example.fizyoapp.domain.usecase.note.DeleteNoteUseCase
import com.example.fizyoapp.domain.usecase.note.DeleteNoteUpdateUseCase
import com.example.fizyoapp.domain.usecase.note.GetNoteByIdUseCase
import com.example.fizyoapp.domain.usecase.note.UpdateNoteUpdateUseCase
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
class NoteDetailViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val addUpdateToNoteUseCase: AddUpdateToNoteUseCase,
    private val updateNoteUpdateUseCase: UpdateNoteUpdateUseCase,
    private val deleteNoteUpdateUseCase: DeleteNoteUpdateUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val uploadNoteImageUseCase: UploadNoteImageUseCase,
    private val uploadNoteDocumentUseCase: UploadNoteDocumentUseCase,
    private val addImageToNoteUseCase: AddImageToNoteUseCase,
    private val addDocumentToNoteUseCase: AddDocumentToNoteUseCase,
    private val addImageToNoteUpdateUseCase: AddImageToNoteUpdateUseCase,
    private val addDocumentToNoteUpdateUseCase: AddDocumentToNoteUpdateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(NoteDetailState())
    val state: StateFlow<NoteDetailState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val noteId: String = savedStateHandle.get<String>("noteId") ?: ""

    init {
        if (noteId.isNotEmpty()) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            getNoteByIdUseCase(noteId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            note = result.data,
                            isLoading = false,
                            updateText = "",
                            selectedUpdateIndex = -1
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Not alınamadı",
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

    fun onEvent(event: NoteDetailEvent) {
        when (event) {
            is NoteDetailEvent.UpdateTextChanged -> {
                _state.value = _state.value.copy(updateText = event.text)
            }
            is NoteDetailEvent.AddUpdate -> addUpdate()
            is NoteDetailEvent.EditUpdate -> {
                _state.value = _state.value.copy(
                    selectedUpdateIndex = event.index,
                    updateText = state.value.note?.updates?.getOrNull(event.index)?.updateText ?: "",
                    isEditingUpdate = true
                )
            }
            is NoteDetailEvent.SaveUpdateEdit -> saveUpdateEdit()
            is NoteDetailEvent.CancelUpdateEdit -> {
                _state.value = _state.value.copy(
                    selectedUpdateIndex = -1,
                    updateText = "",
                    isEditingUpdate = false
                )
            }
            is NoteDetailEvent.DeleteUpdate -> deleteUpdate(event.index)
            is NoteDetailEvent.DeleteNote -> deleteNote()
            is NoteDetailEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateBack(needsRefresh = false))
                }
            }

            is NoteDetailEvent.ShowImagePicker -> {
                _state.value = _state.value.copy(showImagePicker = true)
            }
            is NoteDetailEvent.ShowDocumentPicker -> {
                _state.value = _state.value.copy(showDocumentPicker = true)
            }
            is NoteDetailEvent.AddImage -> {
                _state.value = _state.value.copy(showImagePicker = false)
                uploadAndAddImageToNote(event.uri)
            }
            is NoteDetailEvent.AddDocument -> {
                _state.value = _state.value.copy(showDocumentPicker = false)
                uploadAndAddDocumentToNote(event.uri)
            }

            is NoteDetailEvent.ShowUpdateImagePicker -> {
                _state.value = _state.value.copy(showUpdateImagePicker = true)
            }
            is NoteDetailEvent.ShowUpdateDocumentPicker -> {
                _state.value = _state.value.copy(showUpdateDocumentPicker = true)
            }
            is NoteDetailEvent.AddImageToUpdate -> {
                _state.value = _state.value.copy(
                    showUpdateImagePicker = false,
                    tempImageUris = _state.value.tempImageUris + event.uri
                )
            }
            is NoteDetailEvent.AddDocumentToUpdate -> {
                _state.value = _state.value.copy(
                    showUpdateDocumentPicker = false,
                    tempDocumentUris = _state.value.tempDocumentUris + event.uri
                )
            }
            is NoteDetailEvent.RemoveTempImage -> {
                _state.value = _state.value.copy(
                    tempImageUris = _state.value.tempImageUris.filterIndexed { index, _ -> index != event.index }
                )
            }
            is NoteDetailEvent.RemoveTempDocument -> {
                _state.value = _state.value.copy(
                    tempDocumentUris = _state.value.tempDocumentUris.filterIndexed { index, _ -> index != event.index }
                )
            }
        }
    }

    private fun uploadAndAddImageToNote(uri: Uri) {
        val currentNote = _state.value.note ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                uploadNoteImageUseCase(uri, "notes/${currentNote.physiotherapistId}/images").collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            addImageToNoteUseCase(noteId, result.data).collect { noteResult ->
                                when (noteResult) {
                                    is Resource.Success -> {
                                        _state.value = _state.value.copy(
                                            note = noteResult.data,
                                            isLoading = false
                                        )
                                    }
                                    is Resource.Error -> {
                                        _state.value = _state.value.copy(
                                            error = "Görsel eklenirken hata oluştu",
                                            isLoading = false
                                        )
                                    }
                                    is Resource.Loading -> {
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = "Görsel yüklenirken hata oluştu",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Görsel yüklenirken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun uploadAndAddDocumentToNote(uri: Uri) {
        val currentNote = _state.value.note ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                uploadNoteDocumentUseCase(uri, "notes/${currentNote.physiotherapistId}/documents").collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            addDocumentToNoteUseCase(noteId, result.data).collect { noteResult ->
                                when (noteResult) {
                                    is Resource.Success -> {
                                        _state.value = _state.value.copy(
                                            note = noteResult.data,
                                            isLoading = false
                                        )
                                    }
                                    is Resource.Error -> {
                                        _state.value = _state.value.copy(
                                            error = "Belge eklenirken hata oluştu",
                                            isLoading = false
                                        )
                                    }
                                    is Resource.Loading -> {
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = "Belge yüklenirken hata oluştu",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Belge yüklenirken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun addUpdate() {
        val currentState = _state.value
        if (currentState.updateText.isBlank()) {
            _state.value = _state.value.copy(error = "Not metni boş olamaz")
            return
        }

        val currentNote = currentState.note ?: return
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val imageUrls = mutableListOf<String>()
                for (imageUri in currentState.tempImageUris) {
                    uploadNoteImageUseCase(imageUri, "notes/${currentNote.physiotherapistId}/updates/images").collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                imageUrls.add(result.data)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Görsel yüklenirken hata oluştu: ${result.message}",
                                    isLoading = false
                                )
                                return@collect
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
                }

                val documentUrls = mutableListOf<String>()
                for (documentUri in currentState.tempDocumentUris) {
                    uploadNoteDocumentUseCase(documentUri, "notes/${currentNote.physiotherapistId}/updates/documents").collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                documentUrls.add(result.data)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Belge yüklenirken hata oluştu: ${result.message}",
                                    isLoading = false
                                )
                                return@collect
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
                }

                val update = NoteUpdate(
                    updateText = currentState.updateText,
                    updateDate = Date(),
                    images = imageUrls,
                    documents = documentUrls
                )

                addUpdateToNoteUseCase(noteId, update).collect { result ->
                    _state.value = when (result) {
                        is Resource.Success -> _state.value.copy(
                            note = result.data,
                            updateText = "",
                            isLoading = false,
                            tempImageUris = emptyList(),
                            tempDocumentUris = emptyList()
                        )
                        is Resource.Error -> _state.value.copy(
                            error = "Not güncellenirken bir hata oluştu",
                            isLoading = false
                        )
                        is Resource.Loading -> _state.value.copy(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Not güncellenirken bir hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun saveUpdateEdit() {
        val currentState = _state.value
        val updateIndex = currentState.selectedUpdateIndex
        if (updateIndex < 0 || currentState.note == null || currentState.updateText.isBlank()) {
            _state.value = _state.value.copy(error = "Not metni boş olamaz")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val newUpdate = NoteUpdate(
                updateText = currentState.updateText,
                updateDate = Date()
            )
            updateNoteUpdateUseCase(noteId, updateIndex, newUpdate).collect { result ->
                _state.value = when (result) {
                    is Resource.Success -> _state.value.copy(
                        note = result.data,
                        updateText = "",
                        selectedUpdateIndex = -1,
                        isEditingUpdate = false,
                        isLoading = false
                    )
                    is Resource.Error -> _state.value.copy(
                        error = "Not güncellenirken bir hata oluştu",
                        isLoading = false
                    )
                    is Resource.Loading -> _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun deleteUpdate(index: Int) {
        if (index < 0) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            deleteNoteUpdateUseCase(noteId, index).collect { result ->
                _state.value = when (result) {
                    is Resource.Success -> _state.value.copy(
                        note = result.data,
                        isLoading = false,
                        selectedUpdateIndex = -1,
                        isEditingUpdate = false
                    )
                    is Resource.Error -> _state.value.copy(
                        error = "Not silinirken bir hata oluştu",
                        isLoading = false
                    )
                    is Resource.Loading -> _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun deleteNote() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            deleteNoteUseCase(noteId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiEvent.send(UiEvent.NavigateBack(needsRefresh = true))
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Not silinirken bir hata oluştu",
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

    sealed class UiEvent {
        data class NavigateBack(val needsRefresh: Boolean) : UiEvent()
    }
}