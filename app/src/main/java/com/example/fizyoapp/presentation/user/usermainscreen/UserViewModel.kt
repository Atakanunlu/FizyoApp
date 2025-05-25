package com.example.fizyoapp.presentation.user.usermainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetLatestPainRecordUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getLatestPainRecordUseCase: GetLatestPainRecordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _profileUpdatedEvent = MutableSharedFlow<Unit>()
    val profileUpdatedEvent = _profileUpdatedEvent.asSharedFlow()

    private var loadingTimeoutJob: kotlinx.coroutines.Job? = null

    init {
        fetchUserData()

        viewModelScope.launch {
            profileUpdatedEvent.collect {
                val userId = state.value.userProfile?.userId
                if (userId != null) {
                    fetchUserProfile(userId)
                }
            }
        }
    }

    fun emitProfileUpdated() {
        viewModelScope.launch {
            _profileUpdatedEvent.emit(Unit)
        }
    }

    fun refreshAllData(userId: String) {
        viewModelScope.launch {
            try {
                fetchLatestPainRecord(userId)
                loadUserEmail(userId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun onEvent(event: UserEvent) {
        when (event) {
            is UserEvent.SignOut -> {
                signOut()
            }
            is UserEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
            is UserEvent.LoadUserProfile -> {
                loadingTimeoutJob?.cancel()
                _state.value = _state.value.copy(isLoading = true, error = null)

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    fetchUserProfile(currentUser.uid)
                } else {
                    fetchUserData()
                }
            }
        }
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                startLoadingTimeout()

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    fetchUserProfile(userId)
                } else {
                    getCurrentUserUseCase().collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val user = result.data
                                if (user != null) {
                                    fetchUserProfile(user.id)
                                } else {
                                    stopLoadingTimeout()
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        error = "Kullanıcı bulunamadı. Lütfen tekrar giriş yapın."
                                    )
                                }
                            }
                            is Resource.Error -> {
                                stopLoadingTimeout()
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = result.message ?: "Kullanıcı bilgileri alınamadı"
                                )
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                stopLoadingTimeout()
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Veri yükleme hatası: ${e.message}"
                )
            }
        }
    }

    private fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                startLoadingTimeout()

                try {
                    val userProfileDoc = FirebaseFirestore.getInstance()
                        .collection("user_profiles")
                        .document(userId)
                        .get()
                        .await()

                    if (userProfileDoc.exists()) {
                        val profile = userProfileDoc.toObject(UserProfile::class.java)
                            ?: UserProfile(userId = userId)

                        stopLoadingTimeout()

                        _state.value = _state.value.copy(
                            userProfile = profile,
                            userName = "${profile.firstName} ${profile.lastName}".trim().ifEmpty { "Hasta" },
                            isLoading = false
                        )

                        fetchLatestPainRecord(userId)
                        loadUserEmail(userId)
                        return@launch
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }

                getUserProfileUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val profile = result.data ?: UserProfile(userId = userId)
                            stopLoadingTimeout()

                            _state.value = _state.value.copy(
                                userProfile = profile,
                                userName = "${profile.firstName} ${profile.lastName}".trim().ifEmpty { "Hasta" },
                                isLoading = false
                            )

                            fetchLatestPainRecord(userId)
                            loadUserEmail(userId)
                        }
                        is Resource.Error -> {
                            stopLoadingTimeout()
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message,
                                userProfile = UserProfile(userId = userId)
                            )
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                stopLoadingTimeout()
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Profil yükleme hatası: ${e.message}",
                    userProfile = UserProfile(userId = userId)
                )
            }
        }
    }

    private fun fetchLatestPainRecord(userId: String) {
        viewModelScope.launch {
            try {
                getLatestPainRecordUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                latestPainRecord = result.data
                            )
                        }
                        is Resource.Error -> {
                        }
                        is Resource.Loading -> {
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    private fun loadUserEmail(userId: String) {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val userDoc = firestore.collection("user").document(userId).get().await()
                if (userDoc.exists()) {
                    val email = userDoc.getString("email") ?: ""
                    _state.value = _state.value.copy(email = email)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val timeoutJob = launch {
                    delay(5000)
                    if (_state.value.isLoading) {
                        try {
                            FirebaseAuth.getInstance().signOut()
                            _state.value = _state.value.copy(isLoading = false)
                            _uiEvent.send(UiEvent.NavigateToLogin)
                        } catch (e: Exception) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Çıkış yapma zaman aşımına uğradı"
                            )
                        }
                    }
                }

                try {
                    FirebaseAuth.getInstance().signOut()
                    timeoutJob.cancel()
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.NavigateToLogin)
                    return@launch
                } catch (e: Exception) {
                }

                var useCase = false
                try {
                    signOutUseCase().collect { result ->
                        useCase = true
                        when (result) {
                            is Resource.Success -> {
                                timeoutJob.cancel()
                                _state.value = _state.value.copy(isLoading = false)
                                _uiEvent.send(UiEvent.NavigateToLogin)
                            }
                            is Resource.Error -> {
                                timeoutJob.cancel()
                                try {
                                    FirebaseAuth.getInstance().signOut()
                                    _state.value = _state.value.copy(isLoading = false)
                                    _uiEvent.send(UiEvent.NavigateToLogin)
                                } catch (ex: Exception) {
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        error = "Çıkış yapılamadı: ${result.message}"
                                    )
                                }
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
                } catch (e: Exception) {
                    timeoutJob.cancel()

                    if (!useCase) {
                        try {
                            FirebaseAuth.getInstance().signOut()
                            _state.value = _state.value.copy(isLoading = false)
                            _uiEvent.send(UiEvent.NavigateToLogin)
                        } catch (finalEx: Exception) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Çıkış yapılamadı: ${e.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Çıkış yapılamadı. Lütfen 'Çıkış Yap' düğmesini kullanın."
                )
            }
        }
    }

    private fun startLoadingTimeout() {
        loadingTimeoutJob?.cancel()
        loadingTimeoutJob = viewModelScope.launch {
            delay(15000)
            if (_state.value.isLoading) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Veri yükleme zaman aşımına uğradı. Lütfen tekrar deneyin."
                )
            }
        }
    }

    private fun stopLoadingTimeout() {
        loadingTimeoutJob?.cancel()
        loadingTimeoutJob = null
    }

    sealed class UiEvent {
        object NavigateToLogin : UiEvent()
    }

    override fun onCleared() {
        super.onCleared()
        loadingTimeoutJob?.cancel()
    }
}