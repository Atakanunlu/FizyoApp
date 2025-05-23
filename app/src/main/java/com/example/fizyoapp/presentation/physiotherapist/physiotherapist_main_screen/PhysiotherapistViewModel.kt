package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
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
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PhysiotherapistState())
    val state: StateFlow<PhysiotherapistState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null && user.role == UserRole.PHYSIOTHERAPIST) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                user = user,
                                errorMessage = null
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = "Yetkiniz yok veya oturum açılmamış"
                            )
                            _uiEvent.send(UiEvent.NavigateToLogin)
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        _uiEvent.send(UiEvent.NavigateToLogin)
                    }
                }
            }
        }
    }

    fun onEvent(event: PhysiotherapistEvent) {
        when (event) {
            is PhysiotherapistEvent.SignOut -> {
                viewModelScope.launch {
                    signOutUseCase().collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                _state.value = _state.value.copy(isLoading = true)
                            }
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    user = null
                                )
                                _uiEvent.send(UiEvent.NavigateToLogin)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    errorMessage = result.message
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data object NavigateToLogin : UiEvent()
    }
}