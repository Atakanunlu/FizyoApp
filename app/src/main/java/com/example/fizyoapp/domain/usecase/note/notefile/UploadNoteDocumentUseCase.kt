package com.example.fizyoapp.domain.usecase.note.notefile

import android.net.Uri
import com.example.fizyoapp.data.repository.note.notefile.NoteFileRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadNoteDocumentUseCase @Inject constructor(
    private val noteFileRepository: NoteFileRepository
) {
    operator fun invoke(uri: Uri, path: String): Flow<Resource<String>> {
        return noteFileRepository.uploadDocument(uri, path)
    }
}