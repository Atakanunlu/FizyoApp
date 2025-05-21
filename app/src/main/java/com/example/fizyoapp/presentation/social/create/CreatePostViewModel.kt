package com.example.fizyoapp.presentation.social.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.social.CreatePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    private val _uiEvent = Channel<CreatePostUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkIfPhysiotherapist()
    }

    private fun checkIfPhysiotherapist() {
        viewModelScope.launch {
            try {
                val currentUserResult = getCurrentUserUseCase().first()
                if (currentUserResult is Resource.Success && currentUserResult.data?.role != UserRole.PHYSIOTHERAPIST) {
                    _state.value = _state.value.copy(
                        errorMessage = "Sadece fizyoterapistler gönderi paylaşabilir"
                    )
                    _uiEvent.send(CreatePostUiEvent.NavigateBack)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CreatePostViewModel", "Error checking user role", e)
                _state.value = _state.value.copy(
                    errorMessage = "Kullanıcı rolü kontrol edilirken hata oluştu"
                )
                _uiEvent.send(CreatePostUiEvent.NavigateBack)
            }
        }
    }

    fun onEvent(event: CreatePostEvent) {
        when (event) {
            is CreatePostEvent.ContentChanged -> {
                _state.value = _state.value.copy(content = event.content)
            }
            is CreatePostEvent.AddMedia -> {
                val currentMediaUris = _state.value.mediaUris.toMutableList()
                currentMediaUris.add(event.uri)
                _state.value = _state.value.copy(mediaUris = currentMediaUris)
            }
            is CreatePostEvent.RemoveMedia -> {
                val currentMediaUris = _state.value.mediaUris.toMutableList()
                if (event.index in currentMediaUris.indices) {
                    currentMediaUris.removeAt(event.index)
                    _state.value = _state.value.copy(mediaUris = currentMediaUris)
                }
            }
            is CreatePostEvent.PublishPost -> {
                publishPost()
            }
            is CreatePostEvent.CancelPost -> {
                viewModelScope.launch {
                    _uiEvent.send(CreatePostUiEvent.NavigateBack)
                }
            }
        }
    }

    private fun publishPost() {
        viewModelScope.launch {
            try {
                val content = _state.value.content.trim()
                if (content.isEmpty()) {
                    _state.value = _state.value.copy(
                        errorMessage = "Gönderi içeriği boş olamaz"
                    )
                    return@launch
                }

                _state.value = _state.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                createPostUseCase(content, _state.value.mediaUris)
                    .catch { e ->
                        Log.e("CreatePostViewModel", "Error creating post", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Gönderi oluşturulurken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    isPostPublished = true,
                                    errorMessage = null
                                )
                                _uiEvent.send(CreatePostUiEvent.NavigateBack)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    errorMessage = result.message
                                )
                            }
                            is Resource.Loading -> {
                                _state.value = _state.value.copy(isLoading = true)
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CreatePostViewModel", "Uncaught exception in publishPost", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    sealed class CreatePostUiEvent {
        data object NavigateBack : CreatePostUiEvent()
    }
}