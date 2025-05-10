package com.example.fizyoapp.domain.usecase.note

import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteNoteUpdateUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(noteId: String, updateIndex: Int): Flow<Resource<Note>> {
        return repository.deleteNoteUpdate(noteId, updateIndex)
    }
}