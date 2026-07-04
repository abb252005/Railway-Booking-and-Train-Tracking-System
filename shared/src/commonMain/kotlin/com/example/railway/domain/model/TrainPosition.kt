package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DetailedTrainStatus {
    STATIONARY, BOARDING, DEPARTED, EN_ROUTE, ARRIVING, DELAYED, CANCELLED
}

@Serializable
enum class SignalState { CLEAR, APPROACH, RESTRICTING }

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
    val speedKmH: Double = 0.0,
    val weather: WeatherInfo? = null,
    val status: DetailedTrainStatus = DetailedTrainStatus.EN_ROUTE,
    val energyConsumedKwH: Double = 0.0,
    val signalState: SignalState = SignalState.CLEAR,
    val isUserBooked: Boolean = false
)
