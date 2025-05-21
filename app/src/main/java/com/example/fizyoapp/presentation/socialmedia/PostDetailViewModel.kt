// presentation/socialmedia/PostDetailViewModel.kt
package com.example.fizyoapp.presentation.socialmedia

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.socialmedia.Comment
import com.example.fizyoapp.domain.model.socialmedia.Post
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
    private val deletePostUseCase: DeletePostUseCase, // YENİ
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
            _state.value = _state.value.copy(
                error = "Gönderi bulunamadı"
            )
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
                            _state.value = _state.value.copy(currentUserId = user.id) // YENİ
                            loadUserProfile(user)
                            loadPostAndComments()
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
                when (user.role.name) {
                    "PHYSIOTHERAPIST" -> {
                        getPhysiotherapistProfileUseCase(user.id).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    val profile = result.data
                                    _state.value = _state.value.copy(
                                        currentUserName = "${profile.firstName} ${profile.lastName}",
                                        currentUserPhotoUrl = profile.profilePhotoUrl
                                    )
                                }
                                is Resource.Error -> {
                                    // Profile could not be loaded
                                    Log.e("PostDetailVM", "Fizyoterapist profili yüklenemedi: ${result.message}")
                                }
                                is Resource.Loading -> {
                                    // Loading
                                }
                            }
                        }
                    }
                    else -> {
                        getUserProfileUseCase(user.id).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    val profile = result.data
                                    _state.value = _state.value.copy(
                                        currentUserName = "${profile.firstName} ${profile.lastName}",
                                        currentUserPhotoUrl = profile.profilePhotoUrl
                                    )
                                }
                                is Resource.Error -> {
                                    // Profile could not be loaded
                                    Log.e("PostDetailVM", "Kullanıcı profili yüklenemedi: ${result.message}")
                                }
                                is Resource.Loading -> {
                                    // Loading
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "Profil yükleme hatası", e)
            }
        }
    }

    fun loadPostAndComments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Gönderiyi yükle
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

                            // Yorumları yükle
                            loadComments()
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Gönderi yüklenemedi"
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
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Gönderi yüklenirken bir hata oluştu: ${e.message}"
                )
                Log.e("PostDetailVM", "Post yükleme hatası", e)
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
                            Log.e("PostDetailVM", "Yorumlar yüklenemedi: ${result.message}")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Yorumlar yüklenemedi"
                            )
                        }
                        is Resource.Loading -> {
                            // Already set in loadPostAndComments
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "Yorumlar yüklenirken hata", e)
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
            _state.value = _state.value.copy(
                isCommentLoading = true,
                error = null
            )

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
                            _state.value = _state.value.copy(
                                isCommentLoading = false
                            )
                            loadPostAndComments() // Yorumları ve gönderiyi güncelle
                        }
                        is Resource.Error -> {
                            Log.e("PostDetailVM", "Yorum eklenemedi: ${result.message}", result.exception)
                            _state.value = _state.value.copy(
                                isCommentLoading = false,
                                error = result.message ?: "Yorum eklenemedi"
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                isCommentLoading = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "Yorum ekleme hatası", e)
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
                    // Unlike post
                    unlikePostUseCase(postId, userId).collect { result ->
                        if (result is Resource.Success) {
                            loadPostAndComments() // Beğeni durumunu güncelle
                        } else if (result is Resource.Error) {
                            Log.e("PostDetailVM", "Beğeni kaldırma hatası: ${result.message}")
                        }
                    }
                } else {
                    // Like post
                    likePostUseCase(postId, userId).collect { result ->
                        if (result is Resource.Success) {
                            loadPostAndComments() // Beğeni durumunu güncelle
                        } else if (result is Resource.Error) {
                            Log.e("PostDetailVM", "Beğeni hatası: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PostDetailVM", "Beğeni işlemi hatası", e)
            }
        }
    }

    // YENİ: Gönderi silme fonksiyonu
    fun deletePost() {
        val post = _state.value.post ?: return
        val userId = currentUser?.id ?: return

        // Sadece gönderi sahibi silebilir
        if (post.userId != userId) {
            _state.value = _state.value.copy(
                error = "Bu gönderiyi silme yetkiniz yok"
            )
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