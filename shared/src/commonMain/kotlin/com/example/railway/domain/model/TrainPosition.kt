package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrainPosition(
    val trainId: String,
    val latitude: Double,
    val longitude: Double,
    val currentRouteId: String?,
    val nextDestinationStationId: String?,
    val progress: Double, // 0.0 to 1.0
    val estimatedTimeRemainingMinutes: Double,
    val distanceRemainingKm: Double = 0.0,
    val lastUpdateTime: Long,
    val bearing: Double = 0.0,
    val speedKmH: Double = 0.0
)
