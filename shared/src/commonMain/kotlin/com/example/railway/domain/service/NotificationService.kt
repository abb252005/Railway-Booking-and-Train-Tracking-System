package com.example.railway.domain.service

import com.example.railway.domain.model.*
import com.example.railway.util.currentTimeMillis

data class AdvancedNotification(
    val id: String,
    val type: NotificationType,
    val severity: NotificationSeverity,
    val title: String,
    val body: String,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

object NotificationService {
    private val _notifications = mutableListOf<AdvancedNotification>()
    val notifications: List<AdvancedNotification> get() = _notifications

    fun notify(
        type: NotificationType,
        severity: NotificationSeverity,
        title: String,
        body: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val notification = AdvancedNotification(
            id = "NOTIF-${currentTimeMillis()}",
            type = type,
            severity = severity,
            title = title,
            body = body,
            timestamp = currentTimeMillis(),
            metadata = metadata
        )
        _notifications.add(notification)
        
        // Console Log (Internal Terminal Requirement T-09)
        println("[${notification.timestamp}] ${severity.name} ${type.name}: ${notification.title} - ${notification.body}")
    }

    fun checkAndSendReminders(bookings: List<Booking>, trains: List<Train>) {
        val now = currentTimeMillis()
        bookings.filter { it.status == TicketStatus.ISSUED }.forEach { booking ->
            val timeToDeparture = booking.departureTimeMillis - now
            if (timeToDeparture in 0..1800000) { // 30 mins
                notify(
                    NotificationType.BOARDING_STARTED,
                    NotificationSeverity.INFO,
                    "Departure Reminder",
                    "Your train ${booking.publicTrainNumber} departs in ${timeToDeparture / 60000} minutes!"
                )
            }
        }
    }

    fun checkAndSendStatusAlerts(trains: List<Train>, positions: Map<String, TrainPosition>) {
        // Placeholder for complex status alert logic
    }
}
