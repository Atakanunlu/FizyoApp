package com.example.fizyoapp.data.repository.illnessrecordscreen.radiologicalimagesscreen

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd.RadyolojikGoruntu
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class RadyolojikGoruntuRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : RadyolojikGoruntuRepository {
    private val baseStoragePath = "radiologic_images"

    override suspend fun getRadyolojikGoruntuler(userId: String): Flow<Resource<List<RadyolojikGoruntu>>> = flow {
        emit(Resource.Loading())
        if (userId.isBlank()) {
            emit(Resource.Error("Geçersiz kullanıcı kimliği"))
            return@flow
        }
        try {
            val storageRef = storage.reference.child("$baseStoragePath/$userId")
            val listResult = storageRef.listAll().await()
            val goruntular = mutableListOf<RadyolojikGoruntu>()
            for (item in listResult.items) {
                try {
                    val metadata = item.metadata.await()
                    val title = metadata.getCustomMetadata("title") ?: "Başlıksız"
                    val description = metadata.getCustomMetadata("description") ?: ""
                    val fileType = metadata.getCustomMetadata("fileType") ?: "image"
                    val timestampStr = metadata.getCustomMetadata("timestamp")
                        ?: System.currentTimeMillis().toString()
                    val timestamp = Date(timestampStr.toLong())
                    val fileUrl = item.downloadUrl.await().toString()

                    val thumbnailUrl = if (fileType == "pdf") {
                        ""
                    } else {
                        fileUrl
                    }

                    val goruntu = RadyolojikGoruntu(
                        id = item.name,
                        title = title,
                        description = description,
                        fileUrl = fileUrl,
                        thumbnailUrl = thumbnailUrl,
                        timestamp = timestamp,
                        userId = userId,
                        fileType = fileType
                    )
                    goruntular.add(goruntu)
                } catch (e: Exception) {
                }
            }
            val sortedGoruntular = goruntular.sortedByDescending { it.timestamp }
            emit(Resource.Success(sortedGoruntular))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Radyolojik görüntüler yüklenemedi"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadRadyolojikGoruntu(
        fileUri: Uri,
        title: String,
        description: String,
        userId: String,
        fileType: String
    ): Flow<Resource<RadyolojikGoruntu>> = flow {
        emit(Resource.Loading())
        if (userId.isBlank()) {
            emit(Resource.Error("Geçersiz kullanıcı kimliği"))
            return@flow
        }
        if (fileUri.toString().isBlank()) {
            emit(Resource.Error("Geçersiz dosya"))
            return@flow
        }

        try {
            val timestamp = System.currentTimeMillis()
            val fileExtension = if (fileType == "pdf") "pdf" else "jpg"
            val filename = "radyolojik_${UUID.randomUUID()}.$fileExtension"

            val storageRef = storage.reference.child("$baseStoragePath/$userId/$filename")

            val contentType = if (fileType == "pdf") "application/pdf" else "image/jpeg"

            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata("title", title)
                .setCustomMetadata("description", description)
                .setCustomMetadata("timestamp", timestamp.toString())
                .setCustomMetadata("userId", userId)
                .setCustomMetadata("fileType", fileType)
                .build()

            storageRef.putFile(fileUri, metadata).await()

            val fileUrl = storageRef.downloadUrl.await().toString()

            val createdGoruntu = RadyolojikGoruntu(
                id = filename,
                title = title,
                description = description,
                fileUrl = fileUrl,
                thumbnailUrl = if (fileType == "pdf") "" else fileUrl,
                timestamp = Date(timestamp),
                userId = userId,
                fileType = fileType
            )
            emit(Resource.Success(createdGoruntu))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Dosya yüklenirken bir hata oluştu"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteRadyolojikGoruntu(fileUrl: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        if (fileUrl.isBlank()) {
            emit(Resource.Error("Geçersiz resim URL'si"))
            return@flow
        }
        try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Görüntü silinirken bir hata oluştu"))
        }
    }.flowOn(Dispatchers.IO)
}