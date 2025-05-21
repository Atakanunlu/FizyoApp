package com.example.fizyoapp.presentation.user.usermainscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetLatestPainRecordUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    init {
        fetchUserData()
    }

    fun refreshAllData(userId: String) {
        viewModelScope.launch {
            fetchLatestPainRecord(userId)
            loadUserEmail(userId)
        }
    }

    fun onEvent(event: UserEvent) {
        when (event) {
            is UserEvent.SignOut -> {
                logout()
            }
            is UserEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
            is UserEvent.LoadUserProfile -> {
                val userId = state.value.userProfile?.userId
                if (userId != null) {
                    fetchUserProfile(userId)
                } else {
                    fetchUserData()
                }
            }
        }
    }


    fun emitProfileUpdated() {
        viewModelScope.launch {
            _profileUpdatedEvent.emit(Unit)
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
                            setupProfileListener(userId)
                            fetchUserProfile(userId)
                            fetchLatestPainRecord(userId)
                            loadUserEmail(userId)
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

    private fun setupProfileListener(userId: String) {
        FirebaseFirestore.getInstance().collection("user_profiles").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserViewModel", "Error listening to profile changes: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val profilePhotoUrl = snapshot.getString("profilePhotoUrl")
                        if (!profilePhotoUrl.isNullOrEmpty()) {
                            _state.update { currentState ->
                                currentState.copy(
                                    userProfile = currentState.userProfile?.copy(
                                        profilePhotoUrl = profilePhotoUrl
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error getting profile photo: ${e.message}")
                    }
                }
            }
    }

    private fun loadUserEmail(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance().collection("user").document(userId).get().await()
                if (userDoc.exists()) {
                    val email = userDoc.getString("email") ?: ""
                    _state.update { it.copy(email = email) }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading user email: ${e.message}")
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

                    }
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