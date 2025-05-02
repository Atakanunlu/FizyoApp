package com.example.fizyoapp.presentation.user.usermainscreen

import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetPainRecordsForUserUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PainTrackingViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getPainRecordsForUserUseCase: GetPainRecordsForUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PainTrackingState())
    val state: StateFlow<PainTrackingState> = _state.asStateFlow()

    init {
        loadPainRecords()
    }

    fun onEvent(event: PainTrackingEvent) {
        when (event) {
            is PainTrackingEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
            is PainTrackingEvent.DeletePainRecord -> {
                // Delete pain record implementation
            }
        }
    }

    private fun loadPainRecords() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getCurrentUserUseCase().collect { userResult ->
                when (userResult) {
                    is Resource.Success -> {
                        val userId = userResult.data?.id
                        if (userId != null) {
                            // Kullanıcı ağrı kayıtlarını al
                            getPainRecordsForUserUseCase(userId).collect { result ->
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
                        } else {
                            _state.update {
                                it.copy(
                                    error = "Kullanıcı bilgisi bulunamadı",
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = userResult.message ?: "Kullanıcı bilgisi alınamadı",
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

data class PainTrackingState(
    val painRecords: List<PainRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class PainTrackingEvent {
    object DismissError : PainTrackingEvent()
    data class DeletePainRecord(val recordId: String) : PainTrackingEvent()
}