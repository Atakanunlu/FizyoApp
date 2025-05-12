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
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    val state: StateFlow<MessageDetailScreenState> = _state.asStateFlow()
    private val userId: String = savedStateHandle.get<String>("userId") ?: ""

    // Hata kontrolü için yardımcı fonksiyon
    private fun shouldShowError(errorMessage: String?): Boolean {
        if (errorMessage == null) return false

        // "Oturum açmanız gerekiyor" veya benzer hataları gizle
        val ignoredErrors = listOf(
            "oturum açmanız gerekiyor",
            "oturum açman",
            "giriş yapmanız gerekiyor",
            "giriş yapman",
            "authentication",
            "yetkilendirme",
            "yetkili değilsiniz",
            "yetki",
            "auth"
        )

        val lowerCaseError = errorMessage.lowercase()
        return !ignoredErrors.any { lowerCaseError.contains(it) }
    }

    init {
        _state.update { it.copy(isInitialLoading = true, error = null) }

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
                                    _state.update { it.copy(
                                        isInitialLoading = false,
                                        error = if (shouldShowError("Geçersiz kullanıcı bilgisi"))
                                            "Geçersiz kullanıcı bilgisi" else null
                                    ) }
                                }
                            }
                        }
                        is Resource.Error -> {
                            val errorMsg = result.message ?: "Oturum bilgisi alınamadı"
                            _state.update { it.copy(
                                isInitialLoading = false,
                                error = if (shouldShowError(errorMsg)) errorMsg else null
                            ) }
                        }
                        is Resource.Loading -> {
                            // Loading durumunda hata mesajını temizle
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Bir hata oluştu: ${e.message}"
                _state.update { it.copy(
                    isInitialLoading = false,
                    error = if (shouldShowError(errorMsg)) errorMsg else null
                ) }
            }
        }
    }

    fun onEvent(event: MessageDetailScreenEvent) {
        when (event) {
            is MessageDetailScreenEvent.MessageTextChanged -> {
                _state.update { it.copy(messageText = event.text) }
            }
            is MessageDetailScreenEvent.SendMessage -> {
                sendMessage()
            }
            is MessageDetailScreenEvent.RefreshMessages -> {
                loadMessages()
            }
            is MessageDetailScreenEvent.StartVideoCall -> {
                _state.update { it.copy(isVideoCallActive = true) }
            }
            is MessageDetailScreenEvent.EndVideoCall -> {
                _state.update { it.copy(isVideoCallActive = false) }
            }
            is MessageDetailScreenEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadMessages() {
        // Oturum durumunu kontrol et
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Oturum yoksa sessizce çık
            _state.update { it.copy(isInitialLoading = false, isLoading = false) }
            return
        }

        viewModelScope.launch {
            getMessagesUseCase(userId).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                messages = result.data ?: emptyList(),
                                isLoading = false,
                                isInitialLoading = false,
                                error = null
                            )
                        }
                        markMessagesAsRead()
                    }
                    is Resource.Error -> {
                        val errorMsg = result.message
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isInitialLoading = false,
                                error = if (shouldShowError(errorMsg)) errorMsg else null
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadUserDetails() {
        // Oturum durumunu kontrol et
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Oturum yoksa sessizce çık
            _state.update { it.copy(isInitialLoading = false) }
            return
        }

        viewModelScope.launch {
            try {
                physiotherapistProfileRepository.getPhysiotherapistProfile(userId).collectLatest { result ->
                    if (result is Resource.Success && result.data != null) {
                        _state.update {
                            it.copy(
                                physiotherapist = result.data,
                                isPhysiotherapist = true,
                                isInitialLoading = false
                            )
                        }
                    } else {
                        userProfileRepository.getUserProfile(userId).collectLatest { userresult ->
                            if (userresult is Resource.Success && userresult.data != null) {
                                _state.update {
                                    it.copy(
                                        user = userresult.data,
                                        isPhysiotherapist = false,
                                        isInitialLoading = false
                                    )
                                }
                            } else if (userresult is Resource.Error) {
                                val errorMsg = userresult.message
                                if (shouldShowError(errorMsg)) {
                                    _state.update { it.copy(
                                        isInitialLoading = false,
                                        error = errorMsg
                                    ) }
                                } else {
                                    _state.update { it.copy(isInitialLoading = false) }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Kullanıcı bilgileri yüklenemedi: ${e.message}"
                if (shouldShowError(errorMsg)) {
                    _state.update { it.copy(
                        isInitialLoading = false,
                        error = errorMsg
                    ) }
                } else {
                    _state.update { it.copy(isInitialLoading = false) }
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = state.value.messageText.trim()
        if (messageText.isEmpty()) return

        // Oturum durumunu kontrol et
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Oturum yoksa sessizce çık, hata gösterme
            return
        }

        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            sendMessageUseCase(messageText, userId).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Loading durumunda işlem yok
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(
                            messageText = "",
                            isSending = false,
                            error = null
                        ) }
                        loadMessages()
                    }
                    is Resource.Error -> {
                        val errorMsg = result.message ?: "Mesaj gönderilirken bir hata oluştu"
                        _state.update { it.copy(
                            isSending = false,
                            error = if (shouldShowError(errorMsg)) errorMsg else null
                        ) }
                    }
                }
            }
        }
    }

    private fun markMessagesAsRead() {
        // Oturum durumunu kontrol et
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Oturum yoksa sessizce çık
            return
        }

        viewModelScope.launch {
            markMessagesAsReadUseCase(userId).collectLatest { }
        }
    }
}