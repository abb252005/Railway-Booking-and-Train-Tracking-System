package com.example.railway.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long
)

class NotificationService {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val notifiedTripIds = mutableSetOf<String>()
    private val notifiedDelayIds = mutableSetOf<String>()

    fun sendNotification(title: String, message: String) {
        val notification = AppNotification(
            id = (1000..9999).random().toString(),
            title = title,
            message = message,
            timestamp = com.example.railway.util.currentTimeMillis()
        )
        _notifications.update { it + notification }
        println("NOTIFICATION: [$title] $message")
    }

    fun checkAndSendReminders(
        bookings: List<com.example.railway.domain.model.Booking>,
        trains: List<com.example.railway.domain.model.Train>
    ) {
        val now = com.example.railway.util.currentTimeMillis()
        val reminderWindow = 30 * 60 * 1000L // 30 minutes

        bookings.forEach { booking ->
            val timeToDeparture = booking.departureTimeMillis - now
            if (timeToDeparture in 0..reminderWindow && !notifiedTripIds.contains(booking.id)) {
                val train = trains.find { it.id == booking.trainId }
                sendNotification(
                    title = "Trip Reminder",
                    message = "Your train '${train?.name ?: "Express"}' departs in 30 minutes!"
                )
                notifiedTripIds.add(booking.id)
            }
        }
    }

    fun checkAndSendStatusAlerts(
        trains: List<com.example.railway.domain.model.Train>,
        positions: Map<String, com.example.railway.domain.model.TrainPosition>
    ) {
        // Logic for delay alerts (e.g., if ETA significantly exceeds schedule)
        // For simplicity, we'll mock a delay if speed is below a threshold
        positions.forEach { (trainId, pos) ->
            if (pos.speedKmH < 20.0 && !notifiedDelayIds.contains(trainId)) {
                val train = trains.find { it.id == trainId }
                sendNotification(
                    title = "Status Alert",
                    message = "Train '${train?.name ?: "Express"}' is experiencing delays."
                )
                notifiedDelayIds.add(trainId)
            }
        }
    }

    fun scheduleTripReminder(trainName: String, departureTimeMillis: Long) {
        // In a real app, this would use a work manager or alarms.
        // For this simulation, we'll just log it or send it immediately for demo.
        sendNotification(
            title = "Trip Reminder",
            message = "Your train '$trainName' departs in 30 minutes!"
        )
    }

    fun sendDelayAlert(trainName: String, delayMinutes: Int) {
        sendNotification(
            title = "Status Alert",
            message = "Train '$trainName' is delayed by $delayMinutes minutes."
        )
    }
}
