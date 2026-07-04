package com.example.railway.domain.service

import com.example.railway.domain.model.*
import kotlin.math.atan2
import kotlin.math.sin

class PhysicsEngine {
    
    fun calculateCurrentMaxSpeed(
        baseMaxSpeedKmH: Double,
        route: Route,
        progress: Double,
        weather: WeatherInfo,
        trainId: String = ""
    ): Double {
        var effectiveMaxSpeed = baseMaxSpeedKmH

        // 1. Apply Track Class Limit (Safety Regulation)
        val trackLimitMph = when (route.trackClass) {
            TrackClass.CLASS_1 -> 15
            TrackClass.CLASS_2 -> 30
            TrackClass.CLASS_3 -> 60
            TrackClass.CLASS_4 -> 80
            TrackClass.CLASS_5 -> 90
            TrackClass.CLASS_6 -> 110
            TrackClass.CLASS_7 -> 125
            TrackClass.CLASS_8 -> 150
            TrackClass.CLASS_9 -> 200
        }
        
        // Part 1: Special Protocol for Acela on Class 7/8
        // Acela is authorized for higher speeds than standard regional trains on specific segments
        val isAcela = trainId.contains("2151") || trainId.contains("2163") || trainId.startsWith("ACE")
        val authorizedLimitMph = if (isAcela && route.trackClass >= TrackClass.CLASS_7) {
             trackLimitMph // Authorized for max class speed
        } else if (route.trackClass >= TrackClass.CLASS_7) {
            110 // Standard regional cap on Class 7+ (Part 1 Safety Protocol)
        } else {
            trackLimitMph
        }

        val trackLimitKmH = authorizedLimitMph * 1.60934
        effectiveMaxSpeed = minOf(effectiveMaxSpeed, trackLimitKmH)
// ... rest of the code ...

        // 2. Apply Slow Orders
        val currentSlowOrder = route.slowOrders.find { progress in it.startProgress..it.endProgress }
        if (currentSlowOrder != null) {
            effectiveMaxSpeed = minOf(effectiveMaxSpeed, currentSlowOrder.speedLimitKmH)
        }
// ... rest of the code ...

        // 2. Apply Weather Reductions
        val weatherMultiplier = when (weather.condition) {
            WeatherCondition.HEAVY_RAIN -> 0.8
            WeatherCondition.HEAVY_SNOW -> 0.6
            WeatherCondition.FOG -> 0.7
            WeatherCondition.RAIN -> 0.95
            WeatherCondition.SNOW -> 0.9
            WeatherCondition.CLEAR -> 1.0
        }
        effectiveMaxSpeed *= weatherMultiplier

        return effectiveMaxSpeed
    }

    fun calculateGradeImpact(route: Route, progress: Double): Double {
        if (route.elevationProfile.size < 2) return 0.0
        
        val sortedProfile = route.elevationProfile.sortedBy { it.progress }
        val nextPointIndex = sortedProfile.indexOfFirst { it.progress > progress }
        if (nextPointIndex <= 0) return 0.0
        
        val p1 = sortedProfile[nextPointIndex - 1]
        val p2 = sortedProfile[nextPointIndex]
        
        val distPercent = p2.progress - p1.progress
        if (distPercent <= 0) return 0.0
        
        val elevationDiff = p2.elevationMeters - p1.elevationMeters
        val distanceMeters = distPercent * route.distance * 1000.0
        
        // Grade = rise / run
        val grade = elevationDiff / distanceMeters
        
        // Simple impact: -10% acceleration for every 1% upward grade
        // In reality, this is more complex, but this is a good simulation factor.
        return grade * -10.0 
    }
    
    fun calculateAccelerationMultiplier(weather: WeatherInfo, gradeImpact: Double): Double {
        // Traction impact from weather
        val tractionMultiplier = when (weather.condition) {
            WeatherCondition.HEAVY_SNOW, WeatherCondition.HEAVY_RAIN -> 0.7
            WeatherCondition.SNOW, WeatherCondition.RAIN -> 0.9
            else -> 1.0
        }
        
        // Grade impact is added (if grade is positive/uphill, impact is negative)
        return (tractionMultiplier + gradeImpact).coerceIn(0.1, 1.5)
    }

    fun calculateEnergyConsumption(
        speedKmH: Double,
        gradeImpact: Double,
        isAcela: Boolean,
        deltaTimeHours: Double
    ): Double {
        // Base consumption: Acela (Electric) is more efficient per seat but uses more raw KW
        // Regional (Diesel/Electric) has different profile
        val baseKw = if (isAcela) 4000.0 else 3000.0
        
        // Speed impact (Drag is proportional to v^2, but simplified for simulation)
        val speedFactor = (speedKmH / 100.0).coerceAtLeast(0.1)
        
        // Grade impact: Uphill (+grade, -gradeImpact) increases load
        val loadFactor = 1.0 - gradeImpact 
        
        val kwUsed = baseKw * speedFactor * loadFactor
        return kwUsed * deltaTimeHours
    }

    fun calculateSignalImpact(distanceToTrainAheadKm: Double): Pair<SignalState, Double> {
        return when {
            distanceToTrainAheadKm < 2.0 -> SignalState.RESTRICTING to 0.0
            distanceToTrainAheadKm < 5.0 -> SignalState.APPROACH to 0.4 // 40% speed
            else -> SignalState.CLEAR to 1.0
        }
    }
}
