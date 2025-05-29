package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen.EvaluationFormRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EvaluationFormsViewModel @Inject constructor(
    private val evaluationFormRepository: EvaluationFormRepository,
    private val messagesRepository: MessagesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EvaluationFormsState())
    val state: StateFlow<EvaluationFormsState> = _state.asStateFlow()

    private val auth=FirebaseAuth.getInstance()


    fun onEvent(event: EvaluationFormsEvent) {
        when (event) {
            is EvaluationFormsEvent.RefreshData -> loadData()
            is EvaluationFormsEvent.DismissError -> _state.update { it.copy(actionError = null) }
            is EvaluationFormsEvent.ShareFormResponse -> shareFormResponse(event.responseId, event.userId)
            is EvaluationFormsEvent.DeleteFormResponse -> deleteFormResponse(event.responseId)

        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Oturum açmanız gerekiyor"
                    )
                }
                return@launch
            }
            val userId = currentUser.uid
            _state.update { it.copy(currentUserId = userId) }
            evaluationFormRepository.initializeDefaultForms().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        loadRecentThreads(userId)
                        loadEvaluationForms(userId)
                        loadUserResponsesDirectly(userId)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Sistemde bir hata oluştu"
                            )
                        }
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    private fun loadUserResponsesDirectly(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("formResponses")
                    .whereEqualTo("userId", userId)
                    .orderBy("dateCompleted", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (snapshot.documents.isNotEmpty()) {
                    val responses = snapshot.documents.mapNotNull { document ->
                        try {
                            val answers = when (val answersData = document.get("answers")) {
                                is Map<*, *> -> {
                                    answersData.entries.associate {
                                        (it.key?.toString() ?: "") to (it.value?.toString() ?: "")
                                    }
                                }
                                else -> emptyMap<String, String>()
                            }

                            val formId = document.getString("formId") ?: ""
                            var title = document.getString("title") ?: ""

                            if (title.isEmpty() && formId.isNotEmpty()) {
                                try {
                                    val formDoc = FirebaseFirestore.getInstance()
                                        .collection("evaluationForms")
                                        .document(formId)
                                        .get()
                                        .await()
                                    title = formDoc.getString("title") ?: "Bilinmeyen Form"
                                } catch (e: Exception) {
                                    title = "Form Yanıtı"
                                }
                            }

                            FormResponse(
                                id = document.id,
                                formId = formId,
                                userId = document.getString("userId") ?: "",
                                answers = answers,
                                dateCompleted = document.getDate("dateCompleted") ?: Date(),
                                score = document.getLong("score")?.toInt() ?: 0,
                                maxScore = document.getLong("maxScore")?.toInt() ?: 0,
                                notes = document.getString("notes") ?: "",
                                title = title
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _state.update {
                        it.copy(
                            userResponses = responses,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            userResponses = emptyList(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        actionError = "Yanıtlar yüklenirken hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadEvaluationForms(userId: String) {
        evaluationFormRepository.getEvaluationForms(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            forms = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Değerlendirme formları yüklenemedi"
                        )
                    }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun refreshUserResponses() {
        viewModelScope.launch {
            val userId = state.value.currentUserId
            if (userId.isEmpty()) return@launch

            _state.update { it.copy(isLoading = true) }
            loadUserResponsesDirectly(userId)

            _state.update { it.copy(successMessage = "Yanıtlar yenilendi") }
            delay(3000)
            _state.update { it.copy(successMessage = null) }

            loadRecentThreads(userId)
        }
    }

    private fun loadRecentThreads(userId: String) {
        viewModelScope.launch {
            messagesRepository.getChatTreadsForUser(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(recentThreads = result.data ?: emptyList())
                        }
                        println("Yüklenen thread sayısı: ${result.data?.size ?: 0}")
                    }
                    is Resource.Error -> {
                        println("Thread yükleme hatası: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Yükleniyor durumu
                    }
                }
            }
        }
    }
    fun shareFormResponse(formResponseId: String, receiverId: String) {
        val userId = state.value.currentUserId
        if (userId.isBlank()) {
            _state.update { it.copy(actionError = "Kullanıcı bilgileriniz alınamadı") }
            return
        }

        val formToShare = state.value.userResponses.find { it.id == formResponseId }
        if (formToShare == null) {
            _state.update { it.copy(actionError = "Paylaşılacak form yanıtı bulunamadı") }
            return
        }

        viewModelScope.launch {
            try {

                val formDocument = try {
                    FirebaseFirestore.getInstance()
                        .collection("evaluationForms")
                        .document(formToShare.formId ?: "")
                        .get()
                        .await()
                } catch (e: Exception) {
                    null
                }
                val questions = mutableMapOf<String, String>()
                if (formDocument != null && formDocument.exists()) {
                    val questionsData = formDocument.get("questions") as? List<Map<String, Any>> ?: emptyList()
                    questionsData.forEach { questionData ->
                        val questionId = questionData["id"] as? String ?: ""
                        val questionText = questionData["text"] as? String ?: ""
                        if (questionId.isNotEmpty()) {
                            questions[questionId] = questionText
                        }
                    }
                }

                val answersJson = JSONObject()
                formToShare.answers.forEach { (key, value) ->
                    answersJson.put(key, value)
                }

                val questionsJson = JSONObject()
                questions.forEach { (key, text) ->
                    questionsJson.put(key, text)
                }

                val formData = JSONObject().apply {
                    put("id", formToShare.id)
                    put("formId", formToShare.formId ?: "")
                    put("formTitle", formToShare.title ?: "Form Yanıtı")
                    put("score", formToShare.score)
                    put("maxScore", formToShare.maxScore)
                    put("dateCompleted", formToShare.dateCompleted.time.toString())
                    put("notes", formToShare.notes ?: "")
                    put("answers", answersJson.toString())
                    put("questions", questionsJson.toString()) // Soru metinlerini ekle
                }

                val messageContent = "[EVALUATION_FORM]\n${formData}"

                val message = Message(
                    id = "",
                    senderId = userId,
                    receiverId = receiverId,
                    content = messageContent,
                    timestamp = Date(),
                    isRead = false,
                    threadId = ""
                )

                messagesRepository.sendMessage(message).collect { result ->
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(actionError = "Form paylaşımı sırasında bir hata oluştu: ${e.message}")
                }
            }
        }
    }

    private fun deleteFormResponse(responseId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            evaluationFormRepository.deleteFormResponse(responseId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Değerlendirme formu başarıyla silindi"
                            )
                        }
                        delay(500)
                        loadUserResponsesDirectly(state.value.currentUserId)
                        delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                actionError = result.message ?: "Form silinirken bir hata oluştu"
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

}