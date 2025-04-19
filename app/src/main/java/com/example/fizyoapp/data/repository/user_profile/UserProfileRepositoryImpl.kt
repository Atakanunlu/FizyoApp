package com.example.fizyoapp.data.repository.user_profile

import android.net.Uri
import android.util.Log
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor() : UserProfileRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val userProfilesCollection = firestore.collection("user_profiles")
    private val userCollection = firestore.collection("user")

    override fun getUserProfile(userId: String): Flow<Resource<UserProfile>> = flow {
        try {
            emit(Resource.Loading())
            val profileDoc = userProfilesCollection.document(userId).get().await()

            if (profileDoc.exists()) {
                val userProfile = profileDoc.toObject(UserProfile::class.java)
                    ?: throw Exception("Profil verileri dönüştürülemedi.")

                emit(Resource.Success(userProfile))
            } else {
                emit(Resource.Success(UserProfile(userId = userId)))
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Profil getirme hatası", e)
            emit(Resource.Error(e.message ?: "Kullanıcı profili alınamadı", e))
        }
    }

    override fun updateUserProfile(userProfile: UserProfile): Flow<Resource<UserProfile>> = flow {
        try {
            emit(Resource.Loading())

            userProfilesCollection.document(userProfile.userId).set(userProfile).await()


            userCollection.document(userProfile.userId)
                .update("profileCompleted", userProfile.isProfileCompleted).await()

            emit(Resource.Success(userProfile))

        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Profil güncelleme hatası", e)
            emit(Resource.Error(e.message ?: "Kullanıcı profili güncellenemedi", e))
        }
    }

    override fun checkProfileCompleted(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val userDoc = userCollection.document(userId).get().await()
            if (userDoc.exists()) {
                val isCompleted = userDoc.getBoolean("profileCompleted") ?: false
                emit(Resource.Success(isCompleted))
                return@flow
            }
            emit(Resource.Success(false))
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Profil kontrolü hatası", e)
            emit(Resource.Error(e.message ?: "Kullanıcı profil kontrolü yapılamadı", e))
        }
    }

    override fun uploadProfilePhoto(
        photoUriString: String,
        userId: String
    ): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())


            val photoUri = Uri.parse(photoUriString)

            val fileRef =
                storage.reference.child("user_photos/$userId/profile_image_${UUID.randomUUID()}.jpg")

            try {
                fileRef.putFile(photoUri).await()

                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .setCustomMetadata("userId", userId)
                    .setCustomMetadata("userType", "user")
                    .setCustomMetadata("uploadDate", Date().toString())
                    .build()

                fileRef.updateMetadata(metadata).await()

                val downloadUrl = fileRef.downloadUrl.await().toString()
                val fileData = mapOf(
                    "fileName" to fileRef.name,
                    "storagePath" to fileRef.path,
                    "downloadUrl" to downloadUrl,
                    "uploadTime" to FieldValue.serverTimestamp(),
                    "userId" to userId,
                    "fileType" to "profile_photo"
                )

                userProfilesCollection.document(userId)
                    .update("profilePhotoData", fileData)
                    .await()

                Log.d("UserProfileRepository", "Upload successful, URL: $downloadUrl")
                emit(Resource.Success(downloadUrl))
            } catch (e: Exception) {
                Log.e("UserProfileRepository", "Storage upload failed", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Fotoğraf yükleme hatası", e)
            emit(Resource.Error(e.message ?: "Profil fotoğrafı yüklenemedi", e))
        }
    }
}