package com.example.fizyoapp.presentation.user.rehabilitation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.usecase.appointment.GetUserAppointmentsUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
data class AppointmentWithPhysiotherapist(
    val appointment: Appointment,
    val physiotherapistName: String,
    val physiotherapistPhotoUrl: String
)
@HiltViewModel
class RehabilitationHistoryViewModel @Inject constructor(
    private val getUserAppointmentsUseCase: GetUserAppointmentsUseCase,
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RehabilitationHistoryState())
    val state: StateFlow<RehabilitationHistoryState> = _state.asStateFlow()
    private var currentUserId: String? = null
    private var autoRefreshJob: Job? = null
    private val firestore = FirebaseFirestore.getInstance()
    init {
        loadCurrentUserAndAppointments()
        startAutoRefresh()
    }
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while(true) {
                delay(60000)
                currentUserId?.let { userId ->
                    refreshAppointments(userId)
                }
            }
        }
    }
    private fun loadCurrentUserAndAppointments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            currentUserId = user.id
                            loadAppointmentsForUser(user.id)
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Kullanıcı bilgisi alınamadı"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }
    fun loadAppointments() {
        currentUserId?.let { userId ->
            loadAppointmentsForUser(userId)
        } ?: run {
            loadCurrentUserAndAppointments()
        }
    }
    private fun refreshAppointments(userId: String) {
        viewModelScope.launch {
            try {
                getUserAppointmentsUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            processAppointments(result.data ?: emptyList())
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
    private fun loadAppointmentsForUser(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                getUserAppointmentsUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            processAppointments(result.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Randevular yüklenirken hata oluştu"
                            )
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Beklenmedik bir hata oluştu: ${e.message}"
                )
            }
        }
    }
    private suspend fun processAppointments(appointments: List<Appointment>) {
        val currentDate = Date()
        val pastAppointments = appointments.filter { appointment ->
            appointment.date.before(currentDate)
        }
        val appointmentsWithPhysiotherapists = mutableListOf<AppointmentWithPhysiotherapist>()
        for (appointment in pastAppointments) {
            try {
                var physiotherapist: PhysiotherapistProfile? = null
                try {
                    val physiotherapistResult = getPhysiotherapistByIdUseCase(appointment.physiotherapistId).first()
                    physiotherapist = when (physiotherapistResult) {
                        is Resource.Success -> physiotherapistResult.data
                        else -> null
                    }
                } catch (e: Exception) {
                }
                if (physiotherapist == null) {
                    try {
                        val profileDoc = firestore.collection("physiotherapist_profiles")
                            .document(appointment.physiotherapistId)
                            .get()
                            .await()
                        if (profileDoc.exists()) {
                            physiotherapist = profileDoc.toObject(PhysiotherapistProfile::class.java)
                        }
                    } catch (e: Exception) {
                    }
                }
                appointmentsWithPhysiotherapists.add(
                    AppointmentWithPhysiotherapist(
                        appointment = appointment,
                        physiotherapistName = if (physiotherapist != null &&
                            (physiotherapist.firstName.isNotEmpty() || physiotherapist.lastName.isNotEmpty()))
                            "${physiotherapist.firstName} ${physiotherapist.lastName}".trim()
                        else
                            "Fizyoterapist #${appointment.physiotherapistId.takeLast(4)}",
                        physiotherapistPhotoUrl = if (physiotherapist != null && physiotherapist.profilePhotoUrl.isNotEmpty())
                            physiotherapist.profilePhotoUrl
                        else
                            ""
                    )
                )
            } catch (e: Exception) {
                appointmentsWithPhysiotherapists.add(
                    AppointmentWithPhysiotherapist(
                        appointment = appointment,
                        physiotherapistName = "Fizyoterapist",
                        physiotherapistPhotoUrl = ""
                    )
                )
            }
        }
        appointmentsWithPhysiotherapists.sortByDescending { it.appointment.date }
        _state.value = _state.value.copy(
            isLoading = false,
            appointments = appointmentsWithPhysiotherapists
        )
    }
    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
data class RehabilitationHistoryState(
    val appointments: List<AppointmentWithPhysiotherapist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)