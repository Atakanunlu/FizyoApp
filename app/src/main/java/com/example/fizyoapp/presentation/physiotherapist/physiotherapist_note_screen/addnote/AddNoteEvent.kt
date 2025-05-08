package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import com.example.fizyoapp.domain.model.note.NoteColor

sealed class AddNoteEvent {
    data class PatientNameChanged(val patientName: String) : AddNoteEvent()
    data class TitleChanged(val title: String) : AddNoteEvent()
    data class ContentChanged(val content: String) : AddNoteEvent()
    data class ColorChanged(val color: NoteColor) : AddNoteEvent()
    data object SaveNote : AddNoteEvent()
    data object NavigateBack : AddNoteEvent()
}