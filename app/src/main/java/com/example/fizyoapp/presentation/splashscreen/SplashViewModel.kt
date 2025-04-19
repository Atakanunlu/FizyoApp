package com.example.fizyoapp.presentation.splashscreen

import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.user_profile.CheckProfileCompletedUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.CheckPhysiotherapistProfileCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val checkProfileCompletedUseCase: CheckProfileCompletedUseCase,
    private val checkPhysiotherapistProfileCompletedUseCase: CheckPhysiotherapistProfileCompletedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            _state.value = _state.value.copy(
                                isUserLoggedIn = true,
                                userRole = user.role
                            )


                            when (user.role) {
                                UserRole.USER -> {
                                    checkProfileCompletedUseCase(user.id).collect { profileResult ->
                                        when (profileResult) {
                                            is Resource.Success -> {
                                                _state.value = _state.value.copy(
                                                    isProfileCompleted = profileResult.data,
                                                    isLoading = false
                                                )
                                            }
                                            is Resource.Error -> {
                                                _state.value = _state.value.copy(
                                                    isLoading = false
                                                )
                                            }
                                            is Resource.Loading -> {

                                            }
                                        }
                                    }
                                }
                                UserRole.PHYSIOTHERAPIST -> {

                                    checkPhysiotherapistProfileCompletedUseCase(user.id).collect { profileResult ->
                                        when (profileResult) {
                                            is Resource.Success -> {
                                                _state.value = _state.value.copy(
                                                    isProfileCompleted = profileResult.data,
                                                    isLoading = false
                                                )
                                            }
                                            is Resource.Error -> {
                                                _state.value = _state.value.copy(
                                                    isLoading = false
                                                )
                                            }
                                            is Resource.Loading -> {

                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            _state.value = _state.value.copy(
                                isUserLoggedIn = false,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isUserLoggedIn = false,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }
}