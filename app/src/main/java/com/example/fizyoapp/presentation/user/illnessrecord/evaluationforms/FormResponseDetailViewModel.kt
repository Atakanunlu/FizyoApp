package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen.EvaluationFormRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormResponseDetailState(
    val response: FormResponse? = null,
    val questionMap: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionError: String? = null,
    val successMessage: String? = null,
    val currentUserId: String = "",
    val recentThreads: List<ChatThread> = emptyList()
)

@HiltViewModel
class FormResponseDetailViewModel @Inject constructor(
    private val evaluationFormRepository: EvaluationFormRepository,
    private val messagesRepository: MessagesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FormResponseDetailState())
    val state: StateFlow<FormResponseDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                _state.update { it.copy(currentUserId = currentUser.uid) }
                loadRecentThreads(currentUser.uid)
            }
        }
    }

    fun loadResponse(responseId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            evaluationFormRepository.getFormResponseById(responseId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val response = result.data
                        if (response != null) {
                            _state.update {
                                it.copy(
                                    response = response,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            loadFormDetails(response.formId)
                        } else {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Form yanıtı bulunamadı"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Form yanıtı yüklenemedi"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadFormDetails(formId: String) {
        viewModelScope.launch {
            evaluationFormRepository.getEvaluationFormById(formId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val form = result.data
                        if (form != null) {
                            val questionMap = form.questions.associate { question ->
                                question.id to question.text
                            }
                            _state.update {
                                it.copy(questionMap = questionMap)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private suspend fun loadRecentThreads(userId: String) {
        messagesRepository.getChatTreadsForUser(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(recentThreads = result.data ?: emptyList())
                    }
                }
                else -> {}
            }
        }
    }

    fun shareResponse(responseId: String, receiverId: String) {
        viewModelScope.launch {
            evaluationFormRepository.shareFormResponse(responseId, receiverId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(successMessage = "Değerlendirme formu başarıyla paylaşıldı")
                        }
                        delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(actionError = result.message ?: "Form paylaşılırken bir hata oluştu")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(actionError = null) }
    }
}