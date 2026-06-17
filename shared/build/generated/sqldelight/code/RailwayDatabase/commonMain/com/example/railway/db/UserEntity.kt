package com.example.railway.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class UserEntity(
  public val id: String,
  public val username: String,
  public val password: String,
  public val isAdmin: Long,
  public val balance: Double,
)
