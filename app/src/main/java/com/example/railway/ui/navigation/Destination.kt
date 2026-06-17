package com.example.railway.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object Login : Destination

    @Serializable
    data object Home : Destination

    @Serializable
    data object RouteDiscovery : Destination

    @Serializable
    data object LiveTracking : Destination

    @Serializable
    data object Booking : Destination

    @Serializable
    data object AdminDashboard : Destination

    @Serializable
    data object Stations : Destination
}
