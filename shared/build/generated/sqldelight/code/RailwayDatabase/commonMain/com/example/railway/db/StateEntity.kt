package com.example.railway.db

import kotlin.Double
import kotlin.String

public data class StateEntity(
  public val name: String,
  public val points: String,
  public val centroidLat: Double,
  public val centroidLng: Double,
)
