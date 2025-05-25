package com.example.fizyoapp.data.repository.messagesscreen

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.domain.model.messagesscreen.Message
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {

    suspend fun getChatTreadsForUser(userId: String): Flow<Resource<List<ChatThread>>>

    suspend fun getMessages(
            userId1: String,
            userId2: String): Flow<Resource<List<Message>>>

    suspend fun sendMessage(message: Message): Flow<Resource<Boolean>>

    suspend fun markMessagesAsRead(
            senderId: String,
            receiverId: String
    ): Flow<Resource<Boolean>>


}
