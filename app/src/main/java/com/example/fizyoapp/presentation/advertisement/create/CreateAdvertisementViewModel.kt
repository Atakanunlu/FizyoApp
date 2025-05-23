package com.example.fizyoapp.presentation.advertisement.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.repository.advertisement.AdvertisementDataRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.advertisement.CheckActiveAdvertisementByPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentPhysiotherapistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAdvertisementViewModel @Inject constructor(
    private val getCurrentPhysiotherapistUseCase: GetCurrentPhysiotherapistUseCase,
    private val checkActiveAdvertisementByPhysiotherapistUseCase: CheckActiveAdvertisementByPhysiotherapistUseCase,
    private val advertisementDataRepository: AdvertisementDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateAdvertisementState())
    val state: StateFlow<CreateAdvertisementState> = _state.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getCurrentPhysiotherapistUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val physiotherapistId = result.data.id
                        checkActiveAdvertisement(physiotherapistId)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Kullanıcı bilgisi alınamadı"
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

    private fun checkActiveAdvertisement(physiotherapistId: String) {
        viewModelScope.launch {
            checkActiveAdvertisementByPhysiotherapistUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                hasActiveAdvertisement = result.data,
                                physiotherapistId = physiotherapistId
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Aktif reklam kontrolü yapılamadı"
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

    fun onContinueClicked() {
        _state.update { it.copy(navigateToPayment = true) }
    }

    fun setImageUri(uri: Uri) {
        advertisementDataRepository.setImageUri(uri)
    }

    fun setDescription(description: String) {
        advertisementDataRepository.setDescription(description)
    }
}

data class CreateAdvertisementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasActiveAdvertisement: Boolean = false,
    val navigateToPayment: Boolean = false,
    val physiotherapistId: String = ""
)