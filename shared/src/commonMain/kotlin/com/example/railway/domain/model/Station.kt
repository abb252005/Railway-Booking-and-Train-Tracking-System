package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: String,
    val name: String,
    val code: String = "",
    val city: String = "",
    val state: String = "",
    val timezone: String = "America/New_York",
    val latitude: Double,
    val longitude: Double,
    val designatedTrainId: String? = null,
    val info: String = "",
    val terminal: String = "Main Terminal",
    val hasLounge: Boolean = false,
    val hasCheckedBaggage: Boolean = false,
    val hasKiosks: Boolean = true,
    val isHub: Boolean = false
)
