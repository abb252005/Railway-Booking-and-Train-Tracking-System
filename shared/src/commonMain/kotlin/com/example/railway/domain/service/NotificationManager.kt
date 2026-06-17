package com.example.railway.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.railway.util.currentTimeMillis

data class RailwayNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = currentTimeMillis(),
    val isRead: Boolean = false
)

class NotificationManager {
    private val _notifications = MutableStateFlow<List<RailwayNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun postNotification(title: String, message: String) {
        val newNotification = RailwayNotification(
            id = currentTimeMillis().toString(),
            title = title,
            message = message
        )
        _notifications.update { listOf(newNotification) + it }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
