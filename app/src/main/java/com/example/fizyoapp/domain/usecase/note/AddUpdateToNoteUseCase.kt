package com.example.fizyoapp.domain.usecase.note

import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import com.example.fizyoapp.domain.model.note.NoteUpdate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddUpdateToNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(noteId: String, update: NoteUpdate): Flow<Resource<Note>> {
        return repository.addUpdateToNote(noteId, update)
    }
}