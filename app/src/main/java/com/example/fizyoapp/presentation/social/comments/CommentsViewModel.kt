package com.example.fizyoapp.presentation.social.comments

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.social.AddCommentUseCase
import com.example.fizyoapp.domain.usecase.social.GetCommentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CommentsState())
    val state: StateFlow<CommentsState> = _state.asStateFlow()

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    init {
        if (postId.isNotEmpty()) {
            loadComments()
        } else {
            _state.value = _state.value.copy(
                errorMessage = "Gönderi kimliği bulunamadı"
            )
        }
    }

    fun onEvent(event: CommentsEvent) {
        when (event) {
            is CommentsEvent.LoadComments -> loadComments()
            is CommentsEvent.CommentContentChanged -> {
                _state.value = _state.value.copy(commentContent = event.content)
            }
            is CommentsEvent.SubmitComment -> submitComment()
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                getCommentsUseCase(postId)
                    .catch { e ->
                        Log.e("CommentsViewModel", "Error loading comments", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Yorumlar yüklenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    comments = result.data,
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
                Log.e("CommentsViewModel", "Uncaught exception in loadComments", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun submitComment() {
        val content = _state.value.commentContent.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isSubmitting = true,
                    errorMessage = null
                )

                addCommentUseCase(postId, content)
                    .catch { e ->
                        Log.e("CommentsViewModel", "Error submitting comment", e)
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            errorMessage = "Yorum eklenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isSubmitting = false,
                                    commentContent = "", // Clear the input field
                                    errorMessage = null
                                )
                                loadComments() // Reload comments to include the new one
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isSubmitting = false,
                                    errorMessage = result.message
                                )
                            }
                            is Resource.Loading -> {
                                _state.value = _state.value.copy(isSubmitting = true)
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CommentsViewModel", "Uncaught exception in submitComment", e)
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                )
            }
        }
    }
}