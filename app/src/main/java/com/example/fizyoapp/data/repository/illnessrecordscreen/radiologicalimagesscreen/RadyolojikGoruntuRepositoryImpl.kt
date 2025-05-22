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
                    // Meta verileri al
                    val metadata = item.metadata.await()
                    // Custom metadata'dan bilgileri çek
                    val title = metadata.getCustomMetadata("title") ?: "Başlıksız Görüntü"
                    val description = metadata.getCustomMetadata("description") ?: ""
                    val timestampStr = metadata.getCustomMetadata("timestamp")
                        ?: System.currentTimeMillis().toString()
                    val timestamp = Date(timestampStr.toLong())

                    // Görüntü URL'sini al
                    val imageUrl = item.downloadUrl.await().toString()

                    // RadyolojikGoruntu nesnesini oluştur
                    val goruntu = RadyolojikGoruntu(
                        id = item.name, // Dosya adını ID olarak kullan
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        thumbnailUrl = imageUrl, // Aynı URL'yi thumbnail olarak kullan
                        timestamp = timestamp,
                        userId = userId
                    )
                    goruntular.add(goruntu)
                } catch (e: Exception) {
                    // Tekil görüntü hatası tüm süreci durdurmaz, sonraki görüntülere devam ederiz
                }
            }

            // Görüntüleri timestamp'e göre sırala (yeniden eskiye)
            val sortedGoruntular = goruntular.sortedByDescending { it.timestamp }
            emit(Resource.Success(sortedGoruntular))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Radyolojik görüntüler yüklenemedi"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadRadyolojikGoruntu(
        imageUri: Uri,
        title: String,
        description: String,
        userId: String
    ): Flow<Resource<RadyolojikGoruntu>> = flow {
        emit(Resource.Loading())

        if (userId.isBlank()) {
            emit(Resource.Error("Geçersiz kullanıcı kimliği"))
            return@flow
        }

        if (imageUri.toString().isBlank()) {
            emit(Resource.Error("Geçersiz resim dosyası"))
            return@flow
        }

        try {
            // Dosya adı oluştur
            val timestamp = System.currentTimeMillis()
            val filename = "radyolojik_${UUID.randomUUID()}.jpg"

            // Storage referansı oluştur
            val storageRef = storage.reference.child("$baseStoragePath/$userId/$filename")

            // Metadata oluştur
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("title", title)
                .setCustomMetadata("description", description)
                .setCustomMetadata("timestamp", timestamp.toString())
                .setCustomMetadata("userId", userId)
                .build()

            // Dosyayı yükle (metadata ile birlikte)
            try {
                val uploadTask = storageRef.putFile(imageUri, metadata).await()
            } catch (e: Exception) {
                emit(Resource.Error("Dosya yükleme hatası: ${e.message}"))
                return@flow
            }

            // İndirme URL'sini al
            var imageUrl = ""
            try {
                imageUrl = storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                emit(Resource.Error("URL alma hatası: ${e.message}"))
                return@flow
            }

            // Başarılı sonuç döndür
            val createdGoruntu = RadyolojikGoruntu(
                id = filename,
                title = title,
                description = description,
                imageUrl = imageUrl,
                thumbnailUrl = imageUrl,
                timestamp = Date(timestamp),
                userId = userId
            )
            emit(Resource.Success(createdGoruntu))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Görüntü yüklenirken bir hata oluştu"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteRadyolojikGoruntu(imageUrl: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        if (imageUrl.isBlank()) {
            emit(Resource.Error("Geçersiz resim URL'si"))
            return@flow
        }

        try {
            // URL'den Firebase Storage referansını al
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            // Dosyayı sil
            storageRef.delete().await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Görüntü silinirken bir hata oluştu"))
        }
    }.flowOn(Dispatchers.IO)
}