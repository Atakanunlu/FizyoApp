package com.example.fizyoapp.domain.usecase.messagesscreen

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessagesRepository,
    private val authRepository: AuthRepository
) {

    operator fun invoke(content: String, receiverId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        if (content.isBlank() || receiverId.isBlank()) {
            emit(Resource.Error("Geçersiz mesaj veya alıcı"))
            return@flow
        }

        authRepository.getCurrentUser().collect { authResult ->
            if (authResult is Resource.Success && authResult.data?.user != null) {
                val currentUserId = authResult.data.user.id

                val threadId = createChatId(currentUserId, receiverId)

                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = Date(),
                    threadId = threadId,
                    messageType = "text" // Varsayılan olarak text tipi
                )

                messageRepository.sendMessage(message).collect { sendResult ->
                    emit(sendResult)
                }
            } else {
                emit(Resource.Error("Oturum açmanız gerekiyor"))
            }
        }
    }

    // Özel mesaj tipleri için yeni metot
    suspend fun sendCustomMessage(
        content: String,
        receiverId: String,
        messageType: String,
        metadata: Map<String, Any> = emptyMap()
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        if (content.isBlank() || receiverId.isBlank()) {
            emit(Resource.Error("Geçersiz mesaj veya alıcı"))
            return@flow
        }

        authRepository.getCurrentUser().collect { authResult ->
            if (authResult is Resource.Success && authResult.data?.user != null) {
                val currentUserId = authResult.data.user.id

                val threadId = createChatId(currentUserId, receiverId)

                val message = Message(
                    id = UUID.randomUUID().toString(), // Benzersiz ID
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = Date(),
                    isRead = false,
                    threadId = threadId,
                    messageType = messageType,
                    metadata = metadata
                )

                messageRepository.sendMessage(message).collect { sendResult ->
                    emit(sendResult)
                }
            } else {
                emit(Resource.Error("Oturum açmanız gerekiyor"))
            }
        }
    }

    private fun createChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }
}