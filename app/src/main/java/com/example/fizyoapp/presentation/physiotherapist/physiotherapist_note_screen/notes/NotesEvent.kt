package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes

sealed class NotesEvent {
    data object Refresh : NotesEvent()
    data class DeleteNote(val noteId: String) : NotesEvent()
    data object NavigateToAddNote : NotesEvent()
    data class NavigateToNoteDetail(val noteId: String) : NotesEvent()
}