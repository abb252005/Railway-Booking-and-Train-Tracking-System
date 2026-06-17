package com.example.railway.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.domain.service.ScheduleManager
import com.example.railway.domain.service.TrainSimulationManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackingViewModel(
    stations: List<Station>,
    repository: RailwayRepository
) : ViewModel() {
    private val simulationManager = TrainSimulationManager(viewModelScope, stations)
    private val scheduleManager = ScheduleManager(viewModelScope, repository)
    
    val trainPositions = simulationManager.trainPositions

    fun startTracking(train: Train, routePath: List<Route>) {
        simulationManager.startSimulation(train, routePath)
    }

    fun startScheduledTracking(trains: List<Train>, routes: List<Route>) {
        viewModelScope.launch {
            scheduleManager.initializeSchedules(trains)
            scheduleManager.startRotationCycle()
            
            // Sync simulations with active schedules
            scheduleManager.activeSchedules.collect { schedules ->
                // Stop simulations for trains no longer in active schedules
                val activeIds = schedules.map { it.trainId }.toSet()
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
                            simulationManager.startSimulation(train, listOf(route))
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
