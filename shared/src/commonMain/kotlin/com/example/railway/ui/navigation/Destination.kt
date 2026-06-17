package com.example.railway.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
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
    data object History : Destination

    @Serializable
    data object AdminDashboard : Destination

    @Serializable
    data object Stations : Destination

    @Serializable
    data object UserCabinet : Destination
}
