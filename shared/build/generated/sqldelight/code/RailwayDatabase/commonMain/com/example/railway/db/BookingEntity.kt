package com.example.railway.db

import kotlin.Long
import kotlin.String

public data class BookingEntity(
  public val id: String,
  public val userId: String,
  public val trainId: String,
  public val passengerName: String,
  public val carriageId: String,
  public val seatNumber: String,
  public val startStationId: String,
  public val endStationId: String,
  public val departureDate: Long,
  public val departureTimeMillis: Long,
  public val arrivalTimeMillis: Long,
  public val paymentMethod: String,
  public val price: String,
  public val timestamp: Long,
)
