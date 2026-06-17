package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val designatedTrainId: String? = null,
    val info: String = "",
    val terminal: String = "Main Terminal"
)
