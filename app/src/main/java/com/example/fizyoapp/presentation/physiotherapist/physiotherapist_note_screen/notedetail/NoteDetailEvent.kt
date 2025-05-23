package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import android.net.Uri

sealed class NoteDetailEvent {
    data class UpdateTextChanged(val text: String) : NoteDetailEvent()
    data object AddUpdate : NoteDetailEvent()
    data class EditUpdate(val index: Int) : NoteDetailEvent()
    data object SaveUpdateEdit : NoteDetailEvent()
    data object CancelUpdateEdit : NoteDetailEvent()
    data class DeleteUpdate(val index: Int) : NoteDetailEvent()
    data object DeleteNote : NoteDetailEvent()

    data class AddImage(val uri: Uri) : NoteDetailEvent()
    data class AddDocument(val uri: Uri) : NoteDetailEvent()
    data object ShowImagePicker : NoteDetailEvent()
    data object ShowDocumentPicker : NoteDetailEvent()

    data class AddImageToUpdate(val uri: Uri) : NoteDetailEvent()
    data class AddDocumentToUpdate(val uri: Uri) : NoteDetailEvent()
    data object ShowUpdateImagePicker : NoteDetailEvent()
    data object ShowUpdateDocumentPicker : NoteDetailEvent()

    data class RemoveTempImage(val index: Int) : NoteDetailEvent()
    data class RemoveTempDocument(val index: Int) : NoteDetailEvent()

    data object NavigateBack : NoteDetailEvent()
}