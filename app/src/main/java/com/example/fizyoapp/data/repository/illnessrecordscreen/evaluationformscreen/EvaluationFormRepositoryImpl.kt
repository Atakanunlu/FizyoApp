package com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.EvaluationForm
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.EvaluationFormType
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormQuestion
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.QuestionType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class EvaluationFormRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EvaluationFormRepository {
    override fun getEvaluationForms(userId: String): Flow<Resource<List<EvaluationForm>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection("evaluationForms")
            .orderBy("type", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Değerlendirme formları yüklenemedi"))
                    return@addSnapshotListener
                }

                val forms = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val formType = document.getString("type")?.let {
                            try {
                                EvaluationFormType.valueOf(it)
                            } catch (e: Exception) {
                                EvaluationFormType.CUSTOM
                            }
                        } ?: EvaluationFormType.CUSTOM

                        val questionsData = document.get("questions") as? List<Map<String, Any>> ?: emptyList()
                        val questions = questionsData.map { questionData ->
                            val questionType = (questionData["type"] as? String)?.let {
                                try {
                                    QuestionType.valueOf(it)
                                } catch (e: Exception) {
                                    QuestionType.TEXT
                                }
                            } ?: QuestionType.TEXT

                            val options = (questionData["options"] as? List<String>) ?: emptyList()

                            FormQuestion(
                                id = questionData["id"] as? String ?: "",
                                text = questionData["text"] as? String ?: "",
                                type = questionType,
                                options = options,
                                required = questionData["required"] as? Boolean ?: true,
                                minValue = (questionData["minValue"] as? Long)?.toInt(),
                                maxValue = (questionData["maxValue"] as? Long)?.toInt()
                            )
                        }

                        EvaluationForm(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            type = formType,
                            dateCreated = document.getDate("dateCreated") ?: Date(),
                            questions = questions,
                            maxScore = document.getLong("maxScore")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                launch {
                    try {
                        val completedFormsQuery = firestore.collection("formResponses")
                            .whereEqualTo("userId", userId)
                            .get()
                            .await()
                        val completedFormIds = completedFormsQuery.documents.mapNotNull { it.getString("formId") }

                        val finalForms = forms.map { form ->
                            form.copy(isCompleted = form.id in completedFormIds)
                        }

                        trySend(Resource.Success(finalForms))
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.localizedMessage ?: "Form verilerini işlerken hata oluştu"))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getEvaluationFormById(formId: String): Flow<Resource<EvaluationForm>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection("evaluationForms")
            .document(formId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Form detayları yüklenemedi"))
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(Resource.Error("Form bulunamadı"))
                    return@addSnapshotListener
                }

                try {
                    val formType = snapshot.getString("type")?.let {
                        try {
                            EvaluationFormType.valueOf(it)
                        } catch (e: Exception) {
                            EvaluationFormType.CUSTOM
                        }
                    } ?: EvaluationFormType.CUSTOM

                    val questionsData = snapshot.get("questions") as? List<Map<String, Any>> ?: emptyList()
                    val questions = questionsData.map { questionData ->
                        val questionType = (questionData["type"] as? String)?.let {
                            try {
                                QuestionType.valueOf(it)
                            } catch (e: Exception) {
                                QuestionType.TEXT
                            }
                        } ?: QuestionType.TEXT

                        val options = (questionData["options"] as? List<String>) ?: emptyList()

                        FormQuestion(
                            id = questionData["id"] as? String ?: "",
                            text = questionData["text"] as? String ?: "",
                            type = questionType,
                            options = options,
                            required = questionData["required"] as? Boolean ?: true,
                            minValue = (questionData["minValue"] as? Long)?.toInt(),
                            maxValue = (questionData["maxValue"] as? Long)?.toInt()
                        )
                    }

                    val form = EvaluationForm(
                        id = snapshot.id,
                        title = snapshot.getString("title") ?: "",
                        description = snapshot.getString("description") ?: "",
                        type = formType,
                        dateCreated = snapshot.getDate("dateCreated") ?: Date(),
                        questions = questions,
                        maxScore = snapshot.getLong("maxScore")?.toInt() ?: 0
                    )

                    trySend(Resource.Success(form))
                } catch (e: Exception) {
                    trySend(Resource.Error(e.localizedMessage ?: "Form işlenirken hata oluştu"))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun saveFormResponse(formResponse: FormResponse): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val responseId = if (formResponse.id.isNotEmpty()) {
                formResponse.id
            } else {
                UUID.randomUUID().toString()
            }

            val responseData = hashMapOf(
                "id" to responseId,
                "formId" to formResponse.formId,
                "userId" to formResponse.userId,
                "answers" to formResponse.answers,
                "dateCompleted" to formResponse.dateCompleted,
                "score" to formResponse.score,
                "maxScore" to formResponse.maxScore,
                "notes" to formResponse.notes,
                "title" to formResponse.title
            )

            firestore.collection("formResponses")
                .document(responseId)
                .set(responseData)
                .await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Form yanıtı kaydedilemedi"))
        }
    }

    override fun getFormResponsesByUser(userId: String): Flow<Resource<List<FormResponse>>> = flow {
        emit(Resource.Loading())

        try {
            val snapshot = firestore.collection("formResponses")
                .whereEqualTo("userId", userId)
                .orderBy("dateCompleted", Query.Direction.DESCENDING)
                .get()
                .await()

            val responses = snapshot.documents.mapNotNull { document ->
                try {
                    val answers = document.get("answers") as? Map<String, String> ?: emptyMap()
                    val formId = document.getString("formId") ?: ""
                    var title = document.getString("title") ?: ""

                    if (title.isEmpty() && formId.isNotEmpty()) {
                        try {
                            val formDoc = firestore.collection("evaluationForms")
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

            emit(Resource.Success(responses))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Form yanıtları yüklenemedi"))
        }
    }

    override fun getFormResponseById(responseId: String): Flow<Resource<FormResponse>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection("formResponses")
            .document(responseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Form yanıtı yüklenemedi"))
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(Resource.Error("Form yanıtı bulunamadı"))
                    return@addSnapshotListener
                }

                launch {
                    try {
                        val answers = snapshot.get("answers") as? Map<String, String> ?: emptyMap()
                        val formId = snapshot.getString("formId") ?: ""
                        val existingTitle = snapshot.getString("title") ?: ""

                        var formTitle = existingTitle
                        if (formTitle.isEmpty()) {
                            try {
                                val formDoc = firestore.collection("evaluationForms")
                                    .document(formId)
                                    .get()
                                    .await()
                                formTitle = formDoc.getString("title") ?: "Bilinmeyen Form"
                            } catch (e: Exception) {
                                formTitle = "Form Yanıtı"
                            }
                        }

                        val response = FormResponse(
                            id = snapshot.id,
                            formId = formId,
                            userId = snapshot.getString("userId") ?: "",
                            answers = answers,
                            dateCompleted = snapshot.getDate("dateCompleted") ?: Date(),
                            score = snapshot.getLong("score")?.toInt() ?: 0,
                            maxScore = snapshot.getLong("maxScore")?.toInt() ?: 0,
                            notes = snapshot.getString("notes") ?: "",
                            title = formTitle
                        )

                        trySend(Resource.Success(response))
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.localizedMessage ?: "Form yanıtı işlenirken hata oluştu"))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override fun shareFormResponse(responseId: String, receiverId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val responseDoc = firestore.collection("formResponses")
                .document(responseId)
                .get()
                .await()

            if (!responseDoc.exists()) {
                emit(Resource.Error("Paylaşılacak form yanıtı bulunamadı"))
                return@flow
            }

            val formId = responseDoc.getString("formId") ?: ""
            val userId = responseDoc.getString("userId") ?: ""
            val answers = responseDoc.get("answers") as? Map<String, String> ?: emptyMap()
            val score = responseDoc.getLong("score")?.toInt() ?: 0
            val maxScore = responseDoc.getLong("maxScore")?.toInt() ?: 0
            val dateCompleted = responseDoc.getDate("dateCompleted") ?: Date()
            val notes = responseDoc.getString("notes") ?: ""

            val formDoc = firestore.collection("evaluationForms")
                .document(formId)
                .get()
                .await()

            val formTitle = formDoc.getString("title") ?: "Bilinmeyen Form"
            val formDescription = formDoc.getString("description") ?: ""

            val formResponseData = JSONObject().apply {
                put("id", responseId)
                put("formId", formId)
                put("formTitle", formTitle)
                put("formDescription", formDescription)
                put("userId", userId)
                put("score", score)
                put("maxScore", maxScore)
                put("dateCompleted", dateCompleted.time)
                put("notes", notes)

                val answersJson = JSONObject()
                for ((key, value) in answers) {
                    answersJson.put(key, value)
                }
                put("answers", answersJson.toString())
            }

            val messageContent = "[EVALUATION_FORM]\n${formResponseData}"

            val message = hashMapOf(
                "senderId" to userId,
                "receiverId" to receiverId,
                "content" to messageContent,
                "timestamp" to Date(),
                "isRead" to false
            )

            val participantIds = listOf(userId, receiverId).sorted()
            var threadId = ""

            val threadQuery = firestore.collection("chatThreads")
                .whereArrayContains("participantIds", userId)
                .get()
                .await()

            for (threadDoc in threadQuery.documents) {
                val participants = threadDoc.get("participantIds") as? List<String> ?: continue
                if (participants.containsAll(participantIds) && participants.size == participantIds.size) {
                    threadId = threadDoc.id
                    break
                }
            }

            if (threadId.isEmpty()) {
                val newThreadRef = firestore.collection("chatThreads").document()
                threadId = newThreadRef.id
                val threadData = hashMapOf(
                    "participantIds" to participantIds,
                    "lastMessage" to "Değerlendirme Formu Paylaşıldı: $formTitle",
                    "lastMessageTimestamp" to Date(),
                    "unreadCount_$receiverId" to 1
                )
                newThreadRef.set(threadData).await()
            } else {
                firestore.collection("chatThreads").document(threadId)
                    .update(
                        mapOf(
                            "lastMessage" to "Değerlendirme Formu Paylaşıldı: $formTitle",
                            "lastMessageTimestamp" to Date(),
                            "unreadCount_$receiverId" to com.google.firebase.firestore.FieldValue.increment(1)
                        )
                    )
                    .await()
            }

            message["threadId"] = threadId
            firestore.collection("messages").add(message).await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Form yanıtı paylaşılırken hata oluştu"))
        }
    }

    override fun deleteFormResponse(responseId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            firestore.collection("formResponses")
                .document(responseId)
                .delete()
                .await()

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Form yanıtı silinemedi"))
        }
    }
}