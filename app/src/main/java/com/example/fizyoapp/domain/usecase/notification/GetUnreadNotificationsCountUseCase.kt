package com.example.fizyoapp.domain.usecase.notification

import com.example.fizyoapp.data.repository.notification.NotificationRepository
import javax.inject.Inject

class GetUnreadNotificationsCountUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(userId: String) = repository.getUnreadNotificationsCount(userId)
}