// presentation/socialmedia/CreatePostViewModel.kt
package com.example.fizyoapp.presentation.socialmedia

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.CreatePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            loadUserProfile(user)
                        } else {
                            _state.value = _state.value.copy(
                                error = "Kullanıcı bulunamadı"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Kullanıcı bilgisi alınamadı"
                        )
                    }
                    is Resource.Loading -> {
                        // Loading state
                    }
                }
            }
        }
    }

    private fun loadUserProfile(user: User) {
        viewModelScope.launch {
            try {
                getPhysiotherapistProfileUseCase(user.id).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val profile = result.data
                            _state.value = _state.value.copy(
                                currentUserId = user.id,
                                currentUserName = "${profile.firstName} ${profile.lastName}",
                                currentUserPhotoUrl = profile.profilePhotoUrl,
                                currentUserRole = user.role.name
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Profil bilgisi alınamadı"
                            )
                        }
                        is Resource.Loading -> {
                            // Loading state
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Profil bilgisi alınırken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun onEvent(event: CreatePostEvent) {
        when (event) {
            is CreatePostEvent.ContentChanged -> {
                _state.value = _state.value.copy(content = event.content)
            }
            is CreatePostEvent.MediaAdded -> {
                _state.value = _state.value.copy(
                    mediaUris = _state.value.mediaUris + event.uris
                )
            }
            is CreatePostEvent.MediaRemoved -> {
                _state.value = _state.value.copy(
                    mediaUris = _state.value.mediaUris.filter { it != event.uri }
                )
            }
            is CreatePostEvent.CreatePost -> {
                if (_state.value.content.isBlank() && _state.value.mediaUris.isEmpty()) {
                    _state.value = _state.value.copy(
                        error = "Gönderi içeriği veya medya ekleyin"
                    )
                    return
                }
                createPost()
            }
        }
    }

    private fun createPost() {
        val currentState = _state.value

        if (currentState.currentUserId.isBlank() ||
            currentState.currentUserName.isBlank() ||
            currentState.currentUserRole.isBlank()
        ) {
            _state.value = _state.value.copy(
                error = "Kullanıcı bilgileri alınamadı"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val post = Post(
                    userId = currentState.currentUserId,
                    userName = currentState.currentUserName,
                    userPhotoUrl = currentState.currentUserPhotoUrl,
                    content = currentState.content,
                    timestamp = Date(),
                    userRole = currentState.currentUserRole
                )

                Log.d("CreatePostVM", "Gönderi oluşturuluyor: ${currentState.mediaUris.size} medya ile")

                createPostUseCase(post, currentState.mediaUris).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = null
                            )
                            _uiEvent.send(UiEvent.NavigateBack)
                        }
                        is Resource.Error -> {
                            Log.e("CreatePostVM", "Gönderi oluşturma hatası: ${result.message}", result.exception)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Gönderi oluşturulamadı"
                            )
                            _uiEvent.send(UiEvent.ShowError(result.message ?: "Gönderi oluşturulamadı"))
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CreatePostVM", "Gönderi oluşturma exception: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Gönderi oluşturulurken bir hata oluştu: ${e.message}"
                )
                _uiEvent.send(UiEvent.ShowError("Gönderi oluşturulurken bir hata oluştu: ${e.message}"))
            }
        }
    }

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}