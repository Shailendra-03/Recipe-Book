package com.codingsp.recipebook.daos

import com.codingsp.recipebook.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationDao {
    private val collection = FirebaseFirestore.getInstance().collection("Notifications")
    suspend fun addNotification(notification: Notification, notificationToID: String) {
        try {
            collection.document(notificationToID).collection("Notifications").document()
                .set(notification).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}