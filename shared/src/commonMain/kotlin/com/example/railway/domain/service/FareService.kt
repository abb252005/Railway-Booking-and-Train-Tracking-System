package com.example.railway.domain.service

import com.example.railway.domain.model.Money
import com.example.railway.domain.model.FareClass

data class NightOwlFare(
    val originCity: String,
    val destinationCity: String,
    val amount: Money
)

object FareService {
    private val pricingEngine = DynamicPricingEngine()

    // Official published Night Owl sample fares (Table 16)
    val nightOwlFares = listOf(
// ... existing fares ...
        NightOwlFare("New Haven", "Washington", Money("USD", 2500)),
        NightOwlFare("Washington", "Boston", Money("USD", 3000))
    )

    fun getQuote(
        originCity: String, 
        destinationCity: String,
        daysUntilDeparture: Int = 14,
        loadFactor: Double = 0.3,
        departureHour: Int = 12
    ): Money {
        val promo = nightOwlFares.find { 
            it.originCity.contains(originCity, ignoreCase = true) && 
            it.destinationCity.contains(destinationCity, ignoreCase = true) 
        }
        val baseAmount = promo?.amount?.amountCents ?: (8000..20000).random().toLong()
        
        val dynamicAmount = pricingEngine.calculateDynamicFare(baseAmount, daysUntilDeparture, loadFactor, departureHour)
        
        return Money("USD", dynamicAmount)
    }

    fun applyPassengerDiscount(baseFare: Money, passengerType: String): Money {
        val discountMultiplier = when (passengerType.uppercase()) {
            "CHILD" -> 0.50 // 50% discount
            "SENIOR" -> 0.85 // 15% discount
            "MILITARY", "VETERAN" -> 0.90 // 10% discount
            "YOUTH" -> 0.75 // 25% discount
            "STUDENT" -> 0.90 // 10% discount
            "INFANT" -> 0.0 // Free (Part 2 Policy)
            else -> 1.0
        }
        return baseFare.copy(amountCents = (baseFare.amountCents * discountMultiplier).toLong())
    }

    // Part 2: Baggage Fees
    fun calculateBaggageFee(numCheckedBags: Int, numAdditionalBags: Int): Money {
        val baseBagsFee = if (numCheckedBags > 2) (numCheckedBags - 2) * 2000L else 0L
        val additionalBagsFee = numAdditionalBags * 2000L // $20.00 each
        return Money("USD", baseBagsFee + additionalBagsFee)
    }

    // Part 2: Refund Policy
    fun getRefundRules(fareClass: FareClass, fareProduct: String): String {
        return when (fareProduct.uppercase()) {
            "FLEXIBLE" -> "100% Refundable. No fees."
            "VALUE" -> "Refundable with 25% fee if > 15 days, else 50% fee."
            "SAVER" -> "Non-refundable. Credit only if cancelled > 24h prior."
            else -> "Standard rules apply."
        }
    }
}
