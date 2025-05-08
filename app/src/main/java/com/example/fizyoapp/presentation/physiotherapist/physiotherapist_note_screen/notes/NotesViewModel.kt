package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.note.DeleteNoteUseCase
import com.example.fizyoapp.domain.usecase.note.GetNotesByPhysiotherapistIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getNotesByPhysiotherapistIdUseCase: GetNotesByPhysiotherapistIdUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

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
                            _state.value = _state.value.copy(physiotherapistId = user.id)
                            loadNotes(user.id)
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Kullanıcı bilgileri alınamadı"
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun loadNotes(physiotherapistId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getNotesByPhysiotherapistIdUseCase(physiotherapistId).collect { result ->
                _state.value = when (result) {
                    is Resource.Success -> _state.value.copy(
                        notes = result.data,
                        isLoading = false
                    )
                    is Resource.Error -> _state.value.copy(
                        error = "Notlar alınamadı",
                        isLoading = false
                    )
                    is Resource.Loading -> _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.Refresh -> {
                _state.value.physiotherapistId?.let { loadNotes(it) }
            }
            is NotesEvent.DeleteNote -> {
                deleteNote(event.noteId)
            }
            is NotesEvent.NavigateToAddNote -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToAddNote)
                }
            }
            is NotesEvent.NavigateToNoteDetail -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToNoteDetail(event.noteId))
                }
            }
        }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId).collect { result ->
                when (result) {
                    is Resource.Success -> {

                        _state.value.physiotherapistId?.let { loadNotes(it) }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Not silinirken bir hata oluştu"
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data object NavigateToAddNote : UiEvent()
        data class NavigateToNoteDetail(val noteId: String) : UiEvent()
    }
}