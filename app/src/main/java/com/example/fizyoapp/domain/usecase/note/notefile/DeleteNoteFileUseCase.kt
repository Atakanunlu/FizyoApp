package com.example.fizyoapp.domain.usecase.note.notefile
import android.net.Uri
import com.example.fizyoapp.data.repository.note.notefile.NoteFileRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteNoteFileUseCase @Inject constructor(
    private val noteFileRepository: NoteFileRepository
) {
    operator fun invoke(url: String): Flow<Resource<Unit>> {
        return noteFileRepository.deleteFile(url)
    }
}