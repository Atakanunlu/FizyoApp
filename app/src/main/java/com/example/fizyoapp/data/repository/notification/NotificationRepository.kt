package com.example.fizyoapp.data.repository.notification

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.notification.SocialMediaNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<Resource<List<SocialMediaNotification>>>
    fun getUnreadNotificationsCount(userId: String): Flow<Resource<Int>>
    fun markNotificationAsRead(notificationId: String): Flow<Resource<Unit>>
    fun markAllNotificationsAsRead(userId: String): Flow<Resource<Unit>>
    fun createNotification(notification: SocialMediaNotification): Flow<Resource<Unit>>
    fun deleteNotification(notificationId: String): Flow<Resource<Unit>>
}