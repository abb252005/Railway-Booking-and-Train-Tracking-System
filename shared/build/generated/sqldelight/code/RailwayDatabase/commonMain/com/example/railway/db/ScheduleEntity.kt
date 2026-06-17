package com.example.railway.db

import kotlin.Long
import kotlin.String

public data class ScheduleEntity(
  public val id: String,
  public val trainId: String,
  public val sourceStationId: String,
  public val destinationStationId: String,
  public val departureTimeMillis: Long,
  public val arrivalTimeMillis: Long,
  public val routeId: String,
  public val isActive: Long,
)
