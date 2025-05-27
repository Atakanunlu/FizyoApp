package com.example.fizyoapp.domain.usecase.appointment

import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CancelAppointmentWithRoleUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(appointmentId: String, cancelledBy: String): Flow<Resource<Boolean>> {
        return repository.cancelAppointmentWithRole(appointmentId, cancelledBy)
    }
}