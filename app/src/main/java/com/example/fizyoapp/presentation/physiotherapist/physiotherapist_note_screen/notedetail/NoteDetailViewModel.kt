package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.NoteUpdate
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
            is NoteDetailEvent.SaveNote -> { /* Kullanılmıyor */ }
            is NoteDetailEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateBack(needsRefresh = false))
                }
            }
        }
    }

    private fun addUpdate() {
        val currentState = _state.value
        if (currentState.updateText.isBlank()) {
            _state.value = _state.value.copy(error = "Not metni boş olamaz")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val update = NoteUpdate(
                updateText = currentState.updateText,
                updateDate = Date()
            )

            addUpdateToNoteUseCase(noteId, update).collect { result ->
                _state.value = when (result) {
                    is Resource.Success -> _state.value.copy(
                        note = result.data,
                        updateText = "",
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