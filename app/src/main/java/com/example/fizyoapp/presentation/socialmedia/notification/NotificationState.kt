package com.example.fizyoapp.presentation.socialmedia.notification

import com.example.fizyoapp.domain.model.notification.SocialMediaNotification

data class NotificationState(
    val notifications: List<SocialMediaNotification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)