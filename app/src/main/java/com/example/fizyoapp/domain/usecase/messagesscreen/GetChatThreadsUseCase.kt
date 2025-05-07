package com.example.fizyoapp.domain.usecase.messagesscreen

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessageRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetChatThreadsUseCase @Inject constructor(private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository) {

    operator fun invoke():Flow<Resource<List<ChatThread>>> = flow {
        emit(Resource.Loading())

        authRepository.getCurrentUser().collect{ authResult ->
            if(authResult is Resource.Success && authResult.data?.user != null){
                val userId = authResult.data.user.id
                messageRepository.getChatTreadsForUser(userId).collect{threadResult ->
                    emit(threadResult)
                }
            }
            else{
                emit(Resource.Error("oturum açmanız gerekiyor!"))
            }
        }
    }
}