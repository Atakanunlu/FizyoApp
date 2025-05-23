package com.example.fizyoapp.presentation.advertisement.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.advertisement.Advertisement
import com.example.fizyoapp.domain.usecase.advertisement.GetAdvertisementByIdUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvertisementDetailViewModel @Inject constructor(
    private val getAdvertisementByIdUseCase: GetAdvertisementByIdUseCase,
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AdvertisementDetailState())
    val state: StateFlow<AdvertisementDetailState> = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("advertisementId")?.let { advertisementId ->
            loadAdvertisement(advertisementId)
        } ?: run {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Reklam ID'si bulunamadı"
                )
            }
        }
    }

    private fun loadAdvertisement(advertisementId: String) {
        viewModelScope.launch {
            getAdvertisementByIdUseCase(advertisementId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                advertisement = result.data,
                                error = null
                            )
                        }

                        // Load physiotherapist details
                        loadPhysiotherapistDetails(result.data.physiotherapistId)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Reklam yüklenirken bir hata oluştu"
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

    private fun loadPhysiotherapistDetails(physiotherapistId: String) {
        viewModelScope.launch {
            getPhysiotherapistByIdUseCase(physiotherapistId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val physiotherapist = result.data
                        _state.update {
                            it.copy(
                                physiotherapistName = "${physiotherapist.firstName} ${physiotherapist.lastName}",
                                physiotherapistImageUrl = physiotherapist.profilePhotoUrl
                            )
                        }
                    }
                    is Resource.Error -> {
                        // Don't update error state, as the advertisement is still loaded
                    }
                    is Resource.Loading -> {
                        // Don't show loading for physiotherapist details
                    }
                }
            }
        }
    }
}

data class AdvertisementDetailState(
    val isLoading: Boolean = true,
    val advertisement: Advertisement? = null,
    val physiotherapistName: String? = null,
    val physiotherapistImageUrl: String? = null,
    val error: String? = null
)