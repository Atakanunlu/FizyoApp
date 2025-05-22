package com.example.fizyoapp.domain.usecase.appointment

import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhysiotherapistAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(physiotherapistId: String): Flow<Resource<List<Appointment>>> {
        return repository.getAppointmentsForPhysiotherapist(physiotherapistId)
    }
}