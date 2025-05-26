package com.example.fizyoapp.data.repository.exercisemanagescreen
import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseDifficulty
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlan
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanItem
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanStatus
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ExerciseRepository {
    override fun getExercisesByPhysiotherapist(physiotherapistId: String): Flow<Resource<List<Exercise>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("exercises")
            .whereEqualTo("physiotherapistId", physiotherapistId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Egzersizler yüklenemedi"))
                    return@addSnapshotListener
                }
                val exercises = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val mediaUrlsList = document.get("mediaUrls") as? List<String> ?: emptyList()
                        val mediaTypeRaw = document.get("mediaType") as? Map<String, String> ?: emptyMap()
                        val mediaTypes = mediaTypeRaw.mapValues { entry ->
                            when (entry.value) {
                                "VIDEO" -> ExerciseType.VIDEO
                                else -> ExerciseType.IMAGE
                            }
                        }
                        val difficultyStr = document.getString("difficulty") ?: ExerciseDifficulty.MEDIUM.name
                        val difficulty = try {
                            ExerciseDifficulty.valueOf(difficultyStr)
                        } catch (e: Exception) {
                            ExerciseDifficulty.MEDIUM
                        }
                        Exercise(
                            id = document.id,
                            physiotherapistId = document.getString("physiotherapistId") ?: "",
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            category = document.getString("category") ?: "",
                            mediaUrls = mediaUrlsList,
                            mediaType = mediaTypes,
                            instructions = document.getString("instructions") ?: "",
                            duration = document.getLong("duration")?.toInt() ?: 0,
                            repetitions = document.getLong("repetitions")?.toInt() ?: 0,
                            sets = document.getLong("sets")?.toInt() ?: 0,
                            difficulty = difficulty,
                            createdAt = document.getDate("createdAt") ?: Date(),
                            updatedAt = document.getDate("updatedAt") ?: Date()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(exercises))
            }
        awaitClose { listener.remove() }
    }

    override fun getExerciseById(exerciseId: String): Flow<Resource<Exercise>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("exercises")
            .document(exerciseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Egzersiz detayları yüklenemedi"))
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(Resource.Error("Egzersiz bulunamadı"))
                    return@addSnapshotListener
                }
                try {
                    val mediaUrlsList = snapshot.get("mediaUrls") as? List<String> ?: emptyList()

                    // MediaType'ı doğru şekilde çözümleme - BU KISMI DÜZELTİN
                    // getExerciseById fonksiyonunda, mediaType çözümleme kısmı:
                    val mediaTypeRaw = snapshot.get("mediaType") as? Map<String, Any> ?: emptyMap()
                    val mediaTypes = mutableMapOf<String, ExerciseType>()

// Önce mediaType haritasını çözümle
                    for ((url, typeValue) in mediaTypeRaw) {
                        val typeStr = when (typeValue) {
                            is String -> typeValue
                            else -> typeValue.toString()
                        }

                        // ExerciseType değerine dönüştür
                        val type = when (typeStr.uppercase()) {
                            "VIDEO" -> ExerciseType.VIDEO
                            "IMAGE" -> ExerciseType.IMAGE
                            else -> {
                                // Eğer tip belirtilmemişse, URL'den tahmin et
                                if (url.contains("video") || url.contains(".mp4") ||
                                    url.contains(".mov") || url.contains(".avi")) {
                                    ExerciseType.VIDEO
                                } else {
                                    ExerciseType.IMAGE
                                }
                            }
                        }

                        mediaTypes[url] = type
                    }
                    // Hata ayıklama
                    println("REPOSITORY - Media URLs: $mediaUrlsList")
                    println("REPOSITORY - Media Types: $mediaTypes")
                    val difficultyStr = snapshot.getString("difficulty") ?: ExerciseDifficulty.MEDIUM.name
                    val difficulty = try {
                        ExerciseDifficulty.valueOf(difficultyStr)
                    } catch (e: Exception) {
                        ExerciseDifficulty.MEDIUM
                    }

                    val exercise = Exercise(
                        id = snapshot.id,
                        physiotherapistId = snapshot.getString("physiotherapistId") ?: "",
                        title = snapshot.getString("title") ?: "",
                        description = snapshot.getString("description") ?: "",
                        category = snapshot.getString("category") ?: "",
                        mediaUrls = mediaUrlsList,
                        mediaType = mediaTypes,  // DÖNÜŞTÜRÜLMÜŞ mediaTypes'ı KULLANIN
                        instructions = snapshot.getString("instructions") ?: "",
                        duration = snapshot.getLong("duration")?.toInt() ?: 0,
                        repetitions = snapshot.getLong("repetitions")?.toInt() ?: 0,
                        sets = snapshot.getLong("sets")?.toInt() ?: 0,
                        difficulty = difficulty,
                        createdAt = snapshot.getDate("createdAt") ?: Date(),
                        updatedAt = snapshot.getDate("updatedAt") ?: Date()
                    )
                    trySend(Resource.Success(exercise))
                } catch (e: Exception) {
                    trySend(Resource.Error(e.localizedMessage ?: "Egzersiz verisi işlenemedi"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun createExercise(exercise: Exercise): Flow<Resource<Exercise>> = flow {
        emit(Resource.Loading())
        try {
            val exerciseMap = hashMapOf(
                "physiotherapistId" to exercise.physiotherapistId,
                "title" to exercise.title,
                "description" to exercise.description,
                "category" to exercise.category,
                "mediaUrls" to exercise.mediaUrls,
                "mediaType" to exercise.mediaType.mapValues { it.value.name },
                "instructions" to exercise.instructions,
                "duration" to exercise.duration,
                "repetitions" to exercise.repetitions,
                "sets" to exercise.sets,
                "difficulty" to exercise.difficulty.name,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            val docRef = if (exercise.id.isNotEmpty()) {
                firestore.collection("exercises").document(exercise.id)
            } else {
                firestore.collection("exercises").document()
            }
            docRef.set(exerciseMap).await()
            val savedExercise = exercise.copy(id = docRef.id)
            emit(Resource.Success(savedExercise))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Egzersiz kaydedilemedi"))
        }
    }

    override fun updateExercise(exercise: Exercise): Flow<Resource<Exercise>> = flow {
        emit(Resource.Loading())
        try {
            val exerciseMap = hashMapOf(
                "physiotherapistId" to exercise.physiotherapistId,
                "title" to exercise.title,
                "description" to exercise.description,
                "category" to exercise.category,
                "mediaUrls" to exercise.mediaUrls,
                "mediaType" to exercise.mediaType.mapValues { it.value.name },
                "instructions" to exercise.instructions,
                "duration" to exercise.duration,
                "repetitions" to exercise.repetitions,
                "sets" to exercise.sets,
                "difficulty" to exercise.difficulty.name,
                "updatedAt" to Date()
            )
            firestore.collection("exercises")
                .document(exercise.id)
                .update(exerciseMap as Map<String, Any>)
                .await()
            emit(Resource.Success(exercise))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Egzersiz güncellenemedi"))
        }
    }

    override fun deleteExercise(exerciseId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("exercises")
                .document(exerciseId)
                .delete()
                .await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Egzersiz silinemedi"))
        }
    }

    override fun uploadExerciseMedia(
        mediaUri: Uri,
        physiotherapistId: String,
        fileName: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val storageRef = storage.reference
                .child("exercises")
                .child(physiotherapistId)
                .child("$fileName-${UUID.randomUUID()}")
            val uploadTask = storageRef.putFile(mediaUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Medya yüklenemedi"))
        }
    }

    override fun getExercisePlansByPhysiotherapist(physiotherapistId: String): Flow<Resource<List<ExercisePlan>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("exercise_plans")
            .whereEqualTo("physiotherapistId", physiotherapistId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Egzersiz planları yüklenemedi"))
                    return@addSnapshotListener
                }
                val plans = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val exerciseItemsData = document.get("exercises") as? List<Map<String, Any>> ?: emptyList()
                        val exerciseItems = exerciseItemsData.map { itemData ->
                            ExercisePlanItem(
                                exerciseId = itemData["exerciseId"] as? String ?: "",
                                sets = (itemData["sets"] as? Long)?.toInt() ?: 0,
                                repetitions = (itemData["repetitions"] as? Long)?.toInt() ?: 0,
                                duration = (itemData["duration"] as? Long)?.toInt() ?: 0,
                                notes = itemData["notes"] as? String ?: ""
                            )
                        }
                        val statusStr = document.getString("status") ?: ExercisePlanStatus.ACTIVE.name
                        val status = try {
                            ExercisePlanStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            ExercisePlanStatus.ACTIVE
                        }
                        ExercisePlan(
                            id = document.id,
                            physiotherapistId = document.getString("physiotherapistId") ?: "",
                            patientId = document.getString("patientId") ?: "",
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            exercises = exerciseItems,
                            startDate = document.getDate("startDate"),
                            endDate = document.getDate("endDate"),
                            frequency = document.getString("frequency") ?: "Günlük",
                            status = status,
                            notes = document.getString("notes") ?: "",
                            createdAt = document.getDate("createdAt") ?: Date(),
                            updatedAt = document.getDate("updatedAt") ?: Date()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(plans))
            }
        awaitClose { listener.remove() }
    }

    override fun getExercisePlansByPatient(patientId: String): Flow<Resource<List<ExercisePlan>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("exercise_plans")
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Egzersiz planları yüklenemedi"))
                    return@addSnapshotListener
                }
                val plans = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val exerciseItemsData = document.get("exercises") as? List<Map<String, Any>> ?: emptyList()
                        val exerciseItems = exerciseItemsData.map { itemData ->
                            ExercisePlanItem(
                                exerciseId = itemData["exerciseId"] as? String ?: "",
                                exerciseTitle = itemData["exerciseTitle"] as? String ?: "",
                                sets = (itemData["sets"] as? Long)?.toInt() ?: 0,
                                repetitions = (itemData["repetitions"] as? Long)?.toInt() ?: 0,
                                duration = (itemData["duration"] as? Long)?.toInt() ?: 0,
                                notes = itemData["notes"] as? String ?: "",
                                mediaUrls = itemData["mediaUrls"] as? List<String> ?: emptyList()
                            )
                        }
                        val statusStr = document.getString("status") ?: ExercisePlanStatus.ACTIVE.name
                        val status = try {
                            ExercisePlanStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            ExercisePlanStatus.ACTIVE
                        }
                        ExercisePlan(
                            id = document.id,
                            physiotherapistId = document.getString("physiotherapistId") ?: "",
                            patientId = document.getString("patientId") ?: "",
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            exercises = exerciseItems,
                            startDate = document.getDate("startDate"),
                            endDate = document.getDate("endDate"),
                            frequency = document.getString("frequency") ?: "Günlük",
                            status = status,
                            notes = document.getString("notes") ?: "",
                            createdAt = document.getDate("createdAt") ?: Date(),
                            updatedAt = document.getDate("updatedAt") ?: Date()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(Resource.Success(plans))
            }
        awaitClose { listener.remove() }
    }

    override fun createExercisePlan(exercisePlan: ExercisePlan): Flow<Resource<ExercisePlan>> = flow {
        emit(Resource.Loading())
        try {
            val exerciseItemsData = exercisePlan.exercises.map { item ->
                // Firebstore'a kaydedilecek veri haritası
                val itemMap = mutableMapOf<String, Any>(
                    "exerciseId" to item.exerciseId,
                    "exerciseTitle" to item.exerciseTitle,
                    "sets" to item.sets,
                    "repetitions" to item.repetitions,
                    "duration" to item.duration,
                    "notes" to item.notes
                )

                // mediaUrls'yi sadece boş değilse ekle
                if (item.mediaUrls.isNotEmpty()) {
                    itemMap["mediaUrls"] = item.mediaUrls

                    // Eğer ilk medya URL'si bir video ise, bunu belirt
                    val isVideo = item.mediaUrls.firstOrNull()?.let { url ->
                        url.contains("video") || url.contains(".mp4") || url.contains(".mov")
                    } ?: false

                    if (isVideo) {
                        itemMap["mediaType"] = "video"
                    } else {
                        itemMap["mediaType"] = "image"
                    }
                }

                itemMap
            }

            val planMap = hashMapOf(
                "physiotherapistId" to exercisePlan.physiotherapistId,
                "patientId" to exercisePlan.patientId,
                "title" to exercisePlan.title,
                "description" to exercisePlan.description,
                "exercises" to exerciseItemsData,
                "startDate" to exercisePlan.startDate,
                "endDate" to exercisePlan.endDate,
                "frequency" to exercisePlan.frequency,
                "status" to exercisePlan.status.name,
                "notes" to exercisePlan.notes,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )

            val docRef = if (exercisePlan.id.isNotEmpty()) {
                firestore.collection("exercise_plans").document(exercisePlan.id)
            } else {
                firestore.collection("exercise_plans").document()
            }

            docRef.set(planMap).await()

            // Hastaya bildirim gönder
            val notification = hashMapOf(
                "userId" to exercisePlan.patientId,
                "title" to "Yeni Egzersiz Planı",
                "message" to "Size yeni bir egzersiz planı atandı: ${exercisePlan.title}",
                "type" to "EXERCISE_PLAN",
                "relatedId" to docRef.id,
                "createdAt" to Date(),
                "isRead" to false
            )

            firestore.collection("notifications").add(notification).await()

            val savedPlan = exercisePlan.copy(id = docRef.id)
            emit(Resource.Success(savedPlan))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Egzersiz planı kaydedilemedi"))
        }
    }

    override fun updateExercisePlan(exercisePlan: ExercisePlan): Flow<Resource<ExercisePlan>> = flow {
        emit(Resource.Loading())
        try {
            val exerciseItemsData = exercisePlan.exercises.map { item ->
                mapOf(
                    "exerciseId" to item.exerciseId,
                    "exerciseTitle" to item.exerciseTitle,
                    "sets" to item.sets,
                    "repetitions" to item.repetitions,
                    "duration" to item.duration,
                    "notes" to item.notes,
                    "mediaUrls" to item.mediaUrls
                )
            }
            val planMap = hashMapOf(
                "physiotherapistId" to exercisePlan.physiotherapistId,
                "patientId" to exercisePlan.patientId,
                "title" to exercisePlan.title,
                "description" to exercisePlan.description,
                "exercises" to exerciseItemsData,
                "startDate" to exercisePlan.startDate,
                "endDate" to exercisePlan.endDate,
                "frequency" to exercisePlan.frequency,
                "status" to exercisePlan.status.name,
                "notes" to exercisePlan.notes,
                "updatedAt" to Date()
            )
            firestore.collection("exercise_plans")
                .document(exercisePlan.id)
                .update(planMap as Map<String, Any>)
                .await()
            val notification = hashMapOf(
                "userId" to exercisePlan.patientId,
                "title" to "Egzersiz Planı Güncellendi",
                "message" to "Egzersiz planınız güncellendi: ${exercisePlan.title}",
                "type" to "EXERCISE_PLAN_UPDATE",
                "relatedId" to exercisePlan.id,
                "createdAt" to Date(),
                "isRead" to false
            )
            firestore.collection("notifications").add(notification).await()
            emit(Resource.Success(exercisePlan))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Egzersiz planı güncellenemedi"))
        }
    }

    override fun getPatientsList(physiotherapistId: String): Flow<Resource<List<PatientListItem>>> = callbackFlow {
        trySend(Resource.Loading())
        var chatThreadListener: ListenerRegistration? = null
        try {
            chatThreadListener = firestore.collection("chatThreads")
                .whereArrayContains("participantIds", physiotherapistId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.localizedMessage ?: "Hasta listesi yüklenemedi"))
                        return@addSnapshotListener
                    }
                    if (snapshot == null || snapshot.isEmpty) {
                        trySend(Resource.Success(emptyList<PatientListItem>()))
                        return@addSnapshotListener
                    }
                    val patients = mutableListOf<PatientListItem>()
                    val patientIds = mutableSetOf<String>()
                    for (document in snapshot.documents) {
                        try {
                            val participantIds = document.get("participantIds") as? List<String> ?: continue
                            val participantNames = document.get("participantNames") as? Map<String, String> ?: continue
                            val participantPhotoUrls = document.get("participantPhotoUrls") as? Map<String, String> ?: emptyMap()
                            for (participantId in participantIds) {
                                if (participantId != physiotherapistId && !patientIds.contains(participantId)) {
                                    val fullName = participantNames[participantId] ?: "İsimsiz Hasta"
                                    val photoUrl = participantPhotoUrls[participantId]
                                    patients.add(
                                        PatientListItem(
                                            userId = participantId,
                                            fullName = fullName,
                                            profilePhotoUrl = photoUrl
                                        )
                                    )
                                    patientIds.add(participantId)
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                    trySend(Resource.Success(patients))
                }
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Hasta listesi yüklenemedi"))
        }
        awaitClose {
            chatThreadListener?.remove()
        }
    }

    override fun getExercisePlanById(planId: String): Flow<Resource<ExercisePlan>> = flow {
        emit(Resource.Loading())
        try {
            val documentSnapshot = firestore.collection("exercise_plans")
                .document(planId)
                .get()
                .await()
            if (!documentSnapshot.exists()) {
                emit(Resource.Error("Plan bulunamadı"))
                return@flow
            }
            val exerciseItemsData = documentSnapshot.get("exercises") as? List<Map<String, Any>> ?: emptyList()
            val exerciseItems = exerciseItemsData.map { itemData ->
                ExercisePlanItem(
                    exerciseId = itemData["exerciseId"] as? String ?: "",
                    exerciseTitle = itemData["exerciseTitle"] as? String ?: "",
                    sets = (itemData["sets"] as? Long)?.toInt() ?: 0,
                    repetitions = (itemData["repetitions"] as? Long)?.toInt() ?: 0,
                    duration = (itemData["duration"] as? Long)?.toInt() ?: 0,
                    notes = itemData["notes"] as? String ?: "",
                    mediaUrls = itemData["mediaUrls"] as? List<String> ?: emptyList()
                )
            }
            val statusStr = documentSnapshot.getString("status") ?: ExercisePlanStatus.ACTIVE.name
            val status = try {
                ExercisePlanStatus.valueOf(statusStr)
            } catch (e: Exception) {
                ExercisePlanStatus.ACTIVE
            }
            val plan = ExercisePlan(
                id = documentSnapshot.id,
                physiotherapistId = documentSnapshot.getString("physiotherapistId") ?: "",
                patientId = documentSnapshot.getString("patientId") ?: "",
                title = documentSnapshot.getString("title") ?: "",
                description = documentSnapshot.getString("description") ?: "",
                exercises = exerciseItems,
                startDate = documentSnapshot.getDate("startDate"),
                endDate = documentSnapshot.getDate("endDate"),
                frequency = documentSnapshot.getString("frequency") ?: "Günlük",
                status = status,
                notes = documentSnapshot.getString("notes") ?: "",
                createdAt = documentSnapshot.getDate("createdAt") ?: Date(),
                updatedAt = documentSnapshot.getDate("updatedAt") ?: Date()
            )
            emit(Resource.Success(plan))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Plan detayları yüklenemedi"))
        }
    }

    override fun deleteExercisePlan(exercisePlanId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val planDoc = firestore.collection("exercise__plans")
                .document(exercisePlanId)
                .get()
                .await()
            val patientId = planDoc.getString("patientId") ?: ""
            val planTitle = planDoc.getString("title") ?: "Egzersiz Planı"
            firestore.collection("exercise_plans")
                .document(exercisePlanId)
                .delete()
                .await()
            if (patientId.isNotEmpty()) {
                val notification = hashMapOf(
                    "userId" to patientId,
                    "title" to "Egzersiz Planı İptal Edildi",
                    "message" to "Egzersiz planınız iptal edildi: $planTitle",
                    "type" to "EXERCISE_PLAN_CANCELLED",
                    "relatedId" to exercisePlanId,
                    "createdAt" to Date(),
                    "isRead" to false
                )
                firestore.collection("notifications").add(notification).await()
            }
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Egzersiz planı silinemedi"))
        }
    }
}