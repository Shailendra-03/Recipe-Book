package com.codingsp.recipebook.repositories

import com.codingsp.recipebook.daos.NotificationDao
import com.codingsp.recipebook.model.Notification

class NotificationRepository {
    private var notificationDao = NotificationDao()
    suspend fun addNotification(notification: Notification, notificationToID: String) {
        if (notification.notificationBy == notificationToID) return
        notificationDao.addNotification(notification, notificationToID)
    }
}