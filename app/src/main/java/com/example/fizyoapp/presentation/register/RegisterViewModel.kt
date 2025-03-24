package com.example.fizyoapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
): ViewModel(){

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: RegisterEvent){
        when(event){

            is RegisterEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email)
            }

            is RegisterEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password,

                    //Şifre ile onay şifresi eşleşmeli
                    passwordError = event.password != _state.value.confirmPassword && _state.value.confirmPassword.isNotEmpty()
                    )
            }

            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(confirmPassword = event.confirmPassword,
                    passwordError =_state.value.password != event.confirmPassword && event.confirmPassword.isNotEmpty()
                    )
            }

            is RegisterEvent.RoleChanged -> {
                _state.value = _state.value.copy(selectedRole = event.role)
            }

            is RegisterEvent.SignUp -> {
                signUp()
            }

            is RegisterEvent.NavigateToLogin -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToLogin)
                }
            }

            is RegisterEvent.ResetState -> {
                _state.value = RegisterState()
            }

        }
    }

    private fun signUp(){

        viewModelScope.launch {
            if (!validateInput()) return@launch

            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            signUpUseCase(
                _state.value.email,
                _state.value.password,
                _state.value.selectedRole
            ).collect{result ->
                when(result){

                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true
                        )
                        _uiEvent.send(UiEvent.ShowSuccessDialog)
                    }

                    is Resource.Error ->{
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }

                }
            }

        }

    }

    // Kayıt bilgilerinin doğruluğunu kontrol eder
    private fun validateInput(): Boolean {
        // Email boş mu kontrol eder
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Email boş olamaz")
            return false
        }

        // Email formatı geçerli mi kontrol eder
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.value = _state.value.copy(errorMessage = "Geçerli bir email adresi girin")
            return false
        }

        // Şifre boş mu kontrol eder
        if (_state.value.password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Şifre boş olamaz")
            return false
        }

        // Şifre uzunluğu yeterli mi kontrol eder
        if (_state.value.password.length < 6) {
            _state.value = _state.value.copy(errorMessage = "Şifre en az 6 karakter olmalıdır")
            return false
        }

        // Şifreler eşleşiyor mu kontrol eder
        if (_state.value.password != _state.value.confirmPassword) {
            _state.value = _state.value.copy(
                errorMessage = "Şifreler eşleşmiyor",
                passwordError = true
            )
            return false
        }

        // Tüm kontroller geçildi, true döndürür
        return true
    }


    sealed class UiEvent{
        data object NavigateToLogin: UiEvent()
        data object ShowSuccessDialog: UiEvent()
    }

}