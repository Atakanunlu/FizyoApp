package com.example.fizyoapp.presentation.socialmedia.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.notification.GetUnreadNotificationsCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationCountViewModel @Inject constructor(
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            loadUnreadNotificationsCount(user.id)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadUnreadNotificationsCount(userId: String) {
        viewModelScope.launch {
            getUnreadNotificationsCountUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _unreadNotificationsCount.value = result.data
                    }
                    else -> {}
                }
            }
        }
    }
}