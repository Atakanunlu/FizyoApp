package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

sealed class NoteDetailEvent {
    data class UpdateTextChanged(val text: String) : NoteDetailEvent()
    data object AddUpdate : NoteDetailEvent()
    data class EditUpdate(val index: Int) : NoteDetailEvent()
    data object SaveUpdateEdit : NoteDetailEvent()
    data object CancelUpdateEdit : NoteDetailEvent()
    data class DeleteUpdate(val index: Int) : NoteDetailEvent()
    data object DeleteNote : NoteDetailEvent()
    data object SaveNote : NoteDetailEvent()
    data object NavigateBack : NoteDetailEvent()
}