package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.messagesscreen.GetMessagesUseCase
import com.example.fizyoapp.domain.usecase.messagesscreen.MarkMessagesAsReadUseCase
import com.example.fizyoapp.domain.usecase.messagesscreen.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MessagesDetailScreenViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val physiotherapistProfileRepository: PhysiotherapistProfileRepository,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(MessageDetailScreenState())
    val state:StateFlow<MessageDetailScreenState> =_state.asStateFlow()
    private val userId:String = savedStateHandle.get<String>("userId") ?:""

    init {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data?.user != null) {
                                val currentUserId = result.data.user.id
                                _state.update { it.copy(currentUserId = currentUserId) }
                                if (userId.isNotEmpty()) {
                                    loadMessages()
                                    loadUserDetails()
                                } else {
                                    _state.update { it.copy(error = "Geçersiz kullanıcı bilgisi") }
                                }
                            }
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(error = result.message ?: "Oturum bilgisi alınamadı") }
                        }
                        is Resource.Loading -> {

                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Bir hata oluştu: ${e.message}") }
            }
        }
    }

    fun onEvent(event: MessageDetailScreenEvent){
        when(event){
            is MessageDetailScreenEvent.MessageTextChanged ->{
                _state.update { it.copy(messageText = event.text) }
            }
            is MessageDetailScreenEvent.SendMessage ->{
                sendMessage()
            }
            is MessageDetailScreenEvent.RefreshMessages ->{
                loadMessages()
            }
        }
    }

    private fun loadMessages(){
        viewModelScope.launch {
            getMessagesUseCase(userId).collectLatest {result ->
                when(result){
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                messages = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                        markMessagesAsRead()
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadUserDetails(){
        viewModelScope.launch {
            physiotherapistProfileRepository.getPhysiotherapistProfile(userId).collectLatest { result ->
                if(result is Resource.Success && result.data != null){
                    _state.update {
                        it.copy(
                            physiotherapist = result.data,
                            isPhysiotherapist = true
                        )
                    }
                }
                else{
                    userProfileRepository.getUserProfile(userId).collectLatest { userresult->
                        if (userresult is Resource.Success && userresult.data != null){
                            _state.update { it.copy(
                                user = userresult.data,
                                isPhysiotherapist = false
                            ) }
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage(){
        val messageText=state.value.messageText.trim()
        if (messageText.isEmpty()) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            sendMessageUseCase(messageText,userId).collectLatest { result ->
                when(result){
                    is Resource.Loading ->{
                    }
                    is Resource.Success ->{
                        _state.update { it.copy(
                            messageText = "",
                            isSending =false,
                            error = null
                        ) }
                        loadMessages()
                    }
                    is Resource.Error ->{
                        _state.update { it.copy(
                            isSending = false,
                            error = result.message ?: "Mesaj gönderilirken bir hata oluştu"
                        ) }
                    }
                }
            }
        }
    }

    private fun markMessagesAsRead() {
        viewModelScope.launch {
            markMessagesAsReadUseCase(userId).collectLatest {  }
        }
    }
}