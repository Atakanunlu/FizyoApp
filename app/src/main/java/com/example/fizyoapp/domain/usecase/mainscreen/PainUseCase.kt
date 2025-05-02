package com.example.fizyoapp.domain.usecase.mainscreen


import com.example.fizyoapp.data.repository.mainscreen.painrecord.PainRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestPainRecordUseCase @Inject constructor(
    private val painRepository: PainRepository
) {
    operator fun invoke(userId: String): Flow<Resource<PainRecord?>> {
        return painRepository.getLatestPainRecord(userId)
    }
}

class AddPainRecordUseCase @Inject constructor(
    private val painRepository: PainRepository
) {
    suspend operator fun invoke(painRecord: PainRecord): Resource<Unit> {
        return painRepository.addPainRecord(painRecord)
    }
}

class GetPainRecordsForUserUseCase @Inject constructor(
    private val painRepository: PainRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<PainRecord>>> {
        return painRepository.getPainRecordsForUser(userId)
    }
}