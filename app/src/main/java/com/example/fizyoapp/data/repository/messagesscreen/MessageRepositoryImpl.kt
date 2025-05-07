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
):MessageRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override suspend fun getChatTreadsForUser(userId: String): Flow<Resource<List<ChatThread>>> = flow {
        emit(Resource.Loading())
        try {
            val chatThreadsCollection = firestore.collection("chatThreads")
                .whereArrayContains("participantIds", userId)
                .get()
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

                                Log.d("MessageRepo", "User profile belge var mı: ${userProfileDoc.exists()}")

                                if (userProfileDoc.exists()) {
                                    val firstName = userProfileDoc.getString("firstName") ?: ""
                                    val lastName = userProfileDoc.getString("lastName") ?: ""

                                    Log.d("MessageRepo", "UserProfile firstName: $firstName, lastName: $lastName")

                                    if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                        otherParticipantName = "$firstName $lastName".trim()
                                        otherParticipantPhotoUrl = userProfileDoc.getString("profilePhotoUrl") ?: ""
                                    }
                                } else {
                                    val email = userDoc.getString("email") ?: ""

                                    Log.d("MessageRepo", "User email: $email")

                                    otherParticipantName = email.substringBefore("@")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MessageRepo", "Kullanıcı bilgileri alınırken hata: ${e.message}", e)
                    }

                    val lastMessage = threadData["lastMessage"] as? String ?: ""
                    val lastMessageTimestamp = (threadData["lastMessageTimestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
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
    override suspend fun getMessages(
        userId1: String,
        userId2: String
    ): Flow<Resource<List<Message>>> = flow {

        emit(Resource.Loading())
        try {
            val threadId = getThreadId(userId1, userId2)
            val messagesCollection=firestore.collection("messages")
                .whereEqualTo("threadId",threadId)
                .orderBy("timestamp",Query.Direction.ASCENDING)
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

                Message(
                    id = document.id,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = timestamp,
                    isRead = isRead,
                    threadId = msgThreadId
                )
            }

            if (messages.any { !it.isRead && it.receiverId == userId1 }) {
                markMessagesAsRead(userId2, userId1).collect { /* sonucu göz ardı et */ }
            }
            emit(Resource.Success(messages))

        }
        catch (e:Exception){
            emit(Resource.Error(e.localizedMessage ?: "Mesajlar alınamadı"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendMessage(message: Message): Flow<Resource<Boolean>> = flow{
        emit(Resource.Loading())
        try {
            val threadId = getThreadId(message.senderId, message.receiverId)

            // HashMap'i Any tipinde oluştur
            val messageData = mapOf(
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "timestamp" to com.google.firebase.Timestamp(message.timestamp),
                "isRead" to false,
                "threadId" to threadId
            ) as Map<String, Any>

            val messageRef = firestore.collection("messages").document()
            messageRef.set(messageData).await()

            val threadRef = firestore.collection("chatThreads").document(threadId)
            val threadDoc = threadRef.get().await()

            if (threadDoc.exists()) {
                // Var olan thread'i güncelle
                // updateData'yı MutableMap olarak oluştur ve FieldValue'yi Any olarak ekle
                val updateData = mutableMapOf<String, Any>(
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp(message.timestamp)
                )

                // Eğer mesaj alıcıdan gönderene değilse unreadCount artır
                if (threadDoc.get("participantIds.0") == message.receiverId) {
                    // FieldValue'yi Any olarak ekliyoruz
                    updateData["unreadCount"] = FieldValue.increment(1) as Any
                }

                threadRef.update(updateData).await()
            } else {
                // Yeni thread oluştur
                val newThreadData = mapOf(
                    "participantIds" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp(message.timestamp),
                    "unreadCount" to 1
                ) as Map<String, Any>

                threadRef.set(newThreadData).await()
            }

            emit(Resource.Success(true))
        }
        catch (e: Exception){
            emit(Resource.Error(e.localizedMessage ?: "Mesajlar alınamadı"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun markMessagesAsRead(
        senderId: String,
        receiverId: String
    ): Flow<Resource<Boolean>> = flow{
        emit(Resource.Loading())

        try {
            val threadId=getThreadId(senderId,receiverId)
            val batch = firestore.batch()

            val unreadMessagesQuery = firestore.collection("messages")
                .whereEqualTo("threadId",threadId)
                .whereEqualTo("senderId",senderId)
                .whereEqualTo("receiverId",receiverId)
                .whereEqualTo("isRead",false)
                .get()
                .await()

            for (document in unreadMessagesQuery.documents){
                batch.update(document.reference,"isRead",true)
            }

            val threadRef = firestore.collection("chatThreads").document(threadId)
            val threadDoc = threadRef.get().await()

            if(threadDoc.exists()){
                batch.update(threadRef,"unreadCount",0)
            }
            batch.commit().await()
            emit(Resource.Success(true))

        }catch (e:Exception){
            emit(Resource.Error(e.localizedMessage ?: "Mesajlar okundu olarak işaretlenemedi"))

        }
    }.flowOn(Dispatchers.IO)

    private fun getThreadId(userId1: String, userId2: String): String {
        // Tutarlı thread ID oluşturmak için kullanıcı ID'lerini alfabetik olarak sırala
        val sortedUserIds = listOf(userId1, userId2).sorted()
        return "${sortedUserIds[0]}_${sortedUserIds[1]}"
    }
}