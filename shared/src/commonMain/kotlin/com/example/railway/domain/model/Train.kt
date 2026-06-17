package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Train(
    val id: String,
    val name: String,
    val status: TrainStatus,
    val carriages: List<Carriage> = emptyList(),
    val schedule: List<ScheduleEntry> = emptyList()
) {
    val totalSeats: Int get() = carriages.sumOf { it.capacity }
}

@Serializable
enum class TrainStatus {
    RUNNING,
    DELAYED,
    SCHEDULED,
    CANCELLED,
    PENDING,
    WAITING
}
