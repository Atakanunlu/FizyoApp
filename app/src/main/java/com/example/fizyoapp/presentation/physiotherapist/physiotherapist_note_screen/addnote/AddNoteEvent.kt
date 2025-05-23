package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import android.net.Uri
import com.example.fizyoapp.domain.model.note.NoteColor

sealed class AddNoteEvent {
    data class PatientNameChanged(val patientName: String) : AddNoteEvent()
    data class TitleChanged(val title: String) : AddNoteEvent()
    data class ContentChanged(val content: String) : AddNoteEvent()
    data class ColorChanged(val color: NoteColor) : AddNoteEvent()
    data class AddImage(val uri: Uri) : AddNoteEvent()
    data class RemoveImage(val index: Int) : AddNoteEvent()
    data class AddDocument(val uri: Uri) : AddNoteEvent()
    data class RemoveDocument(val index: Int) : AddNoteEvent()
    data object SaveNote : AddNoteEvent()
    data object NavigateBack : AddNoteEvent()
    data object ShowImagePicker : AddNoteEvent()
    data object ShowDocumentPicker : AddNoteEvent()
}