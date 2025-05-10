package com.example.fizyoapp.domain.usecase.note

import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.note.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesByPhysiotherapistIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(physiotherapistId: String): Flow<Resource<List<Note>>> {
        return repository.getNotesByPhysiotherapistId(physiotherapistId)
    }
}