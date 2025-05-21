package com.example.fizyoapp.presentation.social.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.social.FollowUserUseCase
import com.example.fizyoapp.domain.usecase.social.GetFollowingPostsUseCase
import com.example.fizyoapp.domain.usecase.social.GetGeneralPostsUseCase
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialFeedViewModel @Inject constructor(
    private val getGeneralPostsUseCase: GetGeneralPostsUseCase,
    private val getFollowingPostsUseCase: GetFollowingPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SocialFeedState())
    val state: StateFlow<SocialFeedState> = _state.asStateFlow()

    private val _uiEvent = Channel<SocialFeedUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkCurrentUser()
        loadGeneralPosts()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val user = result.data
                            if (user != null) {
                                _state.value = _state.value.copy(
                                    isPhysiotherapist = user.role == UserRole.PHYSIOTHERAPIST
                                )
                                Log.d("SocialFeedViewModel", "User role detected: ${user.role}")
                            } else {
                                Log.d("SocialFeedViewModel", "User is null")
                                _state.value = _state.value.copy(
                                    errorMessage = "Kullanıcı bilgisi alınamadı"
                                )
                            }
                        }
                        is Resource.Error -> {
                            Log.e("SocialFeedViewModel", "Error getting user: ${result.message}")
                            _state.value = _state.value.copy(
                                errorMessage = result.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("SocialFeedViewModel", "Exception in checkCurrentUser", e)
                _state.value = _state.value.copy(
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun onEvent(event: SocialFeedEvent) {
        when (event) {
            is SocialFeedEvent.LoadGeneralPosts -> {
                _state.value = _state.value.copy(showFollowingFeed = false)
                loadGeneralPosts()
            }
            is SocialFeedEvent.LoadFollowingPosts -> {
                _state.value = _state.value.copy(showFollowingFeed = true)
                loadFollowingPosts()
            }
            is SocialFeedEvent.ToggleLike -> toggleLike(event.postId)
            is SocialFeedEvent.ShowComments -> showComments(event.postId)
            is SocialFeedEvent.ToggleFollow -> toggleFollow(event.authorId)
            is SocialFeedEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEvent.send(SocialFeedUiEvent.NavigateToProfile(event.userId))
                }
            }
            is SocialFeedEvent.NavigateToCreatePost -> {
                viewModelScope.launch {
                    Log.d("SocialFeedViewModel", "NavigateToCreatePost event received")
                    _uiEvent.send(SocialFeedUiEvent.NavigateToCreatePost)
                }
            }
        }
    }

    private fun loadGeneralPosts() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                getGeneralPostsUseCase()
                    .catch { e ->
                        Log.e("SocialFeedViewModel", "Error loading general posts", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Gönderiler yüklenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    generalPosts = result.data,
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
                Log.e("SocialFeedViewModel", "Uncaught exception in loadGeneralPosts", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun loadFollowingPosts() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                getFollowingPostsUseCase()
                    .catch { e ->
                        Log.e("SocialFeedViewModel", "Error loading following posts", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Takip edilen gönderiler yüklenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    followingPosts = result.data,
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
                Log.e("SocialFeedViewModel", "Uncaught exception in loadFollowingPosts", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                // Find the post in current lists
                val generalPostIndex = _state.value.generalPosts.indexOfFirst { it.id == postId }
                val followingPostIndex = _state.value.followingPosts.indexOfFirst { it.id == postId }

                // Determine if the post is currently liked
                val isCurrentlyLiked = if (generalPostIndex >= 0) {
                    _state.value.generalPosts[generalPostIndex].isLikedByCurrentUser
                } else if (followingPostIndex >= 0) {
                    _state.value.followingPosts[followingPostIndex].isLikedByCurrentUser
                } else {
                    return@launch
                }

                // Optimistically update the UI
                if (generalPostIndex >= 0) {
                    val post = _state.value.generalPosts[generalPostIndex]
                    val updatedPost = post.copy(
                        isLikedByCurrentUser = !post.isLikedByCurrentUser,
                        likeCount = if (post.isLikedByCurrentUser) post.likeCount - 1 else post.likeCount + 1
                    )
                    val updatedList = _state.value.generalPosts.toMutableList()
                    updatedList[generalPostIndex] = updatedPost
                    _state.value = _state.value.copy(generalPosts = updatedList)
                }

                if (followingPostIndex >= 0) {
                    val post = _state.value.followingPosts[followingPostIndex]
                    val updatedPost = post.copy(
                        isLikedByCurrentUser = !post.isLikedByCurrentUser,
                        likeCount = if (post.isLikedByCurrentUser) post.likeCount - 1 else post.likeCount + 1
                    )
                    val updatedList = _state.value.followingPosts.toMutableList()
                    updatedList[followingPostIndex] = updatedPost
                    _state.value = _state.value.copy(followingPosts = updatedList)
                }

                // Call the appropriate API
                if (isCurrentlyLiked) {
                    unlikePostUseCase(postId)
                        .catch { e ->
                            Log.e("SocialFeedViewModel", "Error unliking post", e)
                            // Could revert UI updates here if needed
                        }
                        .collect { /* result handling if needed */ }
                } else {
                    likePostUseCase(postId)
                        .catch { e ->
                            Log.e("SocialFeedViewModel", "Error liking post", e)
                            // Could revert UI updates here if needed
                        }
                        .collect { /* result handling if needed */ }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("SocialFeedViewModel", "Uncaught exception in toggleLike", e)
            }
        }
    }

    private fun showComments(postId: String) {
        viewModelScope.launch {
            _uiEvent.send(SocialFeedUiEvent.NavigateToComments(postId))
        }
    }

    private fun toggleFollow(authorId: String) {
        viewModelScope.launch {
            try {
                // Check if already following the author in any post
                val isFollowing = isFollowingAuthor(authorId)

                // Call the appropriate API
                if (isFollowing) {
                    unfollowUserUseCase(authorId)
                        .catch { e ->
                            Log.e("SocialFeedViewModel", "Error unfollowing user", e)
                        }
                        .collect { result ->
                            if (result is Resource.Success) {
                                // Reload feeds after successful unfollow
                                loadGeneralPosts()
                                loadFollowingPosts()
                            }
                        }
                } else {
                    followUserUseCase(authorId)
                        .catch { e ->
                            Log.e("SocialFeedViewModel", "Error following user", e)
                        }
                        .collect { result ->
                            if (result is Resource.Success) {
                                // Reload feeds after successful follow
                                loadGeneralPosts()
                                loadFollowingPosts()
                            }
                        }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("SocialFeedViewModel", "Uncaught exception in toggleFollow", e)
            }
        }
    }

    private fun isFollowingAuthor(authorId: String): Boolean {
        // Bu örnekte basit bir yaklaşım kullanılıyor
        // Gerçek uygulamada bu bilgiyi repository'den alabilirsiniz
        // Şimdilik varsayılan olarak takip etmediğimizi kabul edelim
        return false
    }

    sealed class SocialFeedUiEvent {
        data class NavigateToComments(val postId: String) : SocialFeedUiEvent()
        data class NavigateToProfile(val userId: String) : SocialFeedUiEvent()
        data object NavigateToCreatePost : SocialFeedUiEvent()
    }
}