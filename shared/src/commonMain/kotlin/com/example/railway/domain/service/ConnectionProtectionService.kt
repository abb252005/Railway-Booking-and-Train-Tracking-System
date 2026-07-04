package com.example.railway.domain.service

import com.example.railway.domain.model.Booking
import com.example.railway.domain.model.TrainPosition

class ConnectionProtectionService(
    private val aiService: OpenRouterApiService? = null
) {
    fun analyzeConnectionRisk(
        currentBooking: Booking,
        currentPosition: TrainPosition,
        connectingTrainId: String?,
        connectingDepartureTime: Long
    ): ConnectionRiskReport {
        val estimatedArrivalAtConnection = currentPosition.lastUpdateTime + (currentPosition.estimatedTimeRemainingMinutes * 60 * 1000).toLong()
        val bufferMillis = connectingDepartureTime - estimatedArrivalAtConnection
        val bufferMinutes = bufferMillis / 60000.0
        
        val riskLevel = when {
            bufferMinutes < 0 -> RiskLevel.MISSED
            bufferMinutes < 15 -> RiskLevel.CRITICAL
            bufferMinutes < 45 -> RiskLevel.WARNING
            else -> RiskLevel.LOW
        }
        
        return ConnectionRiskReport(
            riskLevel = riskLevel,
            bufferMinutes = bufferMinutes,
            suggestedAction = when (riskLevel) {
                RiskLevel.MISSED -> "Auto-rebook requested. Next available train in 1h 20m."
                RiskLevel.CRITICAL -> "Connection tight. Conductor notified to hold train if possible."
                RiskLevel.WARNING -> "Minor delay. Proceed to gate quickly upon arrival."
                RiskLevel.LOW -> "On track for smooth connection."
            }
        )
    }
}

enum class RiskLevel { LOW, WARNING, CRITICAL, MISSED }

data class ConnectionRiskReport(
    val riskLevel: RiskLevel,
    val bufferMinutes: Double,
    val suggestedAction: String
)
