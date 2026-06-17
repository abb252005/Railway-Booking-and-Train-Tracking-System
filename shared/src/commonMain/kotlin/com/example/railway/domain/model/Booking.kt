package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String,
    val userId: String,
    val trainId: String,
    val passengerName: String,
    val carriageId: String,
    val seatNumber: String,
    val startStationId: String,
    val endStationId: String,
    val departureDate: Long,
    val departureTimeMillis: Long = 0,
    val arrivalTimeMillis: Long = 0,
    val paymentMethod: PaymentMethod,
    val price: String,
    val timestamp: Long
)
