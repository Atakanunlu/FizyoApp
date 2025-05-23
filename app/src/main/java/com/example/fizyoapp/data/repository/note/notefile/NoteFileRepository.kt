package com.example.fizyoapp.data.repository.note.notefile

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow

interface NoteFileRepository {
    fun uploadImage(uri: Uri, path: String): Flow<Resource<String>>
    fun uploadDocument(uri: Uri, path: String): Flow<Resource<String>>
    fun deleteFile(url: String): Flow<Resource<Unit>>
}