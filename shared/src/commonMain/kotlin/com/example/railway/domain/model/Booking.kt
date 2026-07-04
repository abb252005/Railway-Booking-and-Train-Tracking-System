package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String, // Public Ticket ID
    val reservationId: String,
    val userId: String,
    val trainId: String,
    val publicTrainNumber: String = "",
    val serviceName: String = "",
    val passengerName: String,
    val passengerType: String = "Adult",
    val carriageId: String,
    val seatNumber: String,
    val fareClass: FareClass = FareClass.COACH,
    val fareProductName: String = "Standard",
    val startStationId: String,
    val endStationId: String,
    val startStationCode: String = "",
    val endStationCode: String = "",
    val departureDate: Long, // Epoch millis
    val serviceDate: String = "", // YYYY-MM-DD
    val departureTimeMillis: Long = 0L,
    val arrivalTimeMillis: Long = 0L,
    val timezone: String = "America/New_York",
    val paymentMethod: PaymentMethod,
    val paymentStatus: String = "Paid",
    val price: String, // Keep for backward compatibility
    val totalPrice: Money = Money("USD", 0),
    val timestamp: Long, // Issue timestamp
    val validityStartsAt: Long = 0L,
    val validityEndsAt: Long = 0L,
    val barcodePayload: String = "",
    val status: TicketStatus = TicketStatus.ISSUED
)

@Serializable
enum class PaymentMethod {
    CARD,
    CASH
}
