package com.example.fizyoapp.data.repository.mainscreen
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PainTrackingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PainTrackingRepository {
    override fun getPainRecords(userId: String): Flow<Resource<List<PainRecord>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("painRecords")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Ağrı kayıtları alınamadı"))
                    return@addSnapshotListener
                }
                val painRecords = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PainRecord::class.java)
                } ?: emptyList()
                trySend(Resource.Success(painRecords))
            }
        awaitClose { listener.remove() }
    }

    override fun getLatestPainRecord(userId: String): Flow<Resource<PainRecord?>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val snapshot = firestore.collection("painRecords")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val painRecord = if (snapshot.isEmpty) null
            else snapshot.documents[0].toObject(PainRecord::class.java)
            trySend(Resource.Success(painRecord))
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "En son ağrı kaydı alınamadı"))
        }
        awaitClose()
    }

    override fun addPainRecord(painRecord: PainRecord): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val recordId = painRecord.id.ifEmpty { UUID.randomUUID().toString() }
            val record = painRecord.copy(id = recordId)
            firestore.collection("painRecords")
                .document(recordId)
                .set(record)
                .await()
            trySend(Resource.Success(true))
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Ağrı kaydı eklenemedi"))
        }
        awaitClose()
    }

    override fun updatePainRecord(painRecord: PainRecord): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            firestore.collection("painRecords")
                .document(painRecord.id)
                .set(painRecord)
                .await()
            trySend(Resource.Success(true))
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Ağrı kaydı güncellenemedi"))
        }
        awaitClose()
    }

    override fun deletePainRecord(painRecordId: String): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            firestore.collection("painRecords")
                .document(painRecordId)
                .delete()
                .await()
            trySend(Resource.Success(true))
        } catch (e: Exception) {
            trySend(Resource.Error(e.localizedMessage ?: "Ağrı kaydı silinemedi"))
        }
        awaitClose()
    }
}