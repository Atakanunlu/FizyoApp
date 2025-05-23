package com.example.fizyoapp.domain.usecase.appointment

import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class GetAvailableTimeSlotsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(physiotherapistId: String, date: Date): Flow<Resource<List<String>>> {
        return repository.getAvailableTimeSlots(physiotherapistId, date)
    }
}