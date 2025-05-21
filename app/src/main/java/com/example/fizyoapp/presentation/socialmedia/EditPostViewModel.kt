// presentation/socialmedia/EditPostViewModel.kt
package com.example.fizyoapp.presentation.socialmedia

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetPostByIdUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.UpdatePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EditPostState())
    val state: StateFlow<EditPostState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    init {
        if (postId.isNotEmpty()) {
            loadPost()
        } else {
            _state.value = _state.value.copy(
                error = "Gönderi ID'si bulunamadı"
            )
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { userResult ->
                when (userResult) {
                    is Resource.Success -> {
                        val currentUser = userResult.data
                        if (currentUser != null) {
                            _state.value = _state.value.copy(
                                userId = currentUser.id
                            )

                            // Gönderiyi yükle
                            getPostByIdUseCase(postId).collect { postResult ->
                                when (postResult) {
                                    is Resource.Success -> {
                                        val post = postResult.data

                                        // Yalnızca gönderi sahibi düzenleyebilir
                                        if (post.userId != currentUser.id) {
                                            _state.value = _state.value.copy(
                                                error = "Bu gönderiyi düzenleme yetkiniz yok",
                                                isLoading = false
                                            )
                                            _uiEvent.send(UiEvent.NavigateBack)
                                            return@collect
                                        }

                                        _state.value = _state.value.copy(
                                            postId = post.id,
                                            content = post.content,
                                            existingMediaUrls = post.mediaUrls,
                                            existingMediaTypes = post.mediaTypes,
                                            userName = post.userName,
                                            userPhotoUrl = post.userPhotoUrl,
                                            isLoading = false
                                        )
                                    }
                                    is Resource.Error -> {
                                        _state.value = _state.value.copy(
                                            error = postResult.message ?: "Gönderi yüklenemedi",
                                            isLoading = false
                                        )
                                    }
                                    is Resource.Loading -> {
                                        _state.value = _state.value.copy(
                                            isLoading = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = userResult.message ?: "Kullanıcı bilgileri alınamadı",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: EditPostEvent) {
        when (event) {
            is EditPostEvent.ContentChanged -> {
                _state.value = _state.value.copy(
                    content = event.content
                )
            }
            is EditPostEvent.MediaAdded -> {
                _state.value = _state.value.copy(
                    newMediaUris = _state.value.newMediaUris + event.uris
                )
            }
            is EditPostEvent.NewMediaRemoved -> {
                _state.value = _state.value.copy(
                    newMediaUris = _state.value.newMediaUris.filter { it != event.uri }
                )
            }
            is EditPostEvent.ExistingMediaRemoved -> {
                val index = _state.value.existingMediaUrls.indexOf(event.uri)

                // Bu URL'yi ve karşılık gelen medya tipini kaldır
                val updatedUrls = _state.value.existingMediaUrls.toMutableList()
                val updatedTypes = _state.value.existingMediaTypes.toMutableList()

                if (index != -1) {
                    updatedUrls.removeAt(index)
                    if (index < updatedTypes.size) {
                        updatedTypes.removeAt(index)
                    }
                }

                _state.value = _state.value.copy(
                    existingMediaUrls = updatedUrls,
                    existingMediaTypes = updatedTypes
                )
            }
            is EditPostEvent.UpdatePost -> {
                updatePost()
            }
        }
    }

    private fun updatePost() {
        val currentState = _state.value

        if (currentState.content.isBlank() && currentState.existingMediaUrls.isEmpty() && currentState.newMediaUris.isEmpty()) {
            _state.value = _state.value.copy(
                error = "İçerik veya medya ekleyin"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            updatePostUseCase(
                postId = currentState.postId,
                content = currentState.content,
                existingMediaUrls = currentState.existingMediaUrls,
                existingMediaTypes = currentState.existingMediaTypes,
                newMediaUris = currentState.newMediaUris
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isUpdateSuccessful = true
                        )
                        _uiEvent.send(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Gönderi güncellenemedi"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}