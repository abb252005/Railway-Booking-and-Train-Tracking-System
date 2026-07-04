package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TicketStatus {
    DRAFT, HELD, ISSUED, BOARDING, USED, CANCELLED, REFUNDED, EXPIRED
}

@Serializable
enum class FareClass {
    COACH, BUSINESS, ACELA_FIRST, ROOMETTE, BEDROOM, ACCESSIBLE_BEDROOM, FAMILY_BEDROOM
}

@Serializable
data class Money(
    val currency: String = "USD",
    val amountCents: Long,
    val baseAmountCents: Long = 0,
    val taxAmountCents: Long = 0
)

@Serializable
data class FareProduct(
    val id: String,
    val name: String,
    val refundRules: String,
    val changePolicy: String
)

@Serializable
enum class NotificationSeverity {
    INFO, WARN, ERROR
}

@Serializable
enum class NotificationType {
    BOOKING_CONFIRMED,
    PAYMENT_FAILED,
    QUOTE_EXPIRED,
    TRACK_POSTED,
    BOARDING_STARTED,
    DELAY_POSTED,
    TRACK_CHANGED,
    CANCELLED,
    SEAT_CHANGED,
    TICKET_SCANNED,
    TRIP_COMPLETED,
    REFUND_PROCESSED
}
