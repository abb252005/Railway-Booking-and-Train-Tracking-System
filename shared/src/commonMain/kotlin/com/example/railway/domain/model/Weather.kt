package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WeatherCondition {
    CLEAR,
    RAIN,
    HEAVY_RAIN,
    SNOW,
    HEAVY_SNOW,
    FOG
}

@Serializable
data class WeatherInfo(
    val condition: WeatherCondition,
    val temperatureCelsius: Double,
    val windSpeedKmH: Double,
    val visibilityKm: Double = 10.0
)
