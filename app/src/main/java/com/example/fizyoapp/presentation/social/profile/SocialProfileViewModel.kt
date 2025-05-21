package com.example.fizyoapp.presentation.social.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.social.FollowUserUseCase
import com.example.fizyoapp.domain.usecase.social.GetSocialProfileUseCase
import com.example.fizyoapp.domain.usecase.social.GetUserPostsUseCase
import com.example.fizyoapp.domain.usecase.social.LikePostUseCase
import com.example.fizyoapp.domain.usecase.social.UnfollowUserUseCase
import com.example.fizyoapp.domain.usecase.social.UnlikePostUseCase
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
class SocialProfileViewModel @Inject constructor(
    private val getSocialProfileUseCase: GetSocialProfileUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SocialProfileState())
    val state: StateFlow<SocialProfileState> = _state.asStateFlow()

    private val _uiEvent = Channel<SocialProfileUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val userId: String = savedStateHandle.get<String>("userId") ?: ""

    init {
        if (userId.isNotEmpty()) {
            checkIfCurrentUser()
            loadProfile()
            loadUserPosts()
        } else {
            _state.value = _state.value.copy(
                errorMessage = "Kullanıcı kimliği bulunamadı"
            )
        }
    }

    private fun checkIfCurrentUser() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase()
                    .catch { e ->
                        Log.e("SocialProfileViewModel", "Error checking current user", e)
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val user = result.data
                                if (user != null) {
                                    _state.value = _state.value.copy(
                                        isCurrentUser = user.id == userId
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("SocialProfileViewModel", "Uncaught exception in checkIfCurrentUser", e)
            }
        }
    }

    fun onEvent(event: SocialProfileEvent) {
        when (event) {
            is SocialProfileEvent.LoadProfile -> loadProfile()
            is SocialProfileEvent.LoadUserPosts -> loadUserPosts()
            is SocialProfileEvent.ToggleFollow -> toggleFollow()
            is SocialProfileEvent.ToggleLike -> toggleLike(event.postId)
            is SocialProfileEvent.ShowComments -> {
                viewModelScope.launch {
                    _uiEvent.send(SocialProfileUiEvent.NavigateToComments(event.postId))
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                getSocialProfileUseCase(userId)
                    .catch { e ->
                        Log.e("SocialProfileViewModel", "Error loading profile", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Profil yüklenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    profile = result.data,
                                    errorMessage = null
                                )
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
                Log.e("SocialProfileViewModel", "Uncaught exception in loadProfile", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun loadUserPosts() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                getUserPostsUseCase(userId)
                    .catch { e ->
                        Log.e("SocialProfileViewModel", "Error loading user posts", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Kullanıcı gönderileri yüklenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    posts = result.data,
                                    errorMessage = null
                                )
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
                Log.e("SocialProfileViewModel", "Uncaught exception in loadUserPosts", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun toggleFollow() {
        val profile = _state.value.profile ?: return

        viewModelScope.launch {
            try {
                val isCurrentlyFollowed = profile.isFollowedByCurrentUser

                // Optimistically update UI
                _state.value = _state.value.copy(
                    profile = profile.copy(
                        isFollowedByCurrentUser = !isCurrentlyFollowed,
                        followersCount = if (isCurrentlyFollowed) profile.followersCount - 1 else profile.followersCount + 1
                    )
                )

                // Call the appropriate API
                if (isCurrentlyFollowed) {
                    unfollowUserUseCase(userId)
                        .catch { e ->
                            Log.e("SocialProfileViewModel", "Error unfollowing user", e)
                            // Revert on error
                            _state.value = _state.value.copy(
                                profile = _state.value.profile?.copy(
                                    isFollowedByCurrentUser = isCurrentlyFollowed,
                                    followersCount = profile.followersCount
                                )
                            )
                        }
                        .collect { /* result handling if needed */ }
                } else {
                    followUserUseCase(userId)
                        .catch { e ->
                            Log.e("SocialProfileViewModel", "Error following user", e)
                            // Revert on error
                            _state.value = _state.value.copy(
                                profile = _state.value.profile?.copy(
                                    isFollowedByCurrentUser = isCurrentlyFollowed,
                                    followersCount = profile.followersCount
                                )
                            )
                        }
                        .collect { /* result handling if needed */ }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("SocialProfileViewModel", "Uncaught exception in toggleFollow", e)
            }
        }
    }

    private fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                // Find the post in current posts
                val postIndex = _state.value.posts.indexOfFirst { it.id == postId }
                if (postIndex < 0) return@launch

                val post = _state.value.posts[postIndex]
                val isCurrentlyLiked = post.isLikedByCurrentUser

                // Optimistically update the UI
                val updatedPost = post.copy(
                    isLikedByCurrentUser = !isCurrentlyLiked,
                    likeCount = if (isCurrentlyLiked) post.likeCount - 1 else post.likeCount + 1
                )
                val updatedList = _state.value.posts.toMutableList()
                updatedList[postIndex] = updatedPost
                _state.value = _state.value.copy(posts = updatedList)

                // Call the appropriate API
                if (isCurrentlyLiked) {
                    unlikePostUseCase(postId)
                        .catch { e ->
                            Log.e("SocialProfileViewModel", "Error unliking post", e)
                            // Could revert UI updates here if needed
                        }
                        .collect { /* result handling if needed */ }
                } else {
                    likePostUseCase(postId)
                        .catch { e ->
                            Log.e("SocialProfileViewModel", "Error liking post", e)
                            // Could revert UI updates here if needed
                        }
                        .collect { /* result handling if needed */ }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("SocialProfileViewModel", "Uncaught exception in toggleLike", e)
            }
        }
    }

    sealed class SocialProfileUiEvent {
        data class NavigateToComments(val postId: String) : SocialProfileUiEvent()
    }
}