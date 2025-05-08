package com.example.fizyoapp.domain.usecase.note

import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(note: Note): Flow<Resource<Note>> {
        return repository.createNote(note)
    }
}