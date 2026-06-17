package com.example.railway.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class RouteEntity(
  public val id: String,
  public val sourceStationId: String,
  public val destinationStationId: String,
  public val distance: Double,
  public val estimatedTime: Long,
  public val waypoints: String?,
)
