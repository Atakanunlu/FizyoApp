package com.example.fizyoapp.presentation.socialmedia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.socialmedia.Comment
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.*
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
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
class PostDetailViewModel @Inject constructor(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val getCommentsByPostIdUseCase: GetCommentsByPostIdUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""
    private var currentUser: User? = null

    init {
        if (postId.isNotEmpty()) {
            loadCurrentUser()
        } else {
            _state.value = _state.value.copy(error = "Gönderi bulunamadı")
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            currentUser = user
                            _state.value = _state.value.copy(currentUserId = user.id)
                            loadUserProfile(user)
                            loadPostAndComments()
                        } else {
                            _state.value = _state.value.copy(error = "Kullanıcı bulunamadı")
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Kullanıcı bilgisi alınamadı"
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    private fun loadUserProfile(user: User) {
        viewModelScope.launch {
            try {
                when (user.role.name) {
                    "PHYSIOTHERAPIST" -> {
                        getPhysiotherapistProfileUseCase(user.id).collect { result ->
                            if (result is Resource.Success) {
                                val profile = result.data
                                _state.value = _state.value.copy(
                                    currentUserName = "${profile.firstName} ${profile.lastName}",
                                    currentUserPhotoUrl = profile.profilePhotoUrl
                                )
                            }
                        }
                    }
                    else -> {
                        getUserProfileUseCase(user.id).collect { result ->
                            if (result is Resource.Success) {
                                val profile = result.data
                                _state.value = _state.value.copy(
                                    currentUserName = "${profile.firstName} ${profile.lastName}",
                                    currentUserPhotoUrl = profile.profilePhotoUrl
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    fun loadPostAndComments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                getPostByIdUseCase(postId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val post = result.data
                            val isLiked = post.likedBy.contains(currentUser?.id)
                            _state.value = _state.value.copy(
                                post = post,
                                isPostLikedByCurrentUser = isLiked,
                                isLoading = false
                            )
                            loadComments()
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Gönderi yüklenemedi"
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Gönderi yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            try {
                getCommentsByPostIdUseCase(postId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                comments = result.data,
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Yorumlar yüklenemedi"
                            )
                        }
                        is Resource.Loading -> {

                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun addComment() {
        val commentText = _commentText.value.trim()
        val user = currentUser ?: return
        val post = _state.value.post ?: return

        if (commentText.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isCommentLoading = true, error = null)

            try {
                val comment = Comment(
                    postId = postId,
                    userId = user.id,
                    userName = _state.value.currentUserName,
                    userPhotoUrl = _state.value.currentUserPhotoUrl,
                    content = commentText,
                    timestamp = Date(),
                    userRole = user.role.name
                )

                addCommentUseCase(comment).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _commentText.value = ""
                            _state.value = _state.value.copy(isCommentLoading = false)
                            loadPostAndComments()
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isCommentLoading = false,
                                error = result.message ?: "Yorum eklenemedi"
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isCommentLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCommentLoading = false,
                    error = "Yorum eklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun onLikePost() {
        val post = _state.value.post ?: return
        val userId = currentUser?.id ?: return

        viewModelScope.launch {
            try {
                if (post.likedBy.contains(userId)) {
                    unlikePostUseCase(postId, userId).collect { result ->
                        if (result is Resource.Success) {
                            loadPostAndComments()
                        }
                    }
                } else {
                    likePostUseCase(postId, userId).collect { result ->
                        if (result is Resource.Success) {
                            loadPostAndComments()
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    fun deletePost() {
        val post = _state.value.post ?: return
        val userId = currentUser?.id ?: return

        if (post.userId != userId) {
            _state.value = _state.value.copy(error = "Bu gönderiyi silme yetkiniz yok")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                deletePostUseCase(post.id).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                postDeleted = true
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Gönderi silinemedi"
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Gönderi silinirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}