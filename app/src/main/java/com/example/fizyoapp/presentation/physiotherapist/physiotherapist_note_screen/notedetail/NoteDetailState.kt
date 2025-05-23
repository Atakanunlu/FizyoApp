package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import android.net.Uri


data class NoteDetailState(
    val note: com.example.fizyoapp.domain.model.note.Note? = null,
    val updateText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedUpdateIndex: Int = -1,
    val isEditingUpdate: Boolean = false,

    val showImagePicker: Boolean = false,
    val showDocumentPicker: Boolean = false,
    val showUpdateImagePicker: Boolean = false,
    val showUpdateDocumentPicker: Boolean = false,

    val tempImageUris: List<Uri> = emptyList(),
    val tempDocumentUris: List<Uri> = emptyList()
)