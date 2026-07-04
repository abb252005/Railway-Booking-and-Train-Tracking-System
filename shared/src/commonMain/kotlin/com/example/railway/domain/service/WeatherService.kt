package com.example.railway.domain.service

import com.example.railway.domain.model.WeatherCondition
import com.example.railway.domain.model.WeatherInfo
import kotlin.random.Random

class WeatherService {
    fun getCurrentWeather(lat: Double, lng: Double): WeatherInfo {
        // For simulation purposes, we generate semi-random weather based on coordinates
        // In a real app, this would call a weather API.
        val hash = (lat * 100 + lng).toInt()
        val random = Random(hash)
        
        val condition = when (random.nextInt(100)) {
            in 0..70 -> WeatherCondition.CLEAR
            in 71..85 -> WeatherCondition.RAIN
            in 86..92 -> WeatherCondition.HEAVY_RAIN
            in 93..96 -> WeatherCondition.SNOW
            in 97..98 -> WeatherCondition.HEAVY_SNOW
            else -> WeatherCondition.FOG
        }
        
        return WeatherInfo(
            condition = condition,
            temperatureCelsius = 15.0 + random.nextDouble(-10.0, 10.0),
            windSpeedKmH = random.nextDouble(0.0, 40.0),
            visibilityKm = if (condition == WeatherCondition.FOG) 0.5 else 10.0
        )
    }
}
