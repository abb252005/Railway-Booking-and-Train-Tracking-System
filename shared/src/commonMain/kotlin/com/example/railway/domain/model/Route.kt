package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Waypoint(val latitude: Double, val longitude: Double)

@Serializable
data class Route(
    val id: String,
    val sourceStationId: String,
    val destinationStationId: String,
    val distance: Double,
    val estimatedTimeMinutes: Int,
    val waypoints: List<Waypoint> = emptyList()
)
