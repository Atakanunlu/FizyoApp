package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.example.fizyoapp.domain.usecase.messagesscreen.GetChatThreadsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesScreenViewModel @Inject constructor(
    private val getChatThreadsUseCase: GetChatThreadsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(MessagesScreenState(isInitialLoading = true))
    val state: StateFlow<MessagesScreenState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MessagesScreenUiEvent>()
    val uiEvent: SharedFlow<MessagesScreenUiEvent> = _uiEvent.asSharedFlow()

    private fun shouldShowError(errorMessage: String?): Boolean {
        if (errorMessage == null) return false
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
        loadChatThreads()
    }


    fun onEvent(event: MessagesScreenEvent) {
        when (event) {
            is MessagesScreenEvent.NavigateToMessageDetail -> {
                viewModelScope.launch {
                    _uiEvent.emit(MessagesScreenUiEvent.NavigateToMessageDetail(event.userId))
                }
            }
            is MessagesScreenEvent.RefreshChatThreads -> {
                loadChatThreads()
            }
            is MessagesScreenEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadChatThreads() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            _state.update { it.copy(isLoading = false, isInitialLoading = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            _state.update { it.copy(currentUserId = currentUid) }

            getChatThreadsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                chatThreads = result.data ?: emptyList(),
                                isLoading = false,
                                isInitialLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        val errorMsg = result.message ?: "Mesaj listesi yüklenirken bir hata oluştu"
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
}