package com.example.railway.util

expect fun currentTimeMillis(): Long

fun formatDuration(minutes: Double): String {
    val totalMinutes = minutes.toInt()
    val hours = totalMinutes / 60
    val remainingMinutes = totalMinutes % 60
    
    return when {
        hours > 0 -> "${hours}h ${remainingMinutes}m"
        else -> "${remainingMinutes}m"
    }
}
