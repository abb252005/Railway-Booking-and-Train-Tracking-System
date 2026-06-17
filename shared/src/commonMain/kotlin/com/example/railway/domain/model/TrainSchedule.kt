package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleEntry(
    val id: String,
    val trainId: String,
    val sourceStationId: String,
    val destinationStationId: String,
    val departureTimeMillis: Long,
    val arrivalTimeMillis: Long,
    val routeId: String,
    val isActive: Boolean = true
)
