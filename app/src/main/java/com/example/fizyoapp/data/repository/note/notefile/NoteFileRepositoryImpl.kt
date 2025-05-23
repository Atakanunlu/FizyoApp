package com.example.fizyoapp.data.repository.note.notefile

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class NoteFileRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : NoteFileRepository {

    override fun uploadImage(uri: Uri, path: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val fileName = "${UUID.randomUUID()}.jpg"
            val fullPath = "$path/$fileName"
            val storageRef = storage.reference.child(fullPath)

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Görsel yüklenirken hata oluştu", e))
        }
    }

    override fun uploadDocument(uri: Uri, path: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val fileName = "${UUID.randomUUID()}.pdf"
            val fullPath = "$path/$fileName"
            val storageRef = storage.reference.child(fullPath)

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Belge yüklenirken hata oluştu", e))
        }
    }

    override fun deleteFile(url: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val path = url.substringAfter("files%2F").substringBefore("?")
            val storageRef = storage.reference.child(path)

            storageRef.delete().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Dosya silinirken hata oluştu", e))
        }
    }
}