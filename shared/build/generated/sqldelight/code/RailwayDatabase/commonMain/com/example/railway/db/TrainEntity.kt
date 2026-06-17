package com.example.railway.db

import kotlin.Long
import kotlin.String

public data class TrainEntity(
  public val id: String,
  public val name: String,
  public val totalSeats: Long,
  public val status: String,
  public val carriageCount: Long,
)
