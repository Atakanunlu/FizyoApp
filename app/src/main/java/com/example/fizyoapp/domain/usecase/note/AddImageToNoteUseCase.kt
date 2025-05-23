package com.example.fizyoapp.domain.usecase.note

import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddImageToNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteId: String, imageUrl: String): Flow<Resource<Note>> {
        return noteRepository.addImageToNote(noteId, imageUrl)
    }
}
