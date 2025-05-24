package com.example.fizyoapp.data.repository.advertisement

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.advertisement.Advertisement
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class AdvertisementRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AdvertisementRepository {
    private val advertisementCollection = firestore.collection("advertisements")

    override fun getActiveAdvertisements(): Flow<Resource<List<Advertisement>>> = callbackFlow {
        trySend(Resource.Loading())
        val now = Timestamp.now()
        val listenerRegistration = advertisementCollection
            .whereEqualTo("isActive", true)
            .whereGreaterThan("expiresAt", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error("Reklamlar yüklenirken hata oluştu: ${error.message}"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val advertisements = snapshot.documents.mapNotNull { doc ->
                            try {
                                val ad = doc.toObject(Advertisement::class.java)
                                ad?.copy(id = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Resource.Success(advertisements))
                    } catch (e: Exception) {
                        trySend(Resource.Error("Reklamlar işlenirken hata oluştu: ${e.message}"))
                    }
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun getAdvertisementById(id: String): Flow<Resource<Advertisement>> = flow {
        emit(Resource.Loading())
        try {
            val document = advertisementCollection.document(id).get().await()
            if (document.exists()) {
                val advertisement = document.toObject(Advertisement::class.java)?.copy(id = document.id)
                if (advertisement != null) {
                    emit(Resource.Success(advertisement))
                } else {
                    emit(Resource.Error("Reklam bilgileri alınamadı"))
                }
            } else {
                emit(Resource.Error("Reklam bulunamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Reklam alınırken hata oluştu: ${e.message}"))
        }
    }

    override fun getAdvertisementsByPhysiotherapistId(physiotherapistId: String): Flow<Resource<List<Advertisement>>> = flow {
        emit(Resource.Loading())
        try {
            val querySnapshot = advertisementCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .get()
                .await()
            val advertisements = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Advertisement::class.java)?.copy(id = doc.id)
            }
            emit(Resource.Success(advertisements))
        } catch (e: Exception) {
            emit(Resource.Error("Reklamlar alınırken hata oluştu: ${e.message}"))
        }
    }

    override fun createAdvertisement(
        physiotherapistId: String,
        imageUri: Uri,
        description: String,
        paymentId: String
    ): Flow<Resource<Advertisement>> = flow {
        emit(Resource.Loading())
        try {
            val storageRef = storage.reference
                .child("advertisements")
                .child(physiotherapistId)
                .child("${UUID.randomUUID()}.jpg")
            val uploadTask = storageRef.putFile(imageUri).await()
            val imageUrl = uploadTask.storage.downloadUrl.await().toString()
            val now = Timestamp.now()

            // Süreyi 3 dakika olarak ayarlayalım (test için)
            val calendar = Calendar.getInstance()
            calendar.time = now.toDate()
            calendar.add(Calendar.MINUTE, 3) // 24 saat yerine 3 dakika

            val expiresAt = Timestamp(calendar.time)
            val advertisementData = hashMapOf(
                "physiotherapistId" to physiotherapistId,
                "imageUrl" to imageUrl,
                "description" to description,
                "paymentId" to paymentId,
                "createdAt" to now,
                "expiresAt" to expiresAt,
                "isActive" to true
            )
            val documentRef = advertisementCollection.add(advertisementData).await()
            val adId = documentRef.id
            val advertisement = Advertisement(
                id = adId,
                physiotherapistId = physiotherapistId,
                imageUrl = imageUrl,
                description = description,
                paymentId = paymentId,
                createdAt = now,
                expiresAt = expiresAt,
                isActive = true
            )
            emit(Resource.Success(advertisement))
        } catch (e: Exception) {
            emit(Resource.Error("Reklam oluşturulurken hata oluştu: ${e.message}"))
        }
    }

    override fun deleteAdvertisement(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            advertisementCollection.document(id).delete().await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Reklam silinirken hata oluştu: ${e.message}"))
        }
    }

    override fun checkActiveAdvertisementByPhysiotherapist(physiotherapistId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val now = Timestamp.now()
            val querySnapshot = advertisementCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .whereGreaterThan("expiresAt", now)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            val hasActiveAd = !querySnapshot.isEmpty
            emit(Resource.Success(hasActiveAd))
        } catch (e: Exception) {
            emit(Resource.Error("Aktif reklam kontrolü yapılırken hata oluştu: ${e.message}"))
        }
    }
}