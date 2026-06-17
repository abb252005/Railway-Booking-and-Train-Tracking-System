package com.example.railway.domain.service

import com.example.railway.domain.model.Route
import com.example.railway.domain.repository.RailwayRepository
import kotlinx.coroutines.flow.*

class WalletManager(
    private val repository: RailwayRepository
) {
    fun getBalance(userId: String): Flow<Double> = repository.getWalletBalance(userId)

    suspend fun calculatePotentialDiscount(userId: String, route: Route, basePrice: Double): Double {
        val rideCount = repository.getUserBookingCount(userId)
        if (rideCount < 10) return 0.0

        // 1 cent per km
        val calculatedDiscount = route.distance * 0.01
        // Cap at 50% of base price
        val maxDiscount = basePrice * 0.5
        
        return kotlin.math.min(calculatedDiscount, maxDiscount)
    }

    suspend fun addBonusPoints(userId: String, amount: Double) {
        val currentBalance = repository.getWalletBalance(userId).first()
        repository.updateWalletBalance(userId, currentBalance + amount)
    }

    suspend fun redeemPoints(userId: String, amount: Double) {
        val currentBalance = repository.getWalletBalance(userId).first()
        repository.updateWalletBalance(userId, kotlin.math.max(0.0, currentBalance - amount))
    }

    suspend fun initializeNewUserIfNeeded(userId: String) {
        // In a real app, we'd check if a specific 'isNew' flag is set.
        // For this demo, if the user has 0 balance and 0 rides, give them the $50 perk.
        val currentBalance = repository.getWalletBalance(userId).first()
        val rideCount = repository.getUserBookingCount(userId)
        if (currentBalance == 0.0 && rideCount == 0L) {
            repository.updateWalletBalance(userId, 50.0)
        }
    }
}
