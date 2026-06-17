package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod {
    CARD,
    CASH
}
