package com.example.fizyoapp.data.repository.mainscreen

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import kotlinx.coroutines.flow.Flow

interface PainTrackingRepository {
    fun getPainRecords(userId: String): Flow<Resource<List<PainRecord>>>
    fun getLatestPainRecord(userId: String): Flow<Resource<PainRecord?>>
    fun addPainRecord(painRecord: PainRecord): Flow<Resource<Boolean>>
    fun updatePainRecord(painRecord: PainRecord): Flow<Resource<Boolean>>
    fun deletePainRecord(painRecordId: String): Flow<Resource<Boolean>>
}