package com.example.fizyoapp.data.repository.appointment

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.appointment.AppointmentStatus
import com.example.fizyoapp.domain.model.appointment.BlockedTimeSlot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor() : AppointmentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val appointmentsCollection = firestore.collection("appointments")
    private val blockedTimeSlotsCollection = firestore.collection("blocked_time_slots")
    private val appointmentSlotsCollection = firestore.collection("appointment_slots")

    private val availableTimeSlots = listOf(
        "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"
    )

    override fun getAppointmentsForUser(userId: String): Flow<Resource<List<Appointment>>> = flow {
        try {
            emit(Resource.Loading())
            val querySnapshot = appointmentsCollection
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            val appointments = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
            emit(Resource.Success(appointments))
        } catch (e: Exception) {
            emit(Resource.Error("Randevularınız alınamadı: ${e.message}"))
        }
    }

    override fun observeAppointmentsForUser(userId: String): Flow<Resource<List<Appointment>>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            trySend(Resource.Loading())

            listenerRegistration = appointmentsCollection
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error("Randevular alınamadı: ${error.message}"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val appointments = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Resource.Success(appointments))
                    } else {
                        trySend(Resource.Success(emptyList()))
                    }
                }
        } catch (e: Exception) {
            trySend(Resource.Error("Randevular dinlenemedi: ${e.message}"))
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    override fun getAppointmentsForPhysiotherapist(physiotherapistId: String): Flow<Resource<List<Appointment>>> = flow {
        try {
            emit(Resource.Loading())
            val querySnapshot = appointmentsCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            val appointments = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
            emit(Resource.Success(appointments))
        } catch (e: Exception) {
            emit(Resource.Error("Randevular alınamadı: ${e.message}"))
        }
    }

    override fun observeAppointmentsForPhysiotherapist(physiotherapistId: String): Flow<Resource<List<Appointment>>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            trySend(Resource.Loading())

            listenerRegistration = appointmentsCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error("Randevular alınamadı: ${error.message}"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val appointments = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(Resource.Success(appointments))
                    } else {
                        trySend(Resource.Success(emptyList()))
                    }
                }
        } catch (e: Exception) {
            trySend(Resource.Error("Randevular dinlenemedi: ${e.message}"))
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    override fun createAppointment(appointment: Appointment): Flow<Resource<Appointment>> = flow {
        try {
            emit(Resource.Loading())
            val calendar = Calendar.getInstance()
            calendar.time = appointment.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.time
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(appointment.date)
            val slotId = "${appointment.physiotherapistId}_${dateStr}_${appointment.timeSlot.replace(":", "")}"
            val timeSlotDocRef = appointmentSlotsCollection.document(slotId)
            val timeSlotDoc = timeSlotDocRef.get().await()
            if (timeSlotDoc.exists()) {
                emit(Resource.Error("Bu saat dilimi dolu. Lütfen başka bir saat seçin."))
                return@flow
            }
            val userSlotsQuery = appointmentSlotsCollection
                .whereEqualTo("userId", appointment.userId)
                .whereEqualTo("timeSlot", appointment.timeSlot)
                .get()
                .await()
            val userHasExistingAppointment = userSlotsQuery.documents.any { doc ->
                val date = doc.getDate("date")
                date?.let {
                    val docDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(it)
                    docDateStr == dateStr
                } ?: false
            }
            if (userHasExistingAppointment) {
                emit(Resource.Error("Bu tarih ve saatte başka bir randevunuz bulunmaktadır."))
                return@flow
            }
            val blockedSlotQuery = blockedTimeSlotsCollection
                .whereEqualTo("physiotherapistId", appointment.physiotherapistId)
                .whereEqualTo("timeSlot", appointment.timeSlot)
                .get()
                .await()
            val isSlotBlocked = blockedSlotQuery.documents.any { doc ->
                val date = doc.getDate("date")
                date?.let {
                    val docDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(it)
                    docDateStr == dateStr
                } ?: false
            }
            if (isSlotBlocked) {
                emit(Resource.Error("Bu saat dilimi müsait değil. Lütfen başka bir saat seçin."))
                return@flow
            }
            val newAppointmentId = UUID.randomUUID().toString()
            val newAppointment = appointment.copy(
                id = newAppointmentId,
                createdAt = Date()
            )
            firestore.runBatch { batch ->
                batch.set(appointmentsCollection.document(newAppointmentId), newAppointment)
                val slotData = mapOf(
                    "physiotherapistId" to appointment.physiotherapistId,
                    "userId" to appointment.userId,
                    "date" to appointment.date,
                    "timeSlot" to appointment.timeSlot,
                    "appointmentId" to newAppointmentId
                )
                batch.set(timeSlotDocRef, slotData)
            }.await()

            // Firestore'un yeni veriyi indekslemesi için kısa bir gecikme ekleyelim
            kotlinx.coroutines.delay(300)

            emit(Resource.Success(newAppointment))
        } catch (e: Exception) {
            emit(Resource.Error("Randevu oluşturulamadı: ${e.message}"))
        }
    }

    override fun updateAppointment(appointment: Appointment): Flow<Resource<Appointment>> = flow {
        try {
            emit(Resource.Loading())
            appointmentsCollection.document(appointment.id).set(appointment).await()
            emit(Resource.Success(appointment))
        } catch (e: Exception) {
            emit(Resource.Error("Randevu güncellenemedi: ${e.message}"))
        }
    }

    override fun updateAppointmentNotes(appointmentId: String, notes: String): Flow<Resource<Appointment>> = flow {
        try {
            emit(Resource.Loading())

            val appointmentDoc = appointmentsCollection.document(appointmentId).get().await()
            if (!appointmentDoc.exists()) {
                emit(Resource.Error("Randevu bulunamadı"))
                return@flow
            }

            val appointment = appointmentDoc.toObject(Appointment::class.java)?.copy(id = appointmentId)
            if (appointment != null) {
                val updatedAppointment = appointment.copy(rehabilitationNotes = notes)
                appointmentsCollection.document(appointmentId).set(updatedAppointment).await()
                emit(Resource.Success(updatedAppointment))
            } else {
                emit(Resource.Error("Randevu bilgileri alınamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Notlar güncellenemedi: ${e.message}"))
        }
    }

    override fun cancelAppointment(appointmentId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val appointmentDoc = appointmentsCollection.document(appointmentId).get().await()
            if (!appointmentDoc.exists()) {
                emit(Resource.Error("Randevu bulunamadı"))
                return@flow
            }

            val appointment = appointmentDoc.toObject(Appointment::class.java)?.copy(id = appointmentId)
            if (appointment != null) {
                val updatedAppointment = appointment.copy(
                    status = AppointmentStatus.CANCELLED,
                    cancelledAt = Date()
                )

                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(appointment.date)
                val slotId = "${appointment.physiotherapistId}_${dateStr}_${appointment.timeSlot.replace(":", "")}"

                firestore.runBatch { batch ->
                    batch.set(appointmentsCollection.document(appointmentId), updatedAppointment)
                    batch.delete(appointmentSlotsCollection.document(slotId))
                }.await()

                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Randevu bilgileri alınamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Randevu iptal edilemedi: ${e.message}"))
        }
    }

    override fun cancelAppointmentWithRole(appointmentId: String, cancelledBy: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val appointmentDoc = appointmentsCollection.document(appointmentId).get().await()
            if (!appointmentDoc.exists()) {
                emit(Resource.Error("Randevu bulunamadı"))
                return@flow
            }

            val appointment = appointmentDoc.toObject(Appointment::class.java)?.copy(id = appointmentId)
            if (appointment != null) {
                val updatedAppointment = appointment.copy(
                    status = AppointmentStatus.CANCELLED,
                    cancelledBy = cancelledBy,
                    cancelledAt = Date()
                )

                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(appointment.date)
                val slotId = "${appointment.physiotherapistId}_${dateStr}_${appointment.timeSlot.replace(":", "")}"

                firestore.runBatch { batch ->
                    batch.set(appointmentsCollection.document(appointmentId), updatedAppointment)
                    batch.delete(appointmentSlotsCollection.document(slotId))
                }.await()

                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Randevu bilgileri alınamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Randevu iptal edilemedi: ${e.message}"))
        }
    }

    override fun blockTimeSlot(blockedTimeSlot: BlockedTimeSlot): Flow<Resource<BlockedTimeSlot>> = flow {
        try {
            emit(Resource.Loading())

            val calendar = Calendar.getInstance()
            calendar.time = blockedTimeSlot.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.time

            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(blockedTimeSlot.date)
            val slotId = "${blockedTimeSlot.physiotherapistId}_${dateStr}_${blockedTimeSlot.timeSlot.replace(":", "")}"

            val timeSlotDocRef = appointmentSlotsCollection.document(slotId)
            val timeSlotDoc = timeSlotDocRef.get().await()

            if (timeSlotDoc.exists()) {
                emit(Resource.Error("Bu saatte zaten bir randevu var, bloke edilemez."))
                return@flow
            }

            val existingAppointments = appointmentsCollection
                .whereEqualTo("physiotherapistId", blockedTimeSlot.physiotherapistId)
                .whereEqualTo("timeSlot", blockedTimeSlot.timeSlot)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .whereNotEqualTo("status", AppointmentStatus.CANCELLED.name)
                .get()
                .await()

            if (!existingAppointments.isEmpty) {
                emit(Resource.Error("Bu saatte zaten bir randevu var, bloke edilemez."))
                return@flow
            }

            val blockedSlotId = UUID.randomUUID().toString()
            val newBlockedSlot = blockedTimeSlot.copy(id = blockedSlotId)

            firestore.runBatch { batch ->
                batch.set(blockedTimeSlotsCollection.document(blockedSlotId), newBlockedSlot)

                val slotData = mapOf(
                    "physiotherapistId" to blockedTimeSlot.physiotherapistId,
                    "date" to blockedTimeSlot.date,
                    "timeSlot" to blockedTimeSlot.timeSlot,
                    "isBlocked" to true,
                    "blockedSlotId" to blockedSlotId
                )
                batch.set(timeSlotDocRef, slotData)
            }.await()

            emit(Resource.Success(newBlockedSlot))
        } catch (e: Exception) {
            emit(Resource.Error("Saat dilimi bloke edilemedi: ${e.message}"))
        }
    }

    override fun unblockTimeSlot(blockedTimeSlotId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())

            val blockedSlotDoc = blockedTimeSlotsCollection.document(blockedTimeSlotId).get().await()
            if (!blockedSlotDoc.exists()) {
                emit(Resource.Error("Bloke edilmiş zaman dilimi bulunamadı"))
                return@flow
            }

            val blockedSlot = blockedSlotDoc.toObject(BlockedTimeSlot::class.java)
            if (blockedSlot != null) {
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(blockedSlot.date)
                val slotId = "${blockedSlot.physiotherapistId}_${dateStr}_${blockedSlot.timeSlot.replace(":", "")}"

                firestore.runBatch { batch ->
                    batch.delete(blockedTimeSlotsCollection.document(blockedTimeSlotId))
                    batch.delete(appointmentSlotsCollection.document(slotId))
                }.await()

                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Bloke edilmiş zaman dilimi bilgileri alınamadı"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Saat dilimi blokajı kaldırılamadı: ${e.message}"))
        }
    }

    override fun getBlockedTimeSlotsForPhysiotherapist(physiotherapistId: String): Flow<Resource<List<BlockedTimeSlot>>> = flow {
        try {
            emit(Resource.Loading())
            val querySnapshot = blockedTimeSlotsCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            val blockedSlots = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(BlockedTimeSlot::class.java)?.copy(id = doc.id)
            }
            emit(Resource.Success(blockedSlots))
        } catch (e: Exception) {
            emit(Resource.Error("Bloke edilen saatler alınamadı: ${e.message}"))
        }
    }

    override fun getBlockedTimeSlotsForDate(physiotherapistId: String, date: Date): Flow<Resource<List<BlockedTimeSlot>>> = flow {
        try {
            emit(Resource.Loading())

            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.time

            val querySnapshot = blockedTimeSlotsCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .await()

            val blockedSlots = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(BlockedTimeSlot::class.java)?.copy(id = doc.id)
            }
            emit(Resource.Success(blockedSlots))
        } catch (e: Exception) {
            emit(Resource.Error("Bloke edilen saatler alınamadı: ${e.message}"))
        }
    }

    override fun getAvailableTimeSlots(physiotherapistId: String, date: Date): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())

            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.time

            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
            val unavailableSlots = mutableSetOf<String>()

            val reservedSlotsQuery = appointmentSlotsCollection
                .whereEqualTo("physiotherapistId", physiotherapistId)
                .get()
                .await()

            reservedSlotsQuery.documents.forEach { doc ->
                val slotDate = doc.getDate("date")
                val timeSlot = doc.getString("timeSlot")
                if (slotDate != null && timeSlot != null) {
                    val slotDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(slotDate)
                    if (slotDateStr == dateStr) {
                        unavailableSlots.add(timeSlot)
                    }
                }
            }

            val availableSlots = availableTimeSlots.filter { it !in unavailableSlots }
            emit(Resource.Success(availableSlots))
        } catch (e: Exception) {
            emit(Resource.Error("Müsait saatler alınamadı: ${e.message}"))
        }
    }
}