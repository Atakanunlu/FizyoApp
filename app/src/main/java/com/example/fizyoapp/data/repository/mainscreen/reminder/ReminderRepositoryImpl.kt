package com.example.fizyoapp.data.repository.mainscreen.reminder

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReminderRepository {

    override suspend fun addReminder(reminder: Reminder): Resource<Unit> {
        return try {
            val documentRef = firestore.collection("reminders").document()
            val reminderWithId = reminder.copy(id = documentRef.id)
            documentRef.set(reminderWithId).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hatırlatma eklenemedi", e)
        }
    }

    override suspend fun updateReminder(reminder: Reminder): Resource<Unit> {
        return try {
            firestore.collection("reminders").document(reminder.id).set(reminder).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hatırlatma güncellenemedi", e)
        }
    }

    override suspend fun deleteReminder(id: String): Resource<Unit> {
        return try {
            firestore.collection("reminders").document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hatırlatma silinemedi", e)
        }
    }

    override fun getActiveReminders(userId: String): Flow<Resource<List<Reminder>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("reminders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val reminders = snapshot.toObjects(Reminder::class.java)
            emit(Resource.Success(reminders))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Hatırlatmalar alınamadı", e))
        }
    }
}