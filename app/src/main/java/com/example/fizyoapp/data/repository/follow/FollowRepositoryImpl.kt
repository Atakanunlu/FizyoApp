package com.example.fizyoapp.data.repository.follow

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.follow.FollowRelation
import com.example.fizyoapp.domain.model.notification.NotificationType
import com.example.fizyoapp.domain.model.notification.SocialMediaNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FollowRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FollowRepository {
    private val followsCollection = firestore.collection("follows")

    override fun followPhysiotherapist(followerId: String, followerRole: String, followedId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val existingFollowQuery = followsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followedId", followedId)
                .get()
                .await()

            if (existingFollowQuery.documents.isNotEmpty()) {
                emit(Resource.Success(Unit))
                return@flow
            }

            val followRelation = FollowRelation(
                followerId = followerId,
                followedId = followedId,
                followerRole = followerRole,
                followedRole = "PHYSIOTHERAPIST",
                timestamp = Date()
            )
            followsCollection.add(followRelation).await()

            try {
                val followerName: String
                var followerPhotoUrl = ""

                if (followerRole == "USER") {
                    val userProfileDoc = firestore.collection("user_profiles").document(followerId).get().await()
                    followerName = "${userProfileDoc.getString("firstName") ?: ""} ${userProfileDoc.getString("lastName") ?: ""}"
                    followerPhotoUrl = userProfileDoc.getString("profilePhotoUrl") ?: ""
                } else {
                    val physiotherapistProfileDoc = firestore.collection("physiotherapist_profiles").document(followerId).get().await()
                    followerName = "${physiotherapistProfileDoc.getString("firstName") ?: ""} ${physiotherapistProfileDoc.getString("lastName") ?: ""}"
                    followerPhotoUrl = physiotherapistProfileDoc.getString("profilePhotoUrl") ?: ""
                }

                val notification = SocialMediaNotification(
                    recipientId = followedId,
                    senderId = followerId,
                    senderRole = followerRole,
                    type = NotificationType.FOLLOW,
                    contentId = "",
                    contentText = "$followerName sizi takip etmeye başladı",
                    isRead = false,
                    timestamp = Date()
                )
                firestore.collection("notifications").add(notification)
            } catch (e: Exception) {

            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Takip edilemedi: ${e.message}"))
        }
    }

    override fun unfollowPhysiotherapist(followerId: String, followedId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val query = followsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followedId", followedId)
                .get()
                .await()

            if (query.documents.isEmpty()) {
                emit(Resource.Error("Takip ilişkisi bulunamadı"))
                return@flow
            }

            for (document in query.documents) {
                document.reference.delete().await()
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Takipten çıkılamadı: ${e.message}"))
        }
    }

    override fun isFollowing(followerId: String, followedId: String): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.Loading())
        val query = followsCollection
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followedId", followedId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Takip durumu kontrol edilemedi: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val isFollowing = !snapshot.isEmpty
                trySend(Resource.Success(isFollowing))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getFollowersCount(physiotherapistId: String): Flow<Resource<Int>> = callbackFlow {
        trySend(Resource.Loading())
        val query = followsCollection.whereEqualTo("followedId", physiotherapistId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Takipçi sayısı alınamadı: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Resource.Success(snapshot.size()))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getFollowingCount(userId: String): Flow<Resource<Int>> = callbackFlow {
        trySend(Resource.Loading())
        val query = followsCollection.whereEqualTo("followerId", userId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Takip edilen sayısı alınamadı: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Resource.Success(snapshot.size()))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getFollowers(physiotherapistId: String): Flow<Resource<List<FollowRelation>>> = callbackFlow {
        trySend(Resource.Loading())
        val query = followsCollection
            .whereEqualTo("followedId", physiotherapistId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Takipçiler alınamadı: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val followers = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(FollowRelation::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(Resource.Success(followers))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getFollowing(userId: String): Flow<Resource<List<FollowRelation>>> = callbackFlow {
        trySend(Resource.Loading())
        val query = followsCollection
            .whereEqualTo("followerId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Takip edilenler alınamadı: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val following = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(FollowRelation::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(Resource.Success(following))
            }
        }
        awaitClose { listener.remove() }
    }
}