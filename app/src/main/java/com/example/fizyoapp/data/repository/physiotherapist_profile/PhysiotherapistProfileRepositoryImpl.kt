package com.example.fizyoapp.data.repository.physiotherapist_profile

import android.net.Uri
import android.util.Log
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.tasks.await

import java.util.Locale

import javax.inject.Inject

class PhysiotherapistProfileRepositoryImpl @Inject constructor() :
    PhysiotherapistProfileRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val physiotherapistProfilesCollection = firestore.collection("physiotherapist_profiles")
    private val physiotherapistCollection = firestore.collection("physiotherapist")

    override fun getPhysiotherapistProfile(userId: String): Flow<Resource<PhysiotherapistProfile>> =
        flow {
            try {
                emit(Resource.Loading())
                val profileDoc = physiotherapistProfilesCollection.document(userId).get().await()

                if (profileDoc.exists()) {
                    val profile = profileDoc.toObject(PhysiotherapistProfile::class.java)
                        ?: throw Exception("Profil verileri dönüştürülemedi.")

                    emit(Resource.Success(profile))
                } else {
                    emit(Resource.Success(PhysiotherapistProfile(userId = userId)))
                }
            } catch (e: Exception) {
                Log.e("PhysiotherapistProfileRepo", "Profil getirme hatası", e)
                emit(Resource.Error(e.message ?: "Fizyoterapist profili alınamadı", e))
            }
        }

    override fun updatePhysiotherapistProfile(profile: PhysiotherapistProfile): Flow<Resource<PhysiotherapistProfile>> =
        flow {
            try {
                emit(Resource.Loading())


                physiotherapistProfilesCollection.document(profile.userId).set(profile).await()


                physiotherapistCollection.document(profile.userId)
                    .update("profileCompleted", profile.isProfileCompleted).await()


                emit(Resource.Success(profile))

            } catch (e: Exception) {
                Log.e("PhysiotherapistProfileRepo", "Profil güncelleme hatası", e)
                emit(Resource.Error(e.message ?: "Fizyoterapist profili güncellenemedi", e))
            }
        }

    override fun checkProfileCompleted(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val physiotherapistDoc = physiotherapistCollection.document(userId).get().await()
            if (physiotherapistDoc.exists()) {
                val isCompleted = physiotherapistDoc.getBoolean("profileCompleted") ?: false
                emit(Resource.Success(isCompleted))
                return@flow
            }


            emit(Resource.Success(false))
        } catch (e: Exception) {
            Log.e("PhysiotherapistProfileRepo", "Profil kontrolü hatası", e)
            emit(Resource.Error(e.message ?: "Fizyoterapist profil kontrolü yapılamadı", e))
        }
    }

    override fun uploadProfilePhoto(
        photoUriString: String,
        userId: String
    ): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val photoUri = Uri.parse(photoUriString)

            // Basit ve tek dosya adı - her zaman üzerine yazar, geçmiş dosyaları biriktirmez
            val fileRef =
                storage.reference.child("physiotherapist_photos/$userId/profile_image.jpg")
            fileRef.putFile(photoUri).await()

            val downloadUrl = fileRef.downloadUrl.await().toString()

            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Resource.Error("Profil fotoğrafı yüklenemedi", e))
        }
    }

    override fun getAllPhysiotherapists(): Flow<Resource<List<PhysiotherapistProfile>>> = flow {
        try {
            emit(Resource.Loading())

            val snapshot = physiotherapistProfilesCollection.get().await()

            val profiles = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(PhysiotherapistProfile::class.java)
                } catch (e: Exception) {
                    null
                }
            }

            if (profiles.isEmpty()) {

                val physiotherapistDocs = physiotherapistCollection.get().await()

                if (physiotherapistDocs.documents.isNotEmpty()) {

                    val simpleProfiles = physiotherapistDocs.documents.mapNotNull { doc ->
                        try {
                            val userId = doc.id


                            PhysiotherapistProfile(
                                userId = userId,
                                firstName = "Fizyoterapist",
                                lastName = "#${userId.takeLast(4)}",
                                phoneNumber = "",
                                city = "",
                                district = "",
                                fullAddress = "",
                                profilePhotoUrl = ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    emit(Resource.Success(simpleProfiles))
                } else {
                    emit(Resource.Success(emptyList()))
                }
            } else {
                emit(Resource.Success(profiles))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Fizyoterapist listesi alınamadı", e))
        }
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    override fun getPhysiotherapistById(physiotherapistId: String): Flow<Resource<PhysiotherapistProfile>> =
        flow {
            try {
                emit(Resource.Loading())

                val profileDoc =
                    physiotherapistProfilesCollection.document(physiotherapistId).get().await()

                if (profileDoc.exists()) {
                    val profile = profileDoc.toObject(PhysiotherapistProfile::class.java)
                        ?: throw Exception("Profil verileri dönüştürülemedi.")

                    emit(Resource.Success(profile))
                } else {
                    throw Exception("Fizyoterapist bulunamadı")
                }
            } catch (e: Exception) {
                Log.e("PhysiotherapistProfileRepo", "Fizyoterapist getirme hatası", e)
                emit(Resource.Error(e.message ?: "Fizyoterapist profili alınamadı", e))
            }
        }

}