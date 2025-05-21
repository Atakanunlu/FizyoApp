package com.example.fizyoapp.domain.usecase.messagesscreen

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessagesRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(otherUserId:String):Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())

        authRepository.getCurrentUser().collect{authResult ->
            if(authResult is Resource.Success && authResult.data?.user != null){
                val currentUserId=authResult.data.user.id
                messageRepository.getMessages(currentUserId,otherUserId).collect{messagesResult ->
                    emit(messagesResult)
                }
            }
            else{
                emit(Resource.Error("oturum açmanız gerekiyor!"))
            }
        }
    }
}