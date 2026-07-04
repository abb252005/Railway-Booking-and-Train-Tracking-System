package com.example.railway.domain.service

import com.example.railway.domain.model.*
import com.example.railway.util.currentTimeMillis
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DetailedNotificationService {
    
    fun formatTerminalLog(notification: RailNotification): String {
        // [YYYY-MM-DD HH:MM:SS+TZ] LEVEL Component key=value key=value message
        val timestamp = notification.createdAtUtc
        val level = notification.severity.name
        val component = notification.target.name
        val keys = mutableListOf<String>()
        notification.relatedTicketId?.let { keys.add("ticket=$it") }
        notification.relatedTrainNumber?.let { keys.add("train=$it") }
        notification.stationCode?.let { keys.add("station=$it") }
        
        return "[$timestamp] $level $component ${keys.joinToString(" ")} ${notification.title}: ${notification.body}"
    }

    fun sendPush(notification: RailNotification) {
        if (notification.target == NotificationTarget.PASSENGER_PUSH) {
            println("PUSH SENT: ${notification.title} - ${notification.body}")
        }
    }

    fun updateStationBoard(notification: RailNotification) {
        if (notification.target == NotificationTarget.STATION_TERMINAL) {
            println("STATION BOARD UPDATED: ${notification.stationCode} - ${notification.title}")
        }
    }
}
