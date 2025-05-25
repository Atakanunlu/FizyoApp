package com.example.fizyoapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.auth.CheckEmailVerifiedUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.SignInUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import android.util.Log

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val checkProfileCompletedUseCase: CheckProfileCompletedUseCase,
    private val checkPhysiotherapistProfileCompletedUseCase: CheckPhysiotherapistProfileCompletedUseCase,
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase
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
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

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
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    isLoggedIn = false,
                                    user = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Error checking current user: ${result.message}", result.exception)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = result.message,
                                isLoggedIn = false,
                                user = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("LoginViewModel", "Exception in checkCurrentUser: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Kullanıcı kontrolü yapılırken hata oluştu: ${e.message}",
                    isLoggedIn = false,
                    user = null
                )
            }
        }
    }

    private fun onLoginSuccess(user: User) {
        viewModelScope.launch {
            try {
                checkEmailVerifiedUseCase().collect { verifiedResult ->
                    when (verifiedResult) {
                        is Resource.Success -> {
                            if (verifiedResult.data) {
                                when (user.role) {
                                    UserRole.USER -> {
                                        checkProfileCompletedUseCase(user.id).collect { result ->
                                            when (result) {
                                                is Resource.Success -> {
                                                    val isProfileCompleted = result.data
                                                    if (isProfileCompleted) {
                                                        _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                                    } else {
                                                        _uiEvent.send(UiEvent.NavigateToProfileSetup)
                                                    }
                                                }
                                                is Resource.Error -> {
                                                    Log.e("LoginViewModel", "Error checking user profile: ${result.message}", result.exception)
                                                    // Profil kontrol edilemese bile ana ekrana yönlendir
                                                    _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                                }
                                                is Resource.Loading -> {
                                                    // Loading state'i işleme
                                                }
                                            }
                                        }
                                    }
                                    UserRole.PHYSIOTHERAPIST -> {
                                        checkPhysiotherapistProfileCompletedUseCase(user.id).collect { result ->
                                            when (result) {
                                                is Resource.Success -> {
                                                    val isProfileCompleted = result.data
                                                    if (isProfileCompleted) {
                                                        _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                                    } else {
                                                        _uiEvent.send(UiEvent.NavigateToPhysiotherapistProfileSetup)
                                                    }
                                                }
                                                is Resource.Error -> {
                                                    Log.e("LoginViewModel", "Error checking physiotherapist profile: ${result.message}", result.exception)
                                                    // Profil kontrol edilemese bile ana ekrana yönlendir
                                                    _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                                }
                                                is Resource.Loading -> {
                                                    // Loading state'i işleme
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                                    }
                                }
                            } else {
                                _state.value = _state.value.copy(
                                    errorMessage = "Hesabınızı kullanmadan önce e-posta adresinizi doğrulamanız gerekiyor. Lütfen e-postanızı kontrol edin."
                                )
                                try {
                                    signOutUseCase().collect {}
                                } catch (e: Exception) {
                                    Log.e("LoginViewModel", "Error signing out unverified user: ${e.message}", e)
                                }
                            }
                        }
                        is Resource.Error -> {
                            Log.e("LoginViewModel", "Error checking email verification: ${verifiedResult.message}", verifiedResult.exception)
                            _state.value = _state.value.copy(
                                errorMessage = verifiedResult.message ?: "E-posta doğrulama durumu kontrol edilemedi"
                            )
                            // Hata olsa bile yönlendirmeyi yap
                            _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                        }
                        is Resource.Loading -> {
                            // Loading state'i işleme
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("LoginViewModel", "Exception in onLoginSuccess: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Giriş yapılırken bir hata oluştu: ${e.message}"
                )
                // Hata durumunda bile yönlendirmeyi dene
                try {
                    _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                } catch (navError: Exception) {
                    Log.e("LoginViewModel", "Error navigating after exception: ${navError.message}", navError)
                }
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
            is LoginEvent.NavigateToForgotPassword -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToForgotPassword)
                }
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
                delay(15000) // 15 saniye zaman aşımı
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

                            // Email doğrulamasını kontrol et
                            checkEmailVerifiedUseCase().collect { verifiedResult ->
                                when (verifiedResult) {
                                    is Resource.Success -> {
                                        if (verifiedResult.data) {
                                            _state.value = _state.value.copy(
                                                isLoading = false,
                                                isLoggedIn = true,
                                                user = result.data
                                            )
                                            onLoginSuccess(result.data)
                                        } else {
                                            _state.value = _state.value.copy(
                                                isLoading = false,
                                                errorMessage = "Hesabınız doğrulanmamış. Lütfen e-posta adresinize gönderilen doğrulama bağlantısını tıklayın."
                                            )
                                            try {
                                                signOutUseCase().collect {}
                                            } catch (e: Exception) {
                                                Log.e("LoginViewModel", "Error signing out unverified user: ${e.message}", e)
                                            }
                                        }
                                    }
                                    is Resource.Error -> {
                                        Log.e("LoginViewModel", "Error checking email verification: ${verifiedResult.message}", verifiedResult.exception)
                                        _state.value = _state.value.copy(
                                            isLoading = false,
                                            errorMessage = verifiedResult.message
                                        )
                                    }
                                    is Resource.Loading -> {
                                        // Loading state zaten set edildi
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            timeoutJob.cancel()
                            Log.e("LoginViewModel", "Error signing in: ${result.message}", result.exception)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                timeoutJob.cancel()
                Log.e("LoginViewModel", "Exception in signIn: ${e.message}", e)
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
        if (_state.value.password.length < 6) {
            _state.value = _state.value.copy(errorMessage = "Şifre en az 6 karakter olmalıdır")
            return false
        }
        return true
    }

    sealed class UiEvent {
        data class NavigateBasedOnRole(val role: UserRole) : UiEvent()
        data object NavigateToRegister : UiEvent()
        data object NavigateToProfileSetup : UiEvent()
        data object NavigateToPhysiotherapistProfileSetup : UiEvent()
        data object NavigateToForgotPassword : UiEvent()
    }
}