package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes

import com.example.fizyoapp.domain.model.note.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val physiotherapistId: String? = null
)