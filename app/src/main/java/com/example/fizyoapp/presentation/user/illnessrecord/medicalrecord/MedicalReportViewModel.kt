package com.example.fizyoapp.presentation.user.illnessrecord.medicalrecord


import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.illnessrecordscreen.medicalrecord.MedicalReportRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MedicalReportViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val medicalReportRepository: MedicalReportRepository,
    private val messagesRepository: MessagesRepository,
    private val storage: FirebaseStorage
) : ViewModel() {
    private val _state = MutableStateFlow(MedicalReportState())
    val state: StateFlow<MedicalReportState> = _state.asStateFlow()

    private var selectedFileUri: Uri? = null

    init {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                _state.update { it.copy(currentUserId = userId) }

                // Öncelikle threadleri yükle, sonra diğer verileri
                loadRecentThreads(userId)
                // Kısa bir gecikme ekleyelim
                delay(200)
                // Diğer verileri yükle
                loadMedicalReports(userId)
            }
        }
    }

    fun onEvent(event: MedicalReportEvent) {
        when (event) {
            is MedicalReportEvent.RefreshData -> {
                loadData()
            }
            is MedicalReportEvent.DismissError -> {
                _state.update { it.copy(actionError = null) }
            }
            is MedicalReportEvent.FileSelected -> {
                selectedFileUri = event.uri
            }
            is MedicalReportEvent.AddReport -> {
                addReport(event.title, event.description, event.doctorName, event.hospitalName)
            }
            is MedicalReportEvent.ShareReport -> {
                shareReport(event.reportId, event.userId)
            }
            is MedicalReportEvent.DeleteReport -> {
                deleteReport(event.fileUrl)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "You need to sign in"
                    )
                }
                return@launch
            }
            val userId = currentUser.uid
            _state.update { it.copy(currentUserId = userId) }
            loadMedicalReports(userId)
            loadRecentThreads(userId)
        }
    }

    private suspend fun loadMedicalReports(userId: String) {
        medicalReportRepository.getMedicalReports(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            reports = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load medical reports"
                        )
                    }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private suspend fun loadRecentThreads(userId: String) {
        messagesRepository.getChatTreadsForUser(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(recentThreads = result.data ?: emptyList())
                    }
                    Log.d("MedicalReportVM", "Loaded threads: ${result.data?.size ?: 0}")
                }
                is Resource.Error -> {
                    Log.e("MedicalReportVM", "Error loading threads: ${result.message}")
                }
                is Resource.Loading -> {
                    // Yükleme durumu
                }
            }
        }
    }
    private fun addReport(title: String, description: String, doctorName: String, hospitalName: String) {
        if (selectedFileUri == null) {
            _state.update {
                it.copy(actionError = "No valid file selected")
            }
            return
        }

        val userId = state.value.currentUserId
        if (userId.isBlank()) {
            _state.update {
                it.copy(actionError = "User information could not be retrieved")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            medicalReportRepository.uploadMedicalReport(
                fileUri = selectedFileUri!!,
                title = title,
                description = description,
                userId = userId,
                doctorName = doctorName,
                hospitalName = hospitalName
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Medical report successfully saved"
                            )
                        }
                        selectedFileUri = null
                        delay(500)
                        loadMedicalReports(userId)
                        delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                actionError = result.message ?: "An error occurred while uploading the report"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun shareReport(reportId: String, receiverId: String) {
        val userId = state.value.currentUserId
        if (userId.isBlank()) {
            _state.update {
                it.copy(actionError = "User information could not be retrieved")
            }
            return
        }

        val reportToShare = state.value.reports.find { it.id == reportId }
        if (reportToShare == null) {
            _state.update {
                it.copy(actionError = "Report to share not found")
            }
            return
        }

        viewModelScope.launch {
            try {
                val reportData = JSONObject().apply {
                    put("title", reportToShare.title)
                    put("description", reportToShare.description)
                    put("fileUrl", reportToShare.fileUrl)
                    put("timestamp", reportToShare.timestamp.time.toString())
                    put("doctorName", reportToShare.doctorName)
                    put("hospitalName", reportToShare.hospitalName)
                }

                val messageContent = "[MEDICAL_REPORT]\n${reportData}"

                val message = Message(
                    id = "",
                    senderId = userId,
                    receiverId = receiverId,
                    content = messageContent,
                    timestamp = Date(),
                    isRead = false,
                    threadId = ""  // Repository will create thread ID
                )

                messagesRepository.sendMessage(message).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    successMessage = "Medical report successfully shared"
                                )
                            }
                            delay(3000)
                            _state.update { it.copy(successMessage = null) }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    actionError = result.message ?: "An error occurred while sharing the report"
                                )
                            }
                        }
                        is Resource.Loading -> {
                            // Handle loading if needed
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(actionError = "An error occurred while sharing the report: ${e.message}")
                }
            }
        }
    }

    private fun deleteReport(fileUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            medicalReportRepository.deleteMedicalReport(fileUrl).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Medical report successfully deleted"
                            )
                        }
                        // Refresh data
                        delay(500)
                        loadMedicalReports(state.value.currentUserId)
                        // Auto close success message
                        delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                actionError = result.message ?: "An error occurred while deleting the report"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}