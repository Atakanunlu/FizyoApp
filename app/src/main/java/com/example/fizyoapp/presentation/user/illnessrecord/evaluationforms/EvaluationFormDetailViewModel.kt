package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen.EvaluationFormRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.EvaluationForm
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.QuestionType
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class FormDetailState(
    val form: EvaluationForm? = null,
    val answers: Map<String, String> = emptyMap(),
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val actionError: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EvaluationFormDetailViewModel @Inject constructor(
    private val evaluationFormRepository: EvaluationFormRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FormDetailState())
    val state: StateFlow<FormDetailState> = _state.asStateFlow()

    fun loadForm(formId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            evaluationFormRepository.getEvaluationFormById(formId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                form = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Form detayları yüklenemedi"
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

    fun updateAnswer(questionId: String, answer: String) {
        _state.update {
            it.copy(
                answers = it.answers.toMutableMap().apply {
                    put(questionId, answer)
                }
            )
        }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun submitForm() {
        viewModelScope.launch {
            val currentForm = state.value.form ?: return@launch
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch

            val unansweredRequiredQuestions = currentForm.questions.filter { question ->
                question.required && (state.value.answers[question.id] == null || state.value.answers[question.id]!!.isBlank())
            }

            if (unansweredRequiredQuestions.isNotEmpty()) {
                _state.update {
                    it.copy(actionError = "Lütfen tüm gerekli soruları yanıtlayın")
                }
                return@launch
            }

            _state.update { it.copy(isSaving = true) }

            val totalPoints = calculateScore(currentForm)
            val responseId = UUID.randomUUID().toString()

            val formResponse = FormResponse(
                id = responseId,
                formId = currentForm.id,
                userId = currentUser.uid,
                answers = state.value.answers,
                dateCompleted = Date(),
                score = totalPoints,
                maxScore = currentForm.maxScore,
                notes = state.value.notes,
                title = currentForm.title
            )

            evaluationFormRepository.saveFormResponse(formResponse).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isSuccess = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                actionError = result.message ?: "Form gönderilirken bir hata oluştu"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }

    private fun calculateScore(form: EvaluationForm): Int {
        var score = 0
        form.questions.forEach { question ->
            val answer = state.value.answers[question.id] ?: ""
            when (question.type) {
                QuestionType.SCALE -> {
                    answer.toIntOrNull()?.let { score += it }
                }
                QuestionType.YES_NO -> {
                    if (answer == "Evet") {
                        score += 1
                    }
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    val optionIndex = question.options.indexOf(answer)
                    if (optionIndex >= 0) {
                        score += (question.options.size - optionIndex)
                    }
                }
                else -> {}
            }
        }
        return score
    }

    fun dismissError() {
        _state.update { it.copy(actionError = null) }
    }
}