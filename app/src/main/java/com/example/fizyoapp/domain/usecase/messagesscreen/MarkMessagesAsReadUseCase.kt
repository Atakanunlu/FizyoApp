package com.example.fizyoapp.domain.usecase.messagesscreen

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val messageRepository: MessagesRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(senderId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        if (senderId.isBlank()) {
            emit(Resource.Error("Geçersiz gönderen ID'si"))
            return@flow
        }

        authRepository.getCurrentUser().collect { authResult ->
            if (authResult is Resource.Success && authResult.data?.user != null) {
                val currentUserId = authResult.data.user.id
                messageRepository.markMessagesAsRead(senderId, currentUserId).collect { markResult ->
                    emit(markResult)
                }
            } else {
                emit(Resource.Error("Oturum açmanız gerekiyor"))
            }
        }
    }
}