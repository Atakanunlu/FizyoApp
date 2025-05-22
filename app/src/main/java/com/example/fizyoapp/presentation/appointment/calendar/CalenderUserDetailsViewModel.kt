package com.example.fizyoapp.presentation.appointment.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalenderUserDetailsViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CalenderUserDetailsState())
    val state: StateFlow<CalenderUserDetailsState> = _state.asStateFlow()

    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getUserProfileUseCase(userId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                userProfile = result.data,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
        }
    }
}

data class CalenderUserDetailsState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)