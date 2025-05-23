package com.example.fizyoapp.presentation.forgotpassword
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.auth.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email)
            }
            is ForgotPasswordEvent.SendResetEmail -> {
                sendResetEmail()
            }
            is ForgotPasswordEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateBack)
                }
            }
        }
    }

    private fun sendResetEmail() {
        viewModelScope.launch {
            if (!validateEmail()) return@launch
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )
            sendPasswordResetEmailUseCase(_state.value.email).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isEmailSent = true
                        )
                        _uiEvent.send(UiEvent.ShowSuccessDialog)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Şifre sıfırlama e-postası gönderilemedi"
                        )
                    }
                }
            }
        }
    }

    private fun validateEmail(): Boolean {
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "E-posta adresi boş olamaz")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.value = _state.value.copy(errorMessage = "Geçerli bir e-posta adresi girin")
            return false
        }
        return true
    }

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data object ShowSuccessDialog : UiEvent()
    }
}