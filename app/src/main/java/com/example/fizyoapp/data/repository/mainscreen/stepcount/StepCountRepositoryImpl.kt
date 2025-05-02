package com.example.fizyoapp.data.repository.mainscreen.stepcount

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.StepCount
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepCountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val context: Context
) : StepCountRepository, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialSteps: Int? = null
    private var currentStepCount = 0

    init {
        registerStepCounter()
    }

    private fun registerStepCounter() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val steps = it.values[0].toInt()

                if (initialSteps == null) {
                    initialSteps = steps
                }

                currentStepCount = steps - (initialSteps ?: steps)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override suspend fun updateStepCount(stepCount: StepCount): Resource<Unit> {
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
            val snapshot = firestore.collection("stepCount")
                .whereEqualTo("userId", stepCount.userId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .await()

            // Otomatik olarak algılanan adımları kullan
            val updatedStepCount = stepCount.copy(steps = currentStepCount)

            if (snapshot.documents.isNotEmpty()) {
                // Varsa güncelle
                val existingId = snapshot.documents[0].id
                firestore.collection("stepCount").document(existingId).set(updatedStepCount).await()
            } else {
                // Yoksa yeni kayıt oluştur
                val documentRef = firestore.collection("stepCount").document()
                val stepCountWithId = updatedStepCount.copy(id = documentRef.id)
                documentRef.set(stepCountWithId).await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Adım sayısı güncellenemedi", e)
        }
    }

    override fun getStepCountForToday(userId: String): Flow<Resource<StepCount?>> = flow {
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

            val snapshot = firestore.collection("stepCount")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .await()

            val stepCount = if (snapshot.documents.isNotEmpty()) {
                val dbStepCount = snapshot.documents[0].toObject(StepCount::class.java)
                // Sensör verisi ile veritabanı verisini karşılaştır, en yüksek olanı kullan
                dbStepCount?.copy(steps = maxOf(dbStepCount.steps, currentStepCount))
            } else {
                StepCount(userId = userId, date = System.currentTimeMillis(), steps = currentStepCount)
            }

            emit(Resource.Success(stepCount))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Adım sayısı alınamadı", e))
        }
    }

    override fun getStepCountHistory(userId: String, limit: Int): Flow<Resource<List<StepCount>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("stepCount")
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val stepCounts = snapshot.toObjects(StepCount::class.java)
            emit(Resource.Success(stepCounts))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Adım sayısı geçmişi alınamadı", e))
        }
    }

    // Avoid memory leaks by unregistering listener
    fun cleanup() {
        sensorManager.unregisterListener(this)
    }
}