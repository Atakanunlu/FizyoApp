package com.example.fizyoapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.UserRole
import com.example.fizyoapp.domain.usecase.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {

    // State, UI'ın görüntüleyeceği tüm verileri içeren immutable bir nesnedir
    // MutableStateFlow, değeri güncellenebilen reaktif bir akış oluşturur
    private val _state = MutableStateFlow(LoginState())

    // UI katmanına readonly erişim sağlamak için asStateFlow() kullanılır
    // Bu, encapsulation prensibini uygular - UI state'i okuyabilir ama direkt değiştiremez
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // UI'a tek seferlik eventler (navigasyon, toast mesajları vb.) göndermek için Channel kullanılır
    // Channel, akış durdurulduktan sonra yeni değerlerin kaybolmasını sağlar (tek seferlik event'ler için idealdir)
    private val _uiEvent = Channel<UiEvent>()

    // Channel'ı observable bir flow olarak dışarı açar
    // Bu flow, LaunchedEffect içinde collect edilebilir
    val uiEvent = _uiEvent.receiveAsFlow()

    // ViewModel oluşturulduğunda mevcut kullanıcıyı kontrol eder
    init {
        checkCurrentUser()
    }

    // Mevcut kullanıcı kontrol edilir, oturum açılmışsa uygun ekrana yönlendirme yapılır
    private fun checkCurrentUser() {
        viewModelScope.launch {
            // Kullanıcı bilgisini almak için GetCurrentUserUseCase kullanılır
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    // Loading durumunda UI'daki yükleme göstergesini aktif eder
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    // Kullanıcı bilgisi başarıyla alındığında
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            // Kullanıcı oturum açmışsa, state'i günceller
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user
                            )
                            // Kullanıcı rolüne göre yönlendirme yapar
                            _uiEvent.send(UiEvent.NavigateBasedOnRole(user.role))
                        } else {
                            // Kullanıcı oturum açmamışsa, sadece yükleme göstergesini kapatır
                            _state.value = _state.value.copy(isLoading = false)
                        }
                    }
                    // Hata durumunda hata mesajını state'e ekler
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

    // UI'dan gelen eventleri işlemek için kullanılır
    // UI, bu metodu çağırarak kullanıcı etkileşimlerini ViewModel'e iletir
    fun onEvent(event: LoginEvent) {
        when (event) {
            // Email değiştiğinde state'deki email alanını günceller
            is LoginEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email)
            }
            // Şifre değiştiğinde state'deki password alanını günceller
            is LoginEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password)
            }
            // Rol değiştiğinde state'deki selectedRole alanını günceller
            is LoginEvent.RoleChanged -> {
                _state.value = _state.value.copy(selectedRole = event.role)
            }
            // Giriş butonu tıklandığında signIn metodunu çağırır
            is LoginEvent.SignIn -> {
                signIn()
            }
            // Kayıt ol butonu tıklandığında kayıt ekranına yönlendirme eventi gönderir
            is LoginEvent.NavigateToRegister -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateToRegister)
                }
            }
            // State'i resetleme eventi
            is LoginEvent.ResetState -> {
                _state.value = LoginState()
            }
        }
    }

    // Kullanıcı girişi işlemini gerçekleştirir
    private fun signIn() {
        viewModelScope.launch {
            // Girdi validasyonunu kontrol eder, geçersizse işlemi durdurur
            if (!validateInput()) return@launch

            // State'de loading durumunu aktifleştirir ve hata mesajını temizler
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // SignInUseCase'i çağırarak giriş işlemini başlatır
            signInUseCase(
                _state.value.email,
                _state.value.password,
                _state.value.selectedRole
            ).collect { result ->
                when (result) {
                    // Yükleme durumunda
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    // Başarılı giriş durumunda
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data
                        )
                        // Kullanıcı rolüne göre ilgili ekrana yönlendirir
                        _uiEvent.send(UiEvent.NavigateBasedOnRole(result.data.role))
                    }
                    // Hata durumunda
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

    // Giriş bilgilerinin doğruluğunu kontrol eder
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

        // Tüm kontroller geçildi, true döndürür
        return true
    }

    // UI'a gönderilecek event'lerin tanımlandığı sealed class
    sealed class UiEvent {
        // Kullanıcı rolüne göre yönlendirme yapmak için kullanılır
        data class NavigateBasedOnRole(val role: UserRole) : UiEvent()
        // Kayıt ekranına yönlendirme yapmak için kullanılır
        data object NavigateToRegister : UiEvent()
    }
}