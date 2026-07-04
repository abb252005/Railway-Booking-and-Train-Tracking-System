package com.example.railway.domain.service

import com.example.railway.domain.model.Money
import kotlinx.serialization.Serializable

@Serializable
enum class LoyaltyTier {
    BLUE, SILVER, GOLD, PLATINUM
}

data class UserLoyaltyInfo(
    val userId: String,
    val points: Long,
    val tier: LoyaltyTier,
    val milesYTD: Double
)

class LoyaltyService {
    
    fun calculatePointsEarned(ticketPrice: Money, tier: LoyaltyTier): Long {
        val multiplier = when (tier) {
            LoyaltyTier.BLUE -> 2
            LoyaltyTier.SILVER -> 4
            LoyaltyTier.GOLD -> 6
            LoyaltyTier.PLATINUM -> 10
        }
        return (ticketPrice.amountCents / 100) * multiplier
    }
    
    fun determineTier(milesYTD: Double): LoyaltyTier {
        return when {
            milesYTD >= 50000 -> LoyaltyTier.PLATINUM
            milesYTD >= 25000 -> LoyaltyTier.GOLD
            milesYTD >= 10000 -> LoyaltyTier.SILVER
            else -> LoyaltyTier.BLUE
        }
    }
}
