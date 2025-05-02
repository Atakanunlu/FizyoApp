package com.example.fizyoapp.presentation.user.usermainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.local.entity.auth.AuthResult
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.model.usermainscreen.StepCount
import com.example.fizyoapp.domain.model.usermainscreen.WaterIntake
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetActiveRemindersUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetLatestPainRecordUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetStepCountForTodayUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetWaterIntakeForTodayUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.UpdateStepCountUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.UpdateWaterIntakeUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getLatestPainRecordUseCase: GetLatestPainRecordUseCase,
    private val getWaterIntakeForTodayUseCase: GetWaterIntakeForTodayUseCase,
    private val updateWaterIntakeUseCase: UpdateWaterIntakeUseCase,
    private val getStepCountForTodayUseCase: GetStepCountForTodayUseCase,
    private val updateStepCountUseCase: UpdateStepCountUseCase,
    private val getActiveRemindersUseCase: GetActiveRemindersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        fetchUserData()
    }

    fun onEvent(event: UserEvent) {
        when (event) {
            is UserEvent.UpdateWaterIntake -> {
                updateWaterIntake(event.glasses)
            }
            is UserEvent.UpdateStepCount -> {
                updateStepCount(event.steps)
            }
            is UserEvent.Logout -> {
                logout()
            }
            is UserEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val userId = result.data?.id
                        if (userId != null) {
                            // Kullanıcı profil bilgilerini al
                            fetchUserProfile(userId)
                            // Ağrı kaydını al
                            fetchLatestPainRecord(userId)
                            // Su tüketimini al
                            fetchWaterIntakeForToday(userId)
                            // Adım sayısını al
                            fetchStepCountForToday(userId)
                            // Hatırlatmaları al
                            fetchActiveReminders(userId)
                        } else {
                            _state.update {
                                it.copy(
                                    error = "Kullanıcı bilgisi bulunamadı",
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Kullanıcı bilgisi alınamadı",
                                isLoading = false
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

    private fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            getUserProfileUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val profile = result.data ?: UserProfile()
                        _state.update {
                            it.copy(
                                userProfile = profile,
                                userName = "${profile.firstName} ${profile.lastName}".trim().ifEmpty { "Hasta" },
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Kullanıcı profili alınamadı",
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Yükleme durumu zaten ana fonksiyonda ayarlandı
                    }
                }
            }
        }
    }

    private fun fetchLatestPainRecord(userId: String) {
        viewModelScope.launch {
            getLatestPainRecordUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                latestPainRecord = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Ağrı kaydı alınamadı",
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Yükleme durumu zaten ana fonksiyonda ayarlandı
                    }
                }
            }
        }
    }

    private fun fetchWaterIntakeForToday(userId: String) {
        viewModelScope.launch {
            getWaterIntakeForTodayUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                waterIntake = result.data ?: WaterIntake(userId = userId),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Su tüketim bilgisi alınamadı",
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Yükleme durumu zaten ana fonksiyonda ayarlandı
                    }
                }
            }
        }
    }

    private fun fetchStepCountForToday(userId: String) {
        viewModelScope.launch {
            getStepCountForTodayUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                stepCount = result.data ?: StepCount(userId = userId),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Adım sayısı bilgisi alınamadı",
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Yükleme durumu zaten ana fonksiyonda ayarlandı
                    }
                }
            }
        }
    }

    private fun fetchActiveReminders(userId: String) {
        viewModelScope.launch {
            getActiveRemindersUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                reminders = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Hatırlatmalar alınamadı",
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Yükleme durumu zaten ana fonksiyonda ayarlandı
                    }
                }
            }
        }
    }

    private fun updateWaterIntake(glasses: Int) {
        viewModelScope.launch {
            val userId = state.value.userProfile?.userId ?: return@launch
            val waterIntake = WaterIntake(
                userId = userId,
                date = System.currentTimeMillis(),
                glasses = glasses,
                milliliters = glasses * 200 // Her bardak 200ml
            )

            when (val result = updateWaterIntakeUseCase(waterIntake)) {
                is Resource.Success -> {
                    // Başarılı güncelleme sonrası en son verileri al
                    fetchWaterIntakeForToday(userId)
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            error = result.message ?: "Su tüketimi güncellenemedi"
                        )
                    }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun updateStepCount(steps: Int) {
        viewModelScope.launch {
            val userId = state.value.userProfile?.userId ?: return@launch
            val stepCount = StepCount(
                userId = userId,
                date = System.currentTimeMillis(),
                steps = steps
            )

            when (val result = updateStepCountUseCase(stepCount)) {
                is Resource.Success -> {
                    // Başarılı güncelleme sonrası en son verileri al
                    fetchStepCountForToday(userId)
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            error = result.message ?: "Adım sayısı güncellenemedi"
                        )
                    }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            signOutUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiEvent.send(UiEvent.NavigateToLogin)
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                error = result.message ?: "Çıkış yapılamadı"
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

    sealed class UiEvent {
        object NavigateToLogin : UiEvent()
    }
}


