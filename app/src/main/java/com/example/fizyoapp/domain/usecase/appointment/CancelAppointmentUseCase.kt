package com.example.fizyoapp.domain.usecase.appointment

import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CancelAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(appointmentId: String): Flow<Resource<Boolean>> {
        return repository.cancelAppointment(appointmentId)
    }
}