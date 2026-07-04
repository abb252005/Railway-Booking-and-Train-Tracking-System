package com.example.railway.domain.service

import com.example.railway.domain.model.Money
import kotlin.math.pow

class DynamicPricingEngine {
    
    fun calculateDynamicFare(
        baseAmountCents: Long,
        daysUntilDeparture: Int,
        loadFactor: Double, // 0.0 to 1.0 (bookings/capacity)
        departureHour: Int, // 0-23
        isHoliday: Boolean = false
    ): Long {
        var multiplier = 1.0
        
        // 1. Time-based pricing (Prices increase as departure nears)
        multiplier *= when {
            daysUntilDeparture > 21 -> 0.8
            daysUntilDeparture > 14 -> 1.0
            daysUntilDeparture > 7 -> 1.2
            daysUntilDeparture > 1 -> 1.5
            else -> 2.0
        }
        
        // 2. Yield-based pricing (Exponential increase as train fills up)
        multiplier *= (1.0 + loadFactor.pow(3.0))
        
        // 3. Peak/Off-Peak (Amtrak Standard Policy)
        // Morning Peak: 6am - 9am, Evening Peak: 4pm - 7pm
        if (departureHour in 6..9 || departureHour in 16..19) {
            multiplier *= 1.25 // 25% peak surcharge
        } else if (departureHour in 22..23 || departureHour in 0..4) {
            multiplier *= 0.85 // 15% off-peak discount
        }

        // 4. Holiday multiplier
        if (isHoliday) {
            multiplier *= 1.4
        }
        
        return (baseAmountCents * multiplier).toLong()
    }
}
