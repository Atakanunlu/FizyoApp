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
            .whereEqualTo("userId", "")
            .orderBy("title", Query.Direction.ASCENDING)
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


    override fun initializeDefaultForms(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val existingForms = firestore.collection("evaluationForms")
                .whereIn("title", listOf(
                    "VAS - Görsel Analog Ağrı Ölçeği",
                    "DASH - Kol, Omuz ve El Sorunları Anketi",
                    "SF-36 Sağlık Anketi"
                ))
                .get()
                .await()

            if (existingForms.documents.isEmpty()) {
                val defaultForms = listOf(
                    createDefaultVASForm(),
                    createDefaultDASHForm(),
                    createDefaultSF36Form()
                )
                defaultForms.forEach { form ->
                    val formId = UUID.randomUUID().toString()
                    firestore.collection("evaluationForms")
                        .document(formId)
                        .set(form.copy(id = formId))
                        .await()
                }
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Default formlar oluşturulamadı"))
        }
    }
    fun createDefaultVASForm(): EvaluationForm {
        val questions = listOf(
            FormQuestion(
                id = "vas_current_pain",
                text = "Hissettiğiniz ağrının şiddetini 0-10 arasında işaretleyiniz.\n(0 = Hiç ağrı yok, 10 = Hayal edilebilecek en kötü ağrı)",
                type = QuestionType.SCALE,
                minValue = 0,
                maxValue = 10,
                required = true
            )


        )

        return EvaluationForm(
            title = "VAS - Görsel Analog Ağrı Ölçeği",
            description = "Ağrı şiddetinizi değerlendirmek için kullanılan ölçek",
            type = EvaluationFormType.PAIN_ASSESSMENT,
            questions = questions,
            maxScore = 10
        )
    }
    fun createDefaultDASHForm(): EvaluationForm {
        val questions = listOf(
            FormQuestion(
                id = "dash_1",
                text = "Sıkı bir kavanozu açmak.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_2",
                text = "Yazı yazmak.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_3",
                text = "Anahtar çevirmek.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_4",
                text = "Yemek hazırlamak.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_5",
                text = "Ağır bir kapıyı itmek.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_6",
                text = "Bir rafın üzerine bir şey koymak.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_7",
                text = "Ağır ev işleri (duvar boyamak, yer silmek).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_8",
                text = "Bahçe işleri veya avlu işleri.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_9",
                text = "Yatak toplama.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_10",
                text = "Alışveriş torbası veya evrak çantası taşımak.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_11",
                text = "Ağır bir nesne taşımak (4.5 kg'dan fazla).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_12",
                text = "Ampul değiştirmek.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_13",
                text = "Saç yıkamak veya föenlemek.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_14",
                text = "Sırt yıkamak.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_15",
                text = "Kazak giymek.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_16",
                text = "Bıçak kullanmak (yemek kesmek).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_17",
                text = "Eğlence aktiviteleri - az güç gerektiren (kart oynamak, örgü örmek vs.).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_18",
                text = "Eğlence aktiviteleri - güç gerektiren (golf, çekiç kullanmak, tenis vs.).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_19",
                text = "Eğlence aktiviteleri - serbestçe kol hareket ettirme gerektiren (frizbi, badminton vs.).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_20",
                text = "Taşımacılık (bir yerden başka yere gitme).",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_21",
                text = "Cinsel aktiviteler.",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç güçlük çekmem (1)", "Biraz güçlük çekerim (2)", "Orta derecede güçlük çekerim (3)", "Çok güçlük çekerim (4)", "Yapamam (5)"),
                required = false
            ),


            FormQuestion(
                id = "dash_22",
                text = "Geçen hafta, kol, omuz veya elinizde hissettiğiniz ağrının şiddeti nasıldı?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç ağrım yoktu (1)", "Hafif (2)", "Orta (3)", "Şiddetli (4)", "Aşırı şiddetli (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_23",
                text = "Geçen hafta, kol, omuz veya elinizde hissettiğiniz aktivite ağrısının şiddeti nasıldı?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç ağrım yoktu (1)", "Hafif (2)", "Orta (3)", "Şiddetli (4)", "Aşırı şiddetli (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_24",
                text = "Geçen hafta, kol, omuz veya elinizde karıncalanma hissettiniz mi?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç (1)", "Hafif (2)", "Orta (3)", "Şiddetli (4)", "Aşırı şiddetli (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_25",
                text = "Geçen hafta, kol, omuz veya elinizde güçsüzlük hissettiniz mi?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç (1)", "Hafif (2)", "Orta (3)", "Şiddetli (4)", "Aşırı şiddetli (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_26",
                text = "Geçen hafta, kol, omuz veya elinizde sertlik hissettiniz mi?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç (1)", "Hafif (2)", "Orta (3)", "Şiddetli (4)", "Aşırı şiddetli (5)"),
                required = true
            ),


            FormQuestion(
                id = "dash_27",
                text = "Geçen hafta uyurken, kol, omuz veya elinizden kaynaklanan uyku problemi yaşadınız mı?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç zorluk çekmedim (1)", "Biraz zorluk çektim (2)", "Orta derecede zorluk çektim (3)", "Çok zorluk çektim (4)", "O kadar çok zorluk çektim ki uyuyamadım (5)"),
                required = true
            ),
            FormQuestion(
                id = "dash_28",
                text = "Kol, omuz veya el probleminiz nedeniyle kendinizi daha az yetenekli, daha az güvenli veya daha az yararlı hissediyor musunuz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç (1)", "Biraz (2)", "Orta derecede (3)", "Çok (4)", "Aşırı derecede (5)"),
                required = true
            )
        )

        return EvaluationForm(
            title = "DASH - Kol, Omuz ve El Sorunları Anketi",
            description = "Üst ekstremite fonksiyonel değerlendirme anketi",
            type = EvaluationFormType.FUNCTIONAL_MOBILITY,
            questions = questions,
            maxScore = 100
        )
    }

    fun createDefaultSF36Form(): EvaluationForm {
        val questions = listOf(
            FormQuestion(
                id = "sf36_1",
                text = "Genel olarak sağlığınızı nasıl değerlendiriyorsunuz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Mükemmel (1)", "Çok iyi (2)", "İyi (3)", "Orta (4)", "Kötü (5)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_2",
                text = "Sağlığınızı bir yıl öncesi ile karşılaştırdığınızda, şu anda sağlığınız nasıl?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Şimdi bir yıl öncesinden çok daha iyi (1)", "Şimdi bir yıl öncesinden biraz daha iyi (2)", "Hemen hemen bir yıl öncesi ile aynı (3)", "Şimdi bir yıl öncesinden biraz daha kötü (4)", "Şimdi bir yıl öncesinden çok daha kötü (5)"),
                required = true
            ),


            FormQuestion(
                id = "sf36_3a",
                text = "Şu andaki sağlığınız aşağıdaki aktiviteleri ne ölçüde kısıtlıyor? Güçlü aktiviteler, koşma, ağır eşya kaldırma, şiddetli sporlara katılma",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3b",
                text = "Orta derecede aktiviteler, masa itme, elektrik süpürgesi çekme, bowling oynama veya golf oynama",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3c",
                text = "Market alışverişi yapmak",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3d",
                text = "Birkaç kat merdiven çıkmak",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3e",
                text = "Bir kat merdiven çıkmak",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3f",
                text = "Eğilmek, diz çökmek veya çömelmek",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3g",
                text = "1.5 km'den fazla yürümek",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3h",
                text = "Birkaç yüz metre yürümek",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3i",
                text = "100 metre yürümek",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_3j",
                text = "Yıkanmak veya giyinmek",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç kısıtlamıyor (1)", "Biraz kısıtlıyor (2)", "Çok kısıtlıyor (3)"),
                required = true
            ),


            FormQuestion(
                id = "sf36_4a",
                text = "Son 4 hafta içinde fiziksel sağlığınız nedeniyle işinizde veya diğer günlük aktivitelerinizde: İstediğinizden daha az şey yaptınız mı?",
                type = QuestionType.YES_NO,
                required = true
            ),
            FormQuestion(
                id = "sf36_4b",
                text = "Yapmak istediğiniz iş türünde veya diğer aktivitelerde kısıtlılık yaşadınız mı?",
                type = QuestionType.YES_NO,
                required = true
            ),


            FormQuestion(
                id = "sf36_5a",
                text = "Son 4 hafta içinde duygusal problemleriniz nedeniyle işinizde veya diğer günlük aktivitelerinizde: İstediğinizden daha az şey yaptınız mı?",
                type = QuestionType.YES_NO,
                required = true
            ),
            FormQuestion(
                id = "sf36_5b",
                text = "İşinizi veya diğer aktivitelerinizi her zamanki kadar dikkatli yapamadınız mı?",
                type = QuestionType.YES_NO,
                required = true
            ),

            FormQuestion(
                id = "sf36_6",
                text = "Son 4 hafta içinde fiziksel sağlığınız veya duygusal problemleriniz, aile, arkadaş, komşu veya gruplarla olan normal sosyal aktivitelerinizi ne ölçüde etkiledi?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç etkilemedi (1)", "Biraz etkiledi (2)", "Orta derecede etkiledi (3)", "Çok etkiledi (4)", "Aşırı derecede etkiledi (5)"),
                required = true
            ),


            FormQuestion(
                id = "sf36_7",
                text = "Son 4 hafta içinde ne kadar ağrı hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç (1)", "Çok hafif (2)", "Hafif (3)", "Orta (4)", "Şiddetli (5)", "Çok şiddetli (6)"),
                required = true
            ),


            FormQuestion(
                id = "sf36_8",
                text = "Son 4 hafta içinde ağrı, normal işlerinizi (hem ev dışında hem de ev işleri) ne ölçüde engelledi?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiç engellemedi (1)", "Biraz engelledi (2)", "Orta derecede engelledi (3)", "Çok engelledi (4)", "Aşırı derecede engelledi (5)"),
                required = true
            ),


            FormQuestion(
                id = "sf36_9a",
                text = "Son 4 hafta içinde ne sıklıkla kendinizi canlı hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9b",
                text = "Son 4 hafta içinde ne sıklıkla çok sinirli bir insan oldunuz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9c",
                text = "Son 4 hafta içinde ne sıklıkla kendinizi o kadar kötü hissettiniz ki hiçbir şey sizi neşelendiremez hale geldiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9d",
                text = "Son 4 hafta içinde ne sıklıkla kendinizi sakin ve huzurlu hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9e",
                text = "Son 4 hafta içinde ne sıklıkla enerji dolu hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9f",
                text = "Son 4 hafta içinde ne sıklıkla kendinizi kederli ve üzgün hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9g",
                text = "Son 4 hafta içinde ne sıklıkla kendinizi bitkin hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9h",
                text = "Son 4 hafta içinde ne sıklıkla mutlu bir insan oldunuz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_9i",
                text = "Son 4 hafta içinde ne sıklıkla kendinizi yorgun hissettiniz?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Her zaman (1)", "Çoğu zaman (2)", "Oldukça sık (3)", "Bazen (4)", "Nadiren (5)", "Hiçbir zaman (6)"),
                required = true
            ),

            FormQuestion(
                id = "sf36_10",
                text = "Son 4 hafta içinde fiziksel sağlığınız veya duygusal problemleriniz sosyal aktivitelerinizi (arkadaş, akraba ziyaret etmek gibi) ne kadar süre engelledi?",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Hiçbir zaman (1)", "Nadiren (2)", "Bazen (3)", "Çoğu zaman (4)", "Her zaman (5)"),
                required = true
            ),


            FormQuestion(
                id = "sf36_11a",
                text = "Aşağıdaki ifadeler sizin için ne kadar doğru veya yanlış: Diğer insanlardan biraz daha kolay hastalanıyor gibiyim",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Kesinlikle doğru (1)", "Çoğunlukla doğru (2)", "Emin değilim (3)", "Çoğunlukla yanlış (4)", "Kesinlikle yanlış (5)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_11b",
                text = "Tanıdığım herhangi biri kadar sağlıklıyım",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Kesinlikle doğru (1)", "Çoğunlukla doğru (2)", "Emin değilim (3)", "Çoğunlukla yanlış (4)", "Kesinlikle yanlış (5)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_11c",
                text = "Sağlığımın kötüleşeceğini düşünüyorum",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Kesinlikle doğru (1)", "Çoğunlukla doğru (2)", "Emin değilim (3)", "Çoğunlukla yanlış (4)", "Kesinlikle yanlış (5)"),
                required = true
            ),
            FormQuestion(
                id = "sf36_11d",
                text = "Sağlığım mükemmel",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Kesinlikle doğru (1)", "Çoğunlukla doğru (2)", "Emin değilim (3)", "Çoğunlukla yanlış (4)", "Kesinlikle yanlış (5)"),
                required = true
            )
        )

        return EvaluationForm(
            title = "SF-36 Sağlık Anketi",
            description = "Genel sağlık durumunu ve yaşam kalitesini değerlendiren anket",
            type = EvaluationFormType.FUNCTIONAL_MOBILITY,
            questions = questions,
            maxScore = 100
        )
    }


}