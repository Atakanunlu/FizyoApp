package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import com.example.fizyoapp.domain.model.note.Note

data class NoteDetailState(
    val note: Note? = null,
    val updateText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedUpdateIndex: Int = -1,
    val isEditingUpdate: Boolean = false
)