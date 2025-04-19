package com.example.fizyoapp.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.CheckPhysiotherapistProfileCompletedUseCase
import com.example.fizyoapp.domain.usecase.user_profile.CheckProfileCompletedUseCase
import kotlinx.coroutines.delay

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val checkProfileCompletedUseCase: CheckProfileCompletedUseCase,
    private val checkPhysiotherapistProfileCompletedUseCase: CheckPhysiotherapistProfileCompletedUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user
                            )
                            onLoginSuccess(user)
                        } else {
                            _state.value = _state.value.copy(isLoading = false)
                        }
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

    private fun onLoginSuccess(user: User) {
        viewModelScope.launch {
            try {
                when (user.role) {
                    UserRole.USER -> {
                        Log.d("LoginViewModel", "User is a regular user, checking profile")

                        checkProfileCompletedUseCase(user.id).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    val isProfileCompleted = result.data
                                    Log.d("LoginViewModel", "User profile completed: $isProfileCompleted")
                                    if (isProfileCompleted) {
                                        _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                    } else {
                                        _uiEvent.send(UiEvent.NavigateToProfileSetup)
                                    }
                                }
                                is Resource.Error -> {
                                    Log.e("LoginViewModel", "Error checking profile: ${result.message}")

                                    _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                }
                                is Resource.Loading -> {
                                }
                            }
                        }
                    }
                    UserRole.PHYSIOTHERAPIST -> {
                        Log.d("LoginViewModel", "User is a physiotherapist, checking profile")

                        checkPhysiotherapistProfileCompletedUseCase(user.id).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    val isProfileCompleted = result.data
                                    Log.d("LoginViewModel", "Physiotherapist profile completed: $isProfileCompleted")
                                    if (isProfileCompleted) {
                                        _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                    } else {
                                        _uiEvent.send(UiEvent.NavigateToPhysiotherapistProfileSetup)
                                    }
                                }
                                is Resource.Error -> {
                                    Log.e("LoginViewModel", "Error checking physiotherapist profile: ${result.message}")
                                    _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                }
                                is Resource.Loading -> {
                                }
                            }
                        }
                    }
                    else -> {
                        Log.d("LoginViewModel", "Unknown role, navigating to default screen")
                        _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception during login navigation: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Giriş yapılırken bir hata oluştu: ${e.message}"
                )
                _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
            }
        }
    }


    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email)
            }
            is LoginEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password)
            }
            is LoginEvent.RoleChanged -> {
                _state.value = _state.value.copy(selectedRole = event.role)
            }
            is LoginEvent.SignIn -> {
                signIn()
            }
            is LoginEvent.NavigateToRegister -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToRegister)
                }
            }
            is LoginEvent.ResetState -> {
                _state.value = LoginState()
            }
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            if (!validateInput()) return@launch

            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )


            val timeoutJob = launch {
                delay(10000)
                if (_state.value.isLoading) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Giriş zaman aşımına uğradı. Lütfen tekrar deneyin."
                    )
                }
            }

            try {
                signInUseCase(
                    _state.value.email,
                    _state.value.password,
                    _state.value.selectedRole
                ).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                        is Resource.Success -> {
                            timeoutJob.cancel()
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = result.data
                            )
                            onLoginSuccess(result.data)
                        }
                        is Resource.Error -> {
                            timeoutJob.cancel()
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                timeoutJob.cancel()
                Log.e("LoginViewModel", "Sign in exception", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Giriş sırasında bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun validateInput(): Boolean {
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Email boş olamaz")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.value = _state.value.copy(errorMessage = "Geçerli bir email adresi girin")
            return false
        }
        if (_state.value.password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Şifre boş olamaz")
            return false
        }
        return true
    }

    sealed class UiEvent {
        data class NavigateBasedOnRole(val role: UserRole) : UiEvent()
        data object NavigateToRegister : UiEvent()
        data object NavigateToProfileSetup : UiEvent()
        data object NavigateToPhysiotherapistProfileSetup : UiEvent()


    }
}