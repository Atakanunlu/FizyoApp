package com.example.fizyoapp.data.repository.mainscreen.painrecord

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PainRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PainRepository {

    override suspend fun addPainRecord(painRecord: PainRecord): Resource<Unit> {
        return try {
            val documentRef = firestore.collection("painRecords").document()
            val painRecordWithId = painRecord.copy(id = documentRef.id)
            documentRef.set(painRecordWithId).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ağrı kaydı eklenemedi", e)
        }
    }

    override suspend fun updatePainRecord(painRecord: PainRecord): Resource<Unit> {
        return try {
            firestore.collection("painRecords").document(painRecord.id).set(painRecord).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ağrı kaydı güncellenemedi", e)
        }
    }

    override suspend fun deletePainRecord(id: String): Resource<Unit> {
        return try {
            firestore.collection("painRecords").document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ağrı kaydı silinemedi", e)
        }
    }

    override suspend fun getPainRecordById(id: String): Resource<PainRecord> {
        return try {
            val snapshot = firestore.collection("painRecords").document(id).get().await()
            val painRecord = snapshot.toObject(PainRecord::class.java)
                ?: return Resource.Error("Ağrı kaydı bulunamadı")
            Resource.Success(painRecord)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ağrı kaydı alınamadı", e)
        }
    }

    override fun getPainRecordsForUser(userId: String): Flow<Resource<List<PainRecord>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("painRecords")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val painRecords = snapshot.toObjects(PainRecord::class.java)
            emit(Resource.Success(painRecords))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ağrı kayıtları alınamadı", e))
        }
    }

    override fun getLatestPainRecord(userId: String): Flow<Resource<PainRecord?>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("painRecords")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val painRecords = snapshot.toObjects(PainRecord::class.java)
            emit(Resource.Success(painRecords.firstOrNull()))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Son ağrı kaydı alınamadı", e))
        }
    }
}