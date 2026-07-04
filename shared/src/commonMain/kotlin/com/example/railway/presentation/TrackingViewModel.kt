package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.domain.service.ScheduleManager
import com.example.railway.domain.service.ConnectionProtectionService
import com.example.railway.domain.service.TrainSimulationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrackingViewModel(
    stations: List<Station>,
    repository: RailwayRepository,
) : ViewModel() {
    private val simulationManager = TrainSimulationManager(viewModelScope, stations)
    private val scheduleManager = ScheduleManager(viewModelScope, repository)
    private val connectionService = ConnectionProtectionService()
    
    val trainPositions = simulationManager.trainPositions
    val bookings = repository.getAllBookings().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun checkConnectionRisk(booking: Booking, position: TrainPosition): com.example.railway.domain.service.ConnectionRiskReport? {
        // Find if there's a following trip in the same day (simplified)
        return connectionService.analyzeConnectionRisk(
            currentBooking = booking,
            currentPosition = position,
            connectingTrainId = "T-NEXT", // Simulated
            connectingDepartureTime = booking.arrivalTimeMillis + (40 * 60 * 1000) // 40 min planned transfer
        )
    }

    fun startTracking(train: Train, routePath: List<Route>, isUserBooked: Boolean = false) {
        simulationManager.startSimulation(train, routePath, isUserBooked = isUserBooked)
    }

    fun startScheduledTracking(trains: List<Train>, routes: List<Route>, userBookedTrainIds: Set<String> = emptySet()) {
        viewModelScope.launch {
            scheduleManager.initializeSchedules(trains)
            scheduleManager.startRotationCycle()
            
            // Sync simulations with active schedules
            scheduleManager.activeSchedules.collect { schedules ->
                // Stop simulations for trains no longer in active schedules
                val activeIds = schedules.asSequence().map { it.trainId }.toSet()
                val currentlySimulating = simulationManager.trainPositions.value.keys
                
                currentlySimulating.forEach { id ->
                    if (!activeIds.contains(id)) {
                        simulationManager.stopSimulation(id)
                    }
                }

                // Start simulations for new active schedules
                schedules.forEach { sch ->
                    if (!simulationManager.isSimulating(sch.trainId)) {
                        val train = trains.find { it.id == sch.trainId }
                        val route = routes.find { it.id == sch.routeId }
                        if (train != null && route != null) {
                            simulationManager.startSimulation(train, listOf(route), isUserBooked = userBookedTrainIds.contains(train.id))
                        }
                    }
                }
            }
        }
    }

    fun stopTracking(trainId: String) {
        simulationManager.stopSimulation(trainId)
    }
}
