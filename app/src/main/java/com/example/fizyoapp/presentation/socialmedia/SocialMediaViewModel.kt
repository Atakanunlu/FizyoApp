// presentation/socialmedia/SocialMediaViewModel.kt
package com.example.fizyoapp.presentation.socialmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetAllPostsUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.LikePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.UnlikePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialMediaViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SocialMediaState())
    val state: StateFlow<SocialMediaState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _currentUser.value = result.data
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

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            getAllPostsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            posts = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Gönderiler yüklenemedi"
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

    fun onLikePost(postId: String) {
        val userId = _currentUser.value?.id ?: return
        val post = _state.value.posts.find { it.id == postId } ?: return

        viewModelScope.launch {
            if (post.likedBy.contains(userId)) {
                // Unlike post
                unlikePostUseCase(postId, userId).collect { result ->
                    if (result is Resource.Success) {
                        loadPosts() // Yeniden yükle
                    }
                }
            } else {
                // Like post
                likePostUseCase(postId, userId).collect { result ->
                    if (result is Resource.Success) {
                        loadPosts() // Yeniden yükle
                    }
                }
            }
        }
    }
}