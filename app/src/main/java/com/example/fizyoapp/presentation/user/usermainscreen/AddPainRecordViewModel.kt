package com.example.fizyoapp.presentation.user.usermainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.AddPainRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPainRecordViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val addPainRecordUseCase: AddPainRecordUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddPainRecordState())
    val state: StateFlow<AddPainRecordState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: AddPainRecordEvent) {
        when (event) {
            is AddPainRecordEvent.SetIntensity -> {
                _state.update { it.copy(intensity = event.value) }
            }
            is AddPainRecordEvent.SetLocation -> {
                _state.update { it.copy(location = event.value) }
            }
            is AddPainRecordEvent.SetNote -> {
                _state.update { it.copy(note = event.value) }
            }
            is AddPainRecordEvent.SubmitRecord -> {
                addPainRecord()
            }
            is AddPainRecordEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun addPainRecord() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val intensity = state.value.intensity
            val location = state.value.location
            val note = state.value.note

            if (intensity <= 0) {
                _state.update {
                    it.copy(
                        error = "Lütfen ağrı şiddetini seçin",
                        isSubmitting = false
                    )
                }
                return@launch
            }

            if (location.isBlank()) {
                _state.update {
                    it.copy(
                        error = "Lütfen ağrı lokasyonunu girin",
                        isSubmitting = false
                    )
                }
                return@launch
            }

            try {
                // GetCurrentUseCase'in Flow ya da Resource tipinde dönüş yaptığını varsayalım
                getCurrentUserUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val currentUser = result.data
                            if (currentUser != null) {
                                val userId = currentUser.id ?: ""  // Burada user modeline bağlı olarak doğru alan adını kullanın

                                if (userId.isNotEmpty()) {
                                    val painRecord = PainRecord(
                                        userId = userId,
                                        intensity = intensity,
                                        location = location,
                                        timestamp = System.currentTimeMillis(),
                                        note = if (note.isBlank()) null else note
                                    )

                                    val addResult = addPainRecordUseCase(painRecord)
                                    when (addResult) {
                                        is Resource.Success -> {
                                            _uiEvent.send(UiEvent.RecordAdded)
                                        }
                                        is Resource.Error -> {
                                            _state.update {
                                                it.copy(
                                                    error = addResult.message ?: "Ağrı kaydı eklenemedi",
                                                    isSubmitting = false
                                                )
                                            }
                                        }
                                        is Resource.Loading -> {
                                            // Bu durum burada ele alınmıyor
                                        }
                                    }
                                } else {
                                    _state.update {
                                        it.copy(
                                            error = "Kullanıcı ID'si boş",
                                            isSubmitting = false
                                        )
                                    }
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        error = "Kullanıcı bilgisi bulunamadı",
                                        isSubmitting = false
                                    )
                                }
                            }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    error = result.message ?: "Kullanıcı bilgisi alınamadı",
                                    isSubmitting = false
                                )
                            }
                        }
                        is Resource.Loading -> {
                            // Yükleme durumu burada ele alınmıyor
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Hata: ${e.message ?: "Bilinmeyen hata"}",
                        isSubmitting = false
                    )
                }
            }
        }
    }

    sealed class UiEvent {
        object RecordAdded : UiEvent()
    }
}

data class AddPainRecordState(
    val intensity: Int = 0,
    val location: String = "",
    val note: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed class AddPainRecordEvent {
    data class SetIntensity(val value: Int) : AddPainRecordEvent()
    data class SetLocation(val value: String) : AddPainRecordEvent()
    data class SetNote(val value: String) : AddPainRecordEvent()
    object SubmitRecord : AddPainRecordEvent()
    object DismissError : AddPainRecordEvent()
}