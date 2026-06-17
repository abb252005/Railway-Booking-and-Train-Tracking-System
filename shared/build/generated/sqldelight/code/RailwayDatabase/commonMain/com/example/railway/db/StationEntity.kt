package com.example.railway.db

import kotlin.Double
import kotlin.String

public data class StationEntity(
  public val id: String,
  public val name: String,
  public val latitude: Double,
  public val longitude: Double,
  public val designatedTrainId: String?,
  public val info: String,
  public val terminal: String,
)
