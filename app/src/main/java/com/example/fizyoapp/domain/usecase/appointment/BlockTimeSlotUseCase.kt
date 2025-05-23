package com.example.fizyoapp.domain.usecase.appointment

import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.BlockedTimeSlot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlockTimeSlotUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(blockedTimeSlot: BlockedTimeSlot): Flow<Resource<BlockedTimeSlot>> {
        return repository.blockTimeSlot(blockedTimeSlot)
    }
}