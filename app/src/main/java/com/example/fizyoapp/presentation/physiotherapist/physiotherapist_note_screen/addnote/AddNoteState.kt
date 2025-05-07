package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import com.example.fizyoapp.domain.model.note.NoteColor

data class AddNoteState(
    val physiotherapistId: String? = null,
    val patientName: String = "",
    val title: String = "",
    val content: String = "",
    val noteColor: NoteColor = NoteColor.WHITE,
    val isLoading: Boolean = false,
    val error: String? = null
)