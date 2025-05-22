package com.example.fizyoapp.domain.usecase.notification

import com.example.fizyoapp.data.repository.notification.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(userId: String) = repository.getNotifications(userId)
}