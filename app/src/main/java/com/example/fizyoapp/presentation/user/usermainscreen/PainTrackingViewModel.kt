package com.example.fizyoapp.presentation.user.usermainscreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.AddPainRecordUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.DeletePainRecordUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetPainRecordsUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.UpdatePainRecordUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PainTrackingViewModel @Inject constructor(
    private val getCurrentUseCase: GetCurrentUseCase,
    private val getPainRecordsUseCase: GetPainRecordsUseCase,
    private val addPainRecordUseCase: AddPainRecordUseCase,
    private val updatePainRecordUseCase: UpdatePainRecordUseCase,
    private val deletePainRecordUseCase: DeletePainRecordUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(PainTrackingState())
    val state: StateFlow<PainTrackingState> = _state
    private var userId: String? = null

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        userId = result.data?.id
                        if (userId != null) {
                            fetchPainRecords(userId!!)
                        } else {
                            _state.update { it.copy(error = "Kullanıcı bilgisi bulunamadı") }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Kullanıcı bilgisi alınamadı",
                                isLoading = false
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

    fun onEvent(event: PainTrackingEvent) {
        when (event) {
            is PainTrackingEvent.UpdatePainIntensity -> {
                _state.update { it.copy(painIntensity = event.intensity) }
            }
            is PainTrackingEvent.UpdatePainLocation -> {
                _state.update { it.copy(painLocation = event.location) }
            }
            is PainTrackingEvent.UpdatePainDescription -> {
                _state.update { it.copy(painDescription = event.description) }
            }
            is PainTrackingEvent.ToggleAddRecord -> {
                _state.update {
                    it.copy(
                        isAddingRecord = !it.isAddingRecord,
                        currentPainRecord = null,
                        painIntensity = 0,
                        painLocation = "",
                        painDescription = ""
                    )
                }
            }
            is PainTrackingEvent.SavePainRecord -> {
                savePainRecord()
            }
            is PainTrackingEvent.EditPainRecord -> {
                _state.update {
                    it.copy(
                        isAddingRecord = true,
                        currentPainRecord = event.painRecord,
                        painIntensity = event.painRecord.intensity,
                        painLocation = event.painRecord.location,
                        painDescription = event.painRecord.note ?: ""
                    )
                }
            }
            is PainTrackingEvent.DeletePainRecord -> {
                deletePainRecord(event.painRecordId)
            }
            is PainTrackingEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
            is PainTrackingEvent.RefreshData -> {
                userId?.let { fetchPainRecords(it) }
            }
            else -> {}
        }
    }

    private fun fetchPainRecords(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getPainRecordsUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                painRecords = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Ağrı kayıtları alınamadı",
                                isLoading = false
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

    private fun savePainRecord() {
        val currentState = _state.value
        val auth = FirebaseAuth.getInstance()
        val firebaseUserId = auth.currentUser?.uid
        val userId = firebaseUserId ?: this.userId

        if (userId == null) {
            _state.update { it.copy(error = "Kullanıcı bilgisi alınamadı. Lütfen tekrar giriş yapın.") }
            return
        }

        if (currentState.painLocation.isBlank()) {
            _state.update { it.copy(error = "Lütfen ağrı lokasyonunu belirtin") }
            return
        }

        val recordId = currentState.currentPainRecord?.id ?: UUID.randomUUID().toString()
        val painRecord = PainRecord(
            id = recordId,
            userId = userId,
            intensity = currentState.painIntensity,
            location = currentState.painLocation,
            note = currentState.painDescription.takeIf { it.isNotBlank() },
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val useCase = if (currentState.currentPainRecord != null) {
                updatePainRecordUseCase(painRecord)
            } else {
                addPainRecordUseCase(painRecord)
            }
            useCase.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isAddingRecord = false,
                                currentPainRecord = null,
                                painIntensity = 0,
                                painLocation = "",
                                painDescription = ""
                            )
                        }
                        fetchPainRecords(userId)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Ağrı kaydı kaydedilemedi",
                                isLoading = false
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

    private fun deletePainRecord(painRecordId: String) {
        val userId = this.userId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            deletePainRecordUseCase(painRecordId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        fetchPainRecords(userId)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Ağrı kaydı silinemedi",
                                isLoading = false
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
