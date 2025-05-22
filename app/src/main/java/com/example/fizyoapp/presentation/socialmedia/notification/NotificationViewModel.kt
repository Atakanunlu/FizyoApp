package com.example.fizyoapp.presentation.socialmedia.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.notification.GetNotificationsUseCase
import com.example.fizyoapp.domain.usecase.notification.MarkAllNotificationsAsReadUseCase
import com.example.fizyoapp.domain.usecase.notification.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _currentUser.value = result.data
                        loadNotifications()
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "Kullanıcı bilgisi alınamadı"
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun loadNotifications() {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getNotificationsUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            notifications = result.data,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(notificationId).collect { result ->
                if (result is Resource.Success) {
                    val updatedNotifications = _state.value.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _state.value = _state.value.copy(notifications = updatedNotifications)
                }
            }
        }
    }

    fun markAllAsRead() {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            markAllNotificationsAsReadUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val updatedNotifications = _state.value.notifications.map { it.copy(isRead = true) }
                        _state.value = _state.value.copy(notifications = updatedNotifications)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }
}