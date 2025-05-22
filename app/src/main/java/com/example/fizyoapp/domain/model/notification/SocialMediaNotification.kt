package com.example.fizyoapp.domain.model.notification

import java.util.Date

data class SocialMediaNotification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val contentId: String = "",
    val contentText: String = "",
    val isRead: Boolean = false,
    val timestamp: Date = Date()
)

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW
}