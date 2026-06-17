package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Carriage(
    val id: String,
    val number: Int,
    val capacity: Int = 50
)
