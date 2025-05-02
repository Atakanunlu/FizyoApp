package com.example.fizyoapp.data.repository.mainscreen.painrecord

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import kotlinx.coroutines.flow.Flow

interface PainRepository {
    suspend fun addPainRecord(painRecord: PainRecord): Resource<Unit>
    suspend fun updatePainRecord(painRecord: PainRecord): Resource<Unit>
    suspend fun deletePainRecord(id: String): Resource<Unit>
    suspend fun getPainRecordById(id: String): Resource<PainRecord>
    fun getPainRecordsForUser(userId: String): Flow<Resource<List<PainRecord>>>
    fun getLatestPainRecord(userId: String): Flow<Resource<PainRecord?>>
}