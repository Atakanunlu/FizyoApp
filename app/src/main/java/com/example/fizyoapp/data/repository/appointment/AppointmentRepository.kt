package com.example.fizyoapp.data.repository.appointment

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.appointment.BlockedTimeSlot
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface AppointmentRepository {
    fun getAppointmentsForUser(userId: String): Flow<Resource<List<Appointment>>>
    fun getAppointmentsForPhysiotherapist(physiotherapistId: String): Flow<Resource<List<Appointment>>>
    fun createAppointment(appointment: Appointment): Flow<Resource<Appointment>>
    fun updateAppointment(appointment: Appointment): Flow<Resource<Appointment>>
    fun updateAppointmentNotes(appointmentId: String, notes: String): Flow<Resource<Appointment>>
    fun cancelAppointment(appointmentId: String): Flow<Resource<Boolean>>
    fun cancelAppointmentWithRole(appointmentId: String, cancelledBy: String): Flow<Resource<Boolean>>
    fun blockTimeSlot(blockedTimeSlot: BlockedTimeSlot): Flow<Resource<BlockedTimeSlot>>
    fun unblockTimeSlot(blockedTimeSlotId: String): Flow<Resource<Boolean>>
    fun getBlockedTimeSlotsForPhysiotherapist(physiotherapistId: String): Flow<Resource<List<BlockedTimeSlot>>>
    fun getBlockedTimeSlotsForDate(physiotherapistId: String, date: Date): Flow<Resource<List<BlockedTimeSlot>>>
    fun getAvailableTimeSlots(physiotherapistId: String, date: Date): Flow<Resource<List<String>>>
    fun observeAppointmentsForUser(userId: String): Flow<Resource<List<Appointment>>> {
        return getAppointmentsForUser(userId)
    }
    fun observeAppointmentsForPhysiotherapist(physiotherapistId: String): Flow<Resource<List<Appointment>>> {
        return getAppointmentsForPhysiotherapist(physiotherapistId)
    }
}