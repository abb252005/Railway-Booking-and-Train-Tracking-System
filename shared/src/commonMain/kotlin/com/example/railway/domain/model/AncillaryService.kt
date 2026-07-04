package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AncillaryType {
    MEAL,
    WIFI,
    LUGGAGE,
    PET,
    BICYCLE
}

@Serializable
data class AncillaryService(
    val id: String,
    val type: AncillaryType,
    val name: String,
    val description: String,
    val price: Money
)
