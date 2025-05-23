package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import android.net.Uri
import com.example.fizyoapp.domain.model.note.NoteColor

data class AddNoteState(
    val physiotherapistId: String? = null,
    val patientName: String = "",
    val title: String = "",
    val content: String = "",
    val noteColor: NoteColor = NoteColor.WHITE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageUris: List<Uri> = emptyList(),
    val documentUris: List<Uri> = emptyList(),
    val showImagePicker: Boolean = false,
    val showDocumentPicker: Boolean = false
)