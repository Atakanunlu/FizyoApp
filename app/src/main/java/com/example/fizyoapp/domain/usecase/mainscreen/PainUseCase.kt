
package com.example.fizyoapp.domain.usecase.mainscreen



import com.example.fizyoapp.data.repository.mainscreen.painrecord.PainTrackingRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestPainRecordUseCase @Inject constructor(
    private val repository: PainTrackingRepository
) {
    operator fun invoke(userId: String): Flow<Resource<PainRecord?>> {
        return repository.getLatestPainRecord(userId)
    }
}



class GetPainRecordsUseCase @Inject constructor(
    private val repository: PainTrackingRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<PainRecord>>> {
        return repository.getPainRecords(userId)
    }
}



class AddPainRecordUseCase @Inject constructor(
    private val repository: PainTrackingRepository
) {
    operator fun invoke(painRecord: PainRecord): Flow<Resource<Boolean>> {
        return repository.addPainRecord(painRecord)
    }
}

class UpdatePainRecordUseCase @Inject constructor(
    private val repository: PainTrackingRepository
) {
    operator fun invoke(painRecord: PainRecord): Flow<Resource<Boolean>> {
        return repository.updatePainRecord(painRecord)
    }
}
class DeletePainRecordUseCase @Inject constructor(
    private val repository: PainTrackingRepository
) {
    operator fun invoke(painRecordId: String): Flow<Resource<Boolean>> {
        return repository.deletePainRecord(painRecordId)
    }
}