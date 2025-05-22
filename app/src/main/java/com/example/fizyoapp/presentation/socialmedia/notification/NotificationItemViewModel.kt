package com.example.fizyoapp.presentation.socialmedia.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationItemViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase
) : ViewModel() {
    data class SenderProfileData(
        val name: String = "",
        val photoUrl: String = ""
    )

    private val _senderProfile = MutableStateFlow<SenderProfileData?>(null)
    val senderProfile: StateFlow<SenderProfileData?> = _senderProfile.asStateFlow()

    fun loadSenderProfile(senderId: String, senderRole: String) {
        viewModelScope.launch {
            try {
                when (senderRole) {
                    "USER" -> {
                        getUserProfileUseCase(senderId).collect { result ->
                            if (result is Resource.Success) {
                                val profile = result.data
                                _senderProfile.value = SenderProfileData(
                                    name = "${profile.firstName} ${profile.lastName}",
                                    photoUrl = profile.profilePhotoUrl
                                )
                            }
                        }
                    }
                    "PHYSIOTHERAPIST" -> {
                        getPhysiotherapistByIdUseCase(senderId).collect { result ->
                            if (result is Resource.Success) {
                                val profile = result.data
                                _senderProfile.value = SenderProfileData(
                                    name = "${profile.firstName} ${profile.lastName}",
                                    photoUrl = profile.profilePhotoUrl
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
}