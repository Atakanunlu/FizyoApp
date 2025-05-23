package com.example.fizyoapp.data.repository.advertisement

import android.net.Uri
import android.util.Log
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
        Log.d("AdvertisementRepo", "Aktif reklamlar alınıyor. Şu anki zaman: ${now.toDate()}")

        val listenerRegistration = advertisementCollection
            .whereEqualTo("isActive", true)
            .whereGreaterThan("expiresAt", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdvertisementRepo", "Reklamlar yüklenirken hata: ${error.message}", error)
                    trySend(Resource.Error("Reklamlar yüklenirken hata oluştu: ${error.message}"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val advertisements = snapshot.documents.mapNotNull { doc ->
                            try {
                                val ad = doc.toObject(Advertisement::class.java)
                                Log.d("AdvertisementRepo", "Reklam verisi: ${doc.data}")
                                ad?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.e("AdvertisementRepo", "Reklam dönüştürme hatası: ${e.message}", e)
                                null
                            }
                        }

                        Log.d("AdvertisementRepo", "Toplam aktif reklam sayısı: ${advertisements.size}")
                        advertisements.forEach { ad ->
                            Log.d("AdvertisementRepo", "Aktif Reklam: ID=${ad.id}, PhysioID=${ad.physiotherapistId}, İmage=${ad.imageUrl}, Bitiş=${ad.expiresAt.toDate()}")
                        }

                        trySend(Resource.Success(advertisements))
                    } catch (e: Exception) {
                        Log.e("AdvertisementRepo", "Reklamları işlerken hata: ${e.message}", e)
                        trySend(Resource.Error("Reklamlar işlenirken hata oluştu: ${e.message}"))
                    }
                } else {
                    Log.d("AdvertisementRepo", "Aktif reklam bulunamadı (snapshot null)")
                    trySend(Resource.Success(emptyList()))
                }
            }

        awaitClose {
            Log.d("AdvertisementRepo", "Reklam listener'ı kapatılıyor")
            listenerRegistration.remove()
        }
    }

    override fun getAdvertisementById(id: String): Flow<Resource<Advertisement>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("AdvertisementRepo", "Reklam getiriliyor, ID: $id")
            val document = advertisementCollection.document(id).get().await()
            if (document.exists()) {
                val advertisement = document.toObject(Advertisement::class.java)?.copy(id = document.id)
                if (advertisement != null) {
                    Log.d("AdvertisementRepo", "Reklam bulundu: $advertisement")
                    emit(Resource.Success(advertisement))
                } else {
                    Log.e("AdvertisementRepo", "Reklam null olarak dönüştürüldü")
                    emit(Resource.Error("Reklam bilgileri alınamadı"))
                }
            } else {
                Log.e("AdvertisementRepo", "Reklam bulunamadı, ID: $id")
                emit(Resource.Error("Reklam bulunamadı"))
            }
        } catch (e: Exception) {
            Log.e("AdvertisementRepo", "Reklam getirme hatası: ${e.message}", e)
            emit(Resource.Error("Reklam alınırken hata oluştu: ${e.message}"))
        }
    }

    override fun getAdvertisementsByPhysiotherapistId(physiotherapistId: String): Flow<Resource<List<Advertisement>>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("AdvertisementRepo", "Fizyoterapist reklamları getiriliyor, ID: $physiotherapistId")
            val querySnapshot = advertisementCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .get()
                .await()

            val advertisements = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Advertisement::class.java)?.copy(id = doc.id)
            }

            Log.d("AdvertisementRepo", "Fizyoterapist reklam sayısı: ${advertisements.size}")
            emit(Resource.Success(advertisements))
        } catch (e: Exception) {
            Log.e("AdvertisementRepo", "Fizyoterapist reklamları alınırken hata: ${e.message}", e)
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
            Log.d("AdvertisementRepo", "Reklam oluşturuluyor. PhysioID: $physiotherapistId, Desc: $description")

            // Upload image to storage
            val storageRef = storage.reference
                .child("advertisements")
                .child(physiotherapistId)
                .child("${UUID.randomUUID()}.jpg")

            val uploadTask = storageRef.putFile(imageUri).await()
            val imageUrl = uploadTask.storage.downloadUrl.await().toString()

            Log.d("AdvertisementRepo", "Görsel yüklendi: $imageUrl")

            // Create advertisement
            val now = Timestamp.now()
            val calendar = Calendar.getInstance()
            calendar.time = now.toDate()
            calendar.add(Calendar.HOUR, 24) // Advertisement will expire in 24 hours
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

            Log.d("AdvertisementRepo", "Kaydedilecek reklam verileri: $advertisementData")

            val documentRef = advertisementCollection.add(advertisementData).await()
            val adId = documentRef.id

            Log.d("AdvertisementRepo", "Reklam başarıyla oluşturuldu. ID: $adId")

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
            Log.e("AdvertisementRepo", "Reklam oluşturma hatası: ${e.message}", e)
            emit(Resource.Error("Reklam oluşturulurken hata oluştu: ${e.message}"))
        }
    }

    override fun deleteAdvertisement(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("AdvertisementRepo", "Reklam siliniyor, ID: $id")
            advertisementCollection.document(id).delete().await()
            Log.d("AdvertisementRepo", "Reklam başarıyla silindi")
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.e("AdvertisementRepo", "Reklam silme hatası: ${e.message}", e)
            emit(Resource.Error("Reklam silinirken hata oluştu: ${e.message}"))
        }
    }

    override fun checkActiveAdvertisementByPhysiotherapist(physiotherapistId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            Log.d("AdvertisementRepo", "Fizyoterapist aktif reklam kontrolü, ID: $physiotherapistId")
            val now = Timestamp.now()
            val querySnapshot = advertisementCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .whereGreaterThan("expiresAt", now)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            val hasActiveAd = !querySnapshot.isEmpty
            Log.d("AdvertisementRepo", "Fizyoterapist aktif reklam durumu: $hasActiveAd")
            emit(Resource.Success(hasActiveAd))
        } catch (e: Exception) {
            Log.e("AdvertisementRepo", "Aktif reklam kontrolü hatası: ${e.message}", e)
            emit(Resource.Error("Aktif reklam kontrolü yapılırken hata oluştu: ${e.message}"))
        }
    }
}