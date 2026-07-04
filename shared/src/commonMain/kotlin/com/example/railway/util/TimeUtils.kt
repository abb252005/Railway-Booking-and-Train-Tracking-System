package com.example.railway.util

expect fun currentTimeMillis(): Long

fun formatDuration(minutes: Double, strings: com.example.railway.ui.theme.RailwayStrings? = null): String {
    val totalMinutes = minutes.toInt()
    val hours = totalMinutes / 60
    val remainingMinutes = totalMinutes % 60
    
    val h = strings?.hourShort ?: "h"
    val m = strings?.minuteShort ?: "m"
    
    return when {
        hours > 0 -> "$hours$h $remainingMinutes$m"
        else -> "$remainingMinutes$m"
    }
}
