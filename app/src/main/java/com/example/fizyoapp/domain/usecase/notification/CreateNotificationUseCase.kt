package com.example.fizyoapp.domain.usecase.notification

import com.example.fizyoapp.data.repository.notification.NotificationRepository
import com.example.fizyoapp.domain.model.notification.SocialMediaNotification
import javax.inject.Inject

class CreateNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(notification: SocialMediaNotification) = repository.createNotification(notification)
}