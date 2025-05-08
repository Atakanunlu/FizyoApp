package com.example.fizyoapp.domain.usecase.note

import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(noteId: String): Flow<Resource<Unit>> {
        return repository.deleteNote(noteId)
    }
}