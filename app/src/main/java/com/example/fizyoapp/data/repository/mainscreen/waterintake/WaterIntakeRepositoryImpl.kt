package com.example.fizyoapp.data.repository.mainscreen.waterintake

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.WaterIntake
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterIntakeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WaterIntakeRepository {

    override suspend fun updateWaterIntake(waterIntake: WaterIntake): Resource<Unit> {
        return try {
            // Bugünün başlangıcını belirle
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            // Bugünün sonunu belirle
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis - 1

            // Bugün için kayıt var mı diye kontrol et
            val snapshot = firestore.collection("waterIntake")
                .whereEqualTo("userId", waterIntake.userId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                // Varsa güncelle
                val existingId = snapshot.documents[0].id
                firestore.collection("waterIntake").document(existingId).set(waterIntake).await()
            } else {
                // Yoksa yeni kayıt oluştur
                val documentRef = firestore.collection("waterIntake").document()
                val waterIntakeWithId = waterIntake.copy(id = documentRef.id)
                documentRef.set(waterIntakeWithId).await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Su tüketimi güncellenemedi", e)
        }
    }

    override fun getWaterIntakeForToday(userId: String): Flow<Resource<WaterIntake?>> = flow {
        emit(Resource.Loading())
        try {
            // Bugünün başlangıcını belirle
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            // Bugünün sonunu belirle
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis - 1

            val snapshot = firestore.collection("waterIntake")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .await()

            val waterIntake = if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].toObject(WaterIntake::class.java)
            } else {
                WaterIntake(userId = userId, date = System.currentTimeMillis())
            }

            emit(Resource.Success(waterIntake))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Su tüketimi alınamadı", e))
        }
    }

    override fun getWaterIntakeHistory(userId: String, limit: Int): Flow<Resource<List<WaterIntake>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("waterIntake")
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val waterIntakes = snapshot.toObjects(WaterIntake::class.java)
            emit(Resource.Success(waterIntakes))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Su tüketim geçmişi alınamadı", e))
        }
    }
}