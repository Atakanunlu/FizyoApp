package com.example.fizyoapp.presentation.user.rehabilitation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.appointment.AppointmentStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AppointmentWithPhysiotherapist(
    val appointment: Appointment,
    val physiotherapistName: String,
    val physiotherapistPhotoUrl: String
)

data class RehabilitationHistoryState(
    val pastAppointments: List<AppointmentWithPhysiotherapist> = emptyList(),
    val upcomingAppointments: List<AppointmentWithPhysiotherapist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isFirstLoad: Boolean = true
)

sealed class RehabilitationHistoryEvent {
    object Refresh : RehabilitationHistoryEvent()
    data class CancelAppointment(val appointmentId: String) : RehabilitationHistoryEvent()
}

@HiltViewModel
class RehabilitationHistoryViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(RehabilitationHistoryState())
    val state: StateFlow<RehabilitationHistoryState> = _state.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var appointmentScope: CoroutineScope? = null


    private var cachedAppointments: List<Appointment>? = null
    private val physiotherapistCache = mutableMapOf<String, Pair<String, String>>()

    init {
        Log.d("RehabViewModel", "ViewModel oluşturuldu")
    }

    fun onEvent(event: RehabilitationHistoryEvent) {
        when (event) {
            is RehabilitationHistoryEvent.Refresh -> {
                refreshAppointments(forceRefresh = false)
            }
            is RehabilitationHistoryEvent.CancelAppointment -> {
                cancelAppointment(event.appointmentId)
            }
        }
    }

    private fun refreshAppointments(forceRefresh: Boolean = false) {
        Log.d("RehabViewModel", "Randevular yenileniyor")


        appointmentScope?.cancel()


        appointmentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())


        _state.value = _state.value.copy(isLoading = true, error = null)

        appointmentScope?.launch {
            try {
                val userId = auth.currentUser?.uid

                if (userId == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Oturum açmanız gerekiyor"
                    )
                    return@launch
                }


                if (!forceRefresh && cachedAppointments != null && !_state.value.isFirstLoad) {

                    Log.d("RehabViewModel", "Önbellekten ${cachedAppointments?.size} randevu yükleniyor")

                    processAppointments(cachedAppointments!!)

                    launch {
                        try {
                            fetchAppointmentsFromFirestore(userId)
                        } catch (e: Exception) {
                            Log.e("RehabViewModel", "Arka planda yenileme hatası: ${e.message}", e)
                        }
                    }
                } else {

                    fetchAppointmentsFromFirestore(userId)
                }

            } catch (e: Exception) {
                Log.e("RehabViewModel", "Genel yükleme hatası: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Randevular yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    private suspend fun fetchAppointmentsFromFirestore(userId: String) {
        try {
            val appointments = withContext(Dispatchers.IO) {
                val snapshot = firestore.collection("appointments")
                    .whereEqualTo("userId", userId)
                    .get(Source.SERVER)
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    try {
                        val appointment = doc.toObject(Appointment::class.java)
                        appointment?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("RehabViewModel", "Randevu dönüşüm hatası: ${e.message}", e)
                        null
                    }
                }
            }

            Log.d("RehabViewModel", "${appointments.size} randevu bulundu")

            cachedAppointments = appointments

            processAppointments(appointments)

        } catch (e: Exception) {
            Log.e("RehabViewModel", "Firestore'dan veri çekme hatası: ${e.message}", e)
            if (cachedAppointments != null) {
                _state.value = _state.value.copy(
                    error = "Güncel veriler alınamadı, önbellekteki veriler gösteriliyor: ${e.message}"
                )
                processAppointments(cachedAppointments!!)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Randevular yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    private suspend fun processAppointments(appointments: List<Appointment>) {
        try {
            val physiotherapistIds = appointments.map { it.physiotherapistId }.distinct()


            val newPhysiotherapistInfo = withContext(Dispatchers.IO) {
                val results = mutableMapOf<String, Pair<String, String>>()
                val idsToFetch = physiotherapistIds.filter { !physiotherapistCache.containsKey(it) }
                val deferreds = idsToFetch.map { id ->
                    async {
                        try {
                            val physiotherapistDoc = firestore.collection("physiotherapist_profiles")
                                .document(id)
                                .get()
                                .await()

                            if (physiotherapistDoc.exists()) {
                                val firstName = physiotherapistDoc.getString("firstName") ?: ""
                                val lastName = physiotherapistDoc.getString("lastName") ?: ""
                                val name = "$firstName $lastName".trim().ifEmpty {
                                    "Fizyoterapist #${id.takeLast(4)}"
                                }
                                val photoUrl = physiotherapistDoc.getString("profilePhotoUrl") ?: ""
                                id to Pair(name, photoUrl)
                            } else {
                                id to Pair("Fizyoterapist #${id.takeLast(4)}", "")
                            }
                        } catch (e: Exception) {
                            Log.e("RehabViewModel", "Fizyoterapist bilgisi alınırken hata: ${e.message}", e)
                            id to Pair("Fizyoterapist", "")
                        }
                    }
                }

                deferreds.forEach { deferred ->
                    val (id, info) = deferred.await()
                    results[id] = info
                }

                results
            }

            physiotherapistCache.putAll(newPhysiotherapistInfo)


            val appointmentsWithDetails = appointments.map { appointment ->
                val (name, photoUrl) = physiotherapistCache[appointment.physiotherapistId]
                    ?: Pair("Fizyoterapist", "")

                AppointmentWithPhysiotherapist(
                    appointment = appointment,
                    physiotherapistName = name,
                    physiotherapistPhotoUrl = photoUrl
                )
            }


            val currentDateTime = Date()

            val pastAppointmentsList = mutableListOf<AppointmentWithPhysiotherapist>()
            val upcomingAppointmentsList = mutableListOf<AppointmentWithPhysiotherapist>()

            for (appointmentWithDetails in appointmentsWithDetails) {
                val appointment = appointmentWithDetails.appointment


                val appointmentDateTime = calculateExactAppointmentDateTime(appointment)


                if (appointmentDateTime.before(currentDateTime)) {

                    pastAppointmentsList.add(appointmentWithDetails)
                } else {

                    val isNotCancelledByUser = appointment.status != AppointmentStatus.CANCELLED ||
                            appointment.cancelledBy == "physiotherapist"
                    if (isNotCancelledByUser) {
                        upcomingAppointmentsList.add(appointmentWithDetails)
                    }
                }
            }


            pastAppointmentsList.sortByDescending { calculateExactAppointmentDateTime(it.appointment) }
            upcomingAppointmentsList.sortBy { calculateExactAppointmentDateTime(it.appointment) }

            _state.value = _state.value.copy(
                pastAppointments = pastAppointmentsList,
                upcomingAppointments = upcomingAppointmentsList,
                isLoading = false,
                isFirstLoad = false
            )

            Log.d("RehabViewModel", "Randevu yükleme tamamlandı: ${pastAppointmentsList.size} geçmiş, ${upcomingAppointmentsList.size} yaklaşan")

        } catch (e: Exception) {
            Log.e("RehabViewModel", "Randevuları işlerken hata: ${e.message}", e)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Randevular işlenirken hata oluştu: ${e.message}"
            )
        }
    }


    private fun calculateExactAppointmentDateTime(appointment: Appointment): Date {
        try {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = appointment.date

            val timeParts = appointment.timeSlot.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: 0
                val minute = timeParts[1].toIntOrNull() ?: 0


                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                calendar.set(java.util.Calendar.MINUTE, minute)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
            }

            return calendar.time
        } catch (e: Exception) {
            Log.e("RehabViewModel", "Randevu zamanı hesaplanamadı: ${e.message}", e)
            return appointment.date
        }
    }

    private fun cancelAppointment(appointmentId: String) {
        Log.d("RehabViewModel", "Randevu iptal ediliyor: $appointmentId")

        _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
        appointmentScope?.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                val appointmentDoc = withContext(Dispatchers.IO) {
                    db.collection("appointments").document(appointmentId).get().await()
                }
                if (!appointmentDoc.exists()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Randevu bulunamadı"
                    )
                    return@launch
                }
                val appointment = appointmentDoc.toObject(Appointment::class.java)?.copy(id = appointmentId)
                if (appointment == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Randevu bilgileri alınamadı"
                    )
                    return@launch
                }

                val updatedAppointment = appointment.copy(
                    status = AppointmentStatus.CANCELLED,
                    cancelledBy = "user",
                    cancelledAt = Date()
                )
                withContext(Dispatchers.IO) {
                    db.collection("appointments").document(appointmentId).set(updatedAppointment).await()

                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(appointment.date)
                    val slotId = "${appointment.physiotherapistId}_${dateStr}_${appointment.timeSlot.replace(":", "")}"
                    db.collection("appointment_slots").document(slotId).delete().await()
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Randevu başarıyla iptal edildi"
                )


                startSuccessMessageTimer()


                refreshAppointments()
            } catch (e: Exception) {
                Log.e("RehabViewModel", "Randevu iptal edilirken hata: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Randevu iptal edilemedi: ${e.message}"
                )
            }
        }
    }


    private var successMessageJob: Job? = null


    private fun startSuccessMessageTimer() {

        successMessageJob?.cancel()


        successMessageJob = viewModelScope.launch {
            delay(3000)
            clearSuccessMessage()
        }
    }

    fun clearSuccessMessage() {
        _state.value = _state.value.copy(successMessage = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("RehabViewModel", "ViewModel temizleniyor")
        appointmentScope?.cancel()
        appointmentScope = null
    }
}