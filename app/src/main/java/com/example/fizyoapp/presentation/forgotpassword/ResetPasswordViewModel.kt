package com.example.fizyoapp.presentation.forgotpassword
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        savedStateHandle.get<String>("email")?.let { email ->
            _state.value = _state.value.copy(email = email)
        }
        savedStateHandle.get<String>("code")?.let { code ->
            _state.value = _state.value.copy(code = code)
        }
    }

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.PasswordChanged -> {
                _state.value = _state.value.copy(
                    password = event.password,
                    passwordError = event.password != _state.value.confirmPassword && _state.value.confirmPassword.isNotEmpty()
                )
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(
                    confirmPassword = event.confirmPassword,
                    passwordError = _state.value.password != event.confirmPassword && event.confirmPassword.isNotEmpty()
                )
            }
            is ResetPasswordEvent.ResetPassword -> {
                resetPassword()
            }
            is ResetPasswordEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateBack)
                }
            }
        }
    }

    private fun resetPassword() {
        viewModelScope.launch {
            if (!validateInput()) return@launch
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )
            resetPasswordUseCase(_state.value.code, _state.value.password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isPasswordReset = true
                        )
                        _uiEvent.send(UiEvent.NavigateToLogin)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Şifre sıfırlanamadı"
                        )
                    }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        if (_state.value.password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Şifre boş olamaz")
            return false
        }
        if (_state.value.password.length < 6) {
            _state.value = _state.value.copy(errorMessage = "Şifre en az 6 karakter olmalıdır")
            return false
        }
        if (_state.value.password != _state.value.confirmPassword) {
            _state.value = _state.value.copy(
                errorMessage = "Şifreler eşleşmiyor",
                passwordError = true
            )
            return false
        }
        return true
    }

    sealed class UiEvent {
        data object NavigateBack : UiEvent()
        data object NavigateToLogin : UiEvent()
    }
}