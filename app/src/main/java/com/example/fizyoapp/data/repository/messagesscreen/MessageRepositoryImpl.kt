package com.example.fizyoapp.data.repository.messagesscreen
import android.util.Log
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
    private val physiotherapistProfileRepository: PhysiotherapistProfileRepository
):MessagesRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun getChatTreadsForUser(userId: String): Flow<Resource<List<ChatThread>>> = flow {
        emit(Resource.Loading())
        try {
            val chatThreadsCollection = firestore.collection("chatThreads")
                .whereArrayContains("participantIds", userId)
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()
            val chatThreads = mutableListOf<ChatThread>()
            for (document in chatThreadsCollection.documents) {
                try {
                    val threadData = document.data ?: continue
                    val participantIds = threadData["participantIds"] as? List<String> ?: continue
                    val otherParticipantId = participantIds.firstOrNull { it != userId } ?: continue
                    var otherParticipantName = "Kullanıcı"
                    var otherParticipantPhotoUrl = ""

                    try {
                        val physiotherapistDoc = firestore.collection("physiotherapist").document(otherParticipantId).get().await()
                        if (physiotherapistDoc.exists()) {
                            val physiotherapistProfileDoc = firestore.collection("physiotherapist_profiles").document(otherParticipantId).get().await()
                            if (physiotherapistProfileDoc.exists()) {
                                val firstName = physiotherapistProfileDoc.getString("firstName") ?: ""
                                val lastName = physiotherapistProfileDoc.getString("lastName") ?: ""
                                if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                    otherParticipantName = "FZT. $firstName $lastName".trim()
                                    otherParticipantPhotoUrl = physiotherapistProfileDoc.getString("profilePhotoUrl") ?: ""
                                }
                            } else {
                                val email = physiotherapistDoc.getString("email") ?: ""
                                otherParticipantName = "FZT. " + (email.substringBefore("@") ?: "Kullanıcı")
                            }
                        } else {
                            val userDoc = firestore.collection("user").document(otherParticipantId).get().await()
                            if (userDoc.exists()) {
                                val userProfileDoc = firestore.collection("user_profiles").document(otherParticipantId).get().await()
                                if (userProfileDoc.exists()) {
                                    val firstName = userProfileDoc.getString("firstName") ?: ""
                                    val lastName = userProfileDoc.getString("lastName") ?: ""
                                    if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                        otherParticipantName = "$firstName $lastName".trim()
                                        otherParticipantPhotoUrl = userProfileDoc.getString("profilePhotoUrl") ?: ""
                                    }
                                } else {
                                    val email = userDoc.getString("email") ?: ""
                                    otherParticipantName = email.substringBefore("@")
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }
                    val lastMessage = threadData["lastMessage"] as? String ?: ""
                    val lastMessageTimestamp = (threadData["lastMessageTimestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                    val lastMessageSenderId = threadData["lastMessageSenderId"] as? String ?: ""

                    val unreadCount = when (val count = threadData["unreadCount"]) {
                        is Long -> count.toInt()
                        is Int -> count
                        else -> 0
                    }
                    chatThreads.add(
                        ChatThread(
                            id = document.id,
                            participantIds = participantIds,
                            lastMessage = lastMessage,
                            lastMessageTimestamp = lastMessageTimestamp,
                            unreadCount = unreadCount,
                            otherParticipantName = otherParticipantName,
                            lastMessageSenderId = lastMessageSenderId,
                            otherParticipantPhotoUrl = otherParticipantPhotoUrl
                        )
                    )
                } catch (e: Exception) {
                    Log.e("MessageRepo", "Tekil thread işlerken hata: ${e.message}", e)
                    continue
                }
            }
            val sortedThreads = chatThreads.sortedByDescending { it.lastMessageTimestamp }
            emit(Resource.Success(sortedThreads))
        } catch (e: Exception) {
            Log.e("MessageRepo", "Thread'ler alınırken hata: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Mesaj konuşmaları alınamadı"))
        }
    }

    override suspend fun getMessages(userId1: String, userId2: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())
        try {
            val threadId = getThreadId(userId1, userId2)
            val messagesCollection = firestore.collection("messages")
                .whereEqualTo("threadId", threadId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val messages = messagesCollection.documents.mapNotNull { document ->
                val messageData = document.data ?: return@mapNotNull null
                val senderId = messageData["senderId"] as? String ?: return@mapNotNull null
                val receiverId = messageData["receiverId"] as? String ?: return@mapNotNull null
                val content = messageData["content"] as? String ?: return@mapNotNull null
                val timestamp = (messageData["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                val isRead = messageData["isRead"] as? Boolean ?: false
                val msgThreadId = messageData["threadId"] as? String ?: ""
                val messageType = messageData["messageType"] as? String ?: "text"
                val metadata = messageData["metadata"] as? Map<String, Any> ?: emptyMap()

                // Create message object
                Message(
                    id = document.id,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = timestamp,
                    isRead = isRead,
                    threadId = msgThreadId,
                    messageType = messageType,
                    metadata = metadata
                )
            }

            // Only mark messages as read if current user is the receiver
            val unreadMessagesToMe = messages.filter { !it.isRead && it.receiverId == userId1 }
            if (unreadMessagesToMe.isNotEmpty()) {
                // Start a coroutine to mark messages as read
                markMessagesAsRead(userId2, userId1).collect {}
            }

            emit(Resource.Success(messages))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Mesajlar alınamadı"))
        }
    }.flowOn(Dispatchers.IO)
    override suspend fun sendMessage(message: Message): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val threadId = getThreadId(message.senderId, message.receiverId)
            val messageMap = hashMapOf(
                "id" to message.id,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "isRead" to false,
                "threadId" to threadId,
                "messageType" to message.messageType,
                "metadata" to message.metadata
            ) as Map<String, Any>

            val messageRef = firestore.collection("messages").document()
            messageRef.set(messageMap).await()

            val threadRef = firestore.collection("chatThreads").document(threadId)
            val threadDoc = threadRef.get().await()

            if (threadDoc.exists()) {
                val updateData = hashMapOf(
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp(message.timestamp),
                    "lastMessageSenderId" to message.senderId, // Son mesajı kimin gönderdiğini kaydet
                    "unreadCount" to FieldValue.increment(1)
                ) as Map<String, Any>
                threadRef.update(updateData).await()
            } else {
                val newThreadData = mapOf(
                    "participantIds" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp(message.timestamp),
                    "lastMessageSenderId" to message.senderId, // Son mesajı kimin gönderdiğini kaydet
                    "unreadCount" to 1
                )
                threadRef.set(newThreadData).await()
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Mesaj gönderilemedi"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun markMessagesAsRead(senderId: String, receiverId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val threadId = getThreadId(senderId, receiverId)
            val batch = firestore.batch()

            // Find all unread messages where I am the receiver
            val unreadMessagesQuery = firestore.collection("messages")
                .whereEqualTo("threadId", threadId)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            for (document in unreadMessagesQuery.documents) {
                batch.update(document.reference, "isRead", true)
            }

            // Reset unread count only if there are unread messages
            if (unreadMessagesQuery.documents.isNotEmpty()) {
                val threadRef = firestore.collection("chatThreads").document(threadId)
                batch.update(threadRef, "unreadCount", 0)
            }

            batch.commit().await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.e("MarkAsRead", "Error: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Mesajlar okundu olarak işaretlenemedi"))
        }
    }.flowOn(Dispatchers.IO)

    private fun getThreadId(userId1: String, userId2: String): String {
        val sortedUserIds = listOf(userId1, userId2).sorted()
        return "${sortedUserIds[0]}_${sortedUserIds[1]}"
    }

}