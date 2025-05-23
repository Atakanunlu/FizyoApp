package com.example.fizyoapp.domain.usecase.appointment

import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UnblockTimeSlotUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(blockedTimeSlotId: String): Flow<Resource<Boolean>> {
        return repository.unblockTimeSlot(blockedTimeSlotId)
    }
}