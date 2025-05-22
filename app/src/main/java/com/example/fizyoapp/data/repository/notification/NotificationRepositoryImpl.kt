package com.example.fizyoapp.data.repository.notification

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.notification.SocialMediaNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {
    private val notificationsCollection = firestore.collection("notifications")

    override fun getNotifications(userId: String): Flow<Resource<List<SocialMediaNotification>>> = callbackFlow {
        trySend(Resource.Loading())
        val query = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Bildirimler alınamadı: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val notifications = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(SocialMediaNotification::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(Resource.Success(notifications))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getUnreadNotificationsCount(userId: String): Flow<Resource<Int>> = callbackFlow {
        trySend(Resource.Loading())
        val query = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("isRead", false)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error("Okunmamış bildirim sayısı alınamadı: ${error.message}"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Resource.Success(snapshot.size()))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun markNotificationAsRead(notificationId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Bildirim okundu olarak işaretlenemedi: ${e.message}"))
        }
    }

    override fun markAllNotificationsAsRead(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val query = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in query.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Tüm bildirimler okundu olarak işaretlenemedi: ${e.message}"))
        }
    }

    override fun createNotification(notification: SocialMediaNotification): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            notificationsCollection.add(notification).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Bildirim oluşturulamadı: ${e.message}"))
        }
    }

    override fun deleteNotification(notificationId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            notificationsCollection.document(notificationId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Bildirim silinemedi: ${e.message}"))
        }
    }
}