package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.messagesscreen.GetChatThreadsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesScreenViewModel@Inject constructor(
    private val getChatThreadsUseCase: GetChatThreadsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesScreenState())
    val state: StateFlow<MessagesScreenState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MessagesScreenUiEvent>()
    val uiEvent: SharedFlow<MessagesScreenUiEvent> = _uiEvent.asSharedFlow()

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
        }
    }
    private fun loadChatThreads() {
        viewModelScope.launch {
            getChatThreadsUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                chatThreads = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Mesaj listesi yüklenirken bir hata oluştu"
                            )
                        }
                    }
                }
            }
        }
    }


}
