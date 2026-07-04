package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ItinerarySegment(
    val routeId: String,
    val trainId: String,
    val sourceStationId: String,
    val destinationStationId: String,
    val departureTimeMillis: Long,
    val arrivalTimeMillis: Long
)

@Serializable
data class Itinerary(
    val segments: List<ItinerarySegment>,
    val totalDistance: Double,
    val totalBasePrice: Money,
    val carbonOffsetCents: Long = 0
) {
    val totalDurationMinutes: Int get() {
        if (segments.isEmpty()) return 0
        return ((segments.last().arrivalTimeMillis - segments.first().departureTimeMillis) / 60000).toInt()
    }
}
