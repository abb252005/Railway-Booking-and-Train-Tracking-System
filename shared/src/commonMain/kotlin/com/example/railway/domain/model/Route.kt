package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Waypoint(val latitude: Double, val longitude: Double)

@Serializable
data class SlowOrder(
    val startProgress: Double, // 0.0 to 1.0 along the route
    val endProgress: Double,
    val speedLimitKmH: Double,
    val reason: String = "Maintenance"
)

@Serializable
data class ElevationPoint(
    val progress: Double,
    val elevationMeters: Double
)

@Serializable
enum class TrackClass {
    CLASS_1, // 15 mph
    CLASS_2, // 30 mph
    CLASS_3, // 60 mph
    CLASS_4, // 80 mph
    CLASS_5, // 90 mph
    CLASS_6, // 110 mph
    CLASS_7, // 125 mph
    CLASS_8, // 150 mph
    CLASS_9  // 200 mph
}

@Serializable
data class Route(
    val id: String,
    val sourceStationId: String,
    val destinationStationId: String,
    val distance: Double,
    val estimatedTimeMinutes: Int,
    val waypoints: List<Waypoint> = emptyList(),
    val slowOrders: List<SlowOrder> = emptyList(),
    val elevationProfile: List<ElevationPoint> = emptyList(),
    val trackClass: TrackClass = TrackClass.CLASS_4 // Default FRA standard
)
