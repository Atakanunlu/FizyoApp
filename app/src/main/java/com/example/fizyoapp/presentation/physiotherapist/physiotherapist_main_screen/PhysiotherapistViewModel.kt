package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.auth.GetCurrentPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhysiotherapistViewModel @Inject constructor(
    private val getCurrentPhysiotherapistUseCase: GetCurrentPhysiotherapistUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getPhysiotherapistProfileUseCase: GetPhysiotherapistProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PhysiotherapistState())
    val state: StateFlow<PhysiotherapistState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        fetchPhysiotherapistData()
    }

    private fun fetchPhysiotherapistData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getCurrentPhysiotherapistUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val physiotherapist = result.data
                        if (physiotherapist != null) {
                            _state.value = _state.value.copy(
                                physiotherapistId = physiotherapist.id,
                                physiotherapistName = physiotherapist.email
                            )

                            getPhysiotherapistProfileUseCase(physiotherapist.id).collect { profileResult ->
                                when (profileResult) {
                                    is Resource.Success -> {
                                        val profile = profileResult.data
                                        _state.value = _state.value.copy(
                                            isLoading = false,
                                            physiotherapistProfile = profile
                                        )
                                    }
                                    is Resource.Error -> {
                                        _state.value = _state.value.copy(
                                            isLoading = false,
                                            errorMessage = profileResult.message
                                        )
                                    }
                                    is Resource.Loading -> {
                                    }
                                }
                            }
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = "Fizyoterapist bilgisi bulunamadı"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    fun onEvent(event: PhysiotherapistEvent) {
        when (event) {
            is PhysiotherapistEvent.SignOut -> {
                signOut()
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            signOutUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiEvent.send(UiEvent.NavigateToLogin)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        object NavigateToLogin : UiEvent()
    }
}