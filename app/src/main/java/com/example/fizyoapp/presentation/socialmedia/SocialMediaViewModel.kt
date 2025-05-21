// presentation/socialmedia/SocialMediaViewModel.kt
package com.example.fizyoapp.presentation.socialmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetAllPostsUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.LikePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.UnlikePostUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialMediaViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SocialMediaState())
    val state: StateFlow<SocialMediaState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _physiotherapistProfile = MutableStateFlow<PhysiotherapistProfile?>(null)
    val physiotherapistProfile: StateFlow<PhysiotherapistProfile?> = _physiotherapistProfile.asStateFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _currentUser.value = result.data
                        result.data?.let { loadUserInfo(it) }
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

    fun loadUserInfo() {
        val user = _currentUser.value
        if (user != null) {
            loadUserInfo(user)
        }
    }

    private fun loadUserInfo(user: User) {
        viewModelScope.launch {
            when (user.role) {
                UserRole.USER -> {
                    getUserProfileUseCase(user.id).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _userProfile.value = result.data
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Kullanıcı profili alınamadı"
                                )
                            }
                            is Resource.Loading -> {
                                // Loading state
                            }
                        }
                    }
                }
                UserRole.PHYSIOTHERAPIST -> {
                    getPhysiotherapistProfileUseCase(user.id).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _physiotherapistProfile.value = result.data
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = result.message ?: "Fizyoterapist profili alınamadı"
                                )
                            }
                            is Resource.Loading -> {
                                // Loading state
                            }
                        }
                    }
                }
                else -> {}
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