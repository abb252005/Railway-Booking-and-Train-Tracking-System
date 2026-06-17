package com.example.railway.domain.service

import com.example.railway.domain.model.*
import com.example.railway.util.currentTimeMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

class TrainSimulationManager(
    private val scope: CoroutineScope,
    private val stations: List<Station>,
    private val simulationSpeedMultiplier: Double = 1.0
) {
    private val _trainPositions = MutableStateFlow<Map<String, TrainPosition>>(emptyMap())
    val trainPositions: StateFlow<Map<String, TrainPosition>> = _trainPositions.asStateFlow()

    private val activeSimulations = mutableMapOf<String, Job>()

    fun startSimulation(train: Train, routePath: List<Route>, initialElapsedMillis: Long = 0L) {
        // Cancel existing simulation for this train if any
        activeSimulations[train.id]?.cancel()

        activeSimulations[train.id] = scope.launch {
            try {
                var currentPathIndex = 0
                val totalRoutes = routePath.size
                var totalElapsedForManager = initialElapsedMillis

                while (currentPathIndex < totalRoutes) {
                    val currentRoute = routePath[currentPathIndex]
                    val sourceStation = stations.find { it.id == currentRoute.sourceStationId } ?: break
                    val destStation = stations.find { it.id == currentRoute.destinationStationId } ?: break

                    // Path including waypoints
                    val fullPathPoints = listOf(Waypoint(sourceStation.latitude, sourceStation.longitude)) + 
                                       currentRoute.waypoints + 
                                       listOf(Waypoint(destStation.latitude, destStation.longitude))

                    val segmentDurationMillis = (currentRoute.estimatedTimeMinutes * 60 * 1000L / simulationSpeedMultiplier).toLong()
                    val updateInterval = 50L 
                    val totalSteps = (segmentDurationMillis / updateInterval).toInt().coerceAtLeast(1)

                    // Skip steps already passed
                    val startStep = (totalElapsedForManager / updateInterval).toInt().coerceIn(0, totalSteps)
                    totalElapsedForManager = 0 

                    for (step in startStep..totalSteps) {
                        val progress = step.toDouble() / totalSteps
                        
                        // Interpolate along the list of waypoints
                        val pointProgress = progress * (fullPathPoints.size - 1)
                        val fromIndex = pointProgress.toInt().coerceAtMost(fullPathPoints.size - 2)
                        val toIndex = fromIndex + 1
                        val localProgress = (pointProgress - fromIndex).toFloat()
                        
                        val p1 = fullPathPoints[fromIndex]
                        val p2 = fullPathPoints[toIndex]
                        
                        val currentLat = p1.latitude + (p2.latitude - p1.latitude) * localProgress
                        val currentLng = p1.longitude + (p2.longitude - p1.longitude) * localProgress

                        // Calculate bearing
                        val bearing = kotlin.math.atan2(
                            p2.longitude - p1.longitude,
                            p2.latitude - p1.latitude
                        ) * 180 / kotlin.math.PI

                        // Calculate total remaining time
                        val remainingInSegment = currentRoute.estimatedTimeMinutes * (1.0 - progress)
                        val remainingInFutureSegments = routePath.drop(currentPathIndex + 1).sumOf { it.estimatedTimeMinutes.toDouble() }
                        val totalRemainingMinutes = remainingInSegment + remainingInFutureSegments

                        val distanceRemainingInSegment = currentRoute.distance * (1.0 - progress)
                        val distanceRemainingInFutureSegments = routePath.drop(currentPathIndex + 1).sumOf { it.distance }
                        val totalDistanceRemaining = distanceRemainingInSegment + distanceRemainingInFutureSegments

                        // Actual real-world speed
                        val speed = (currentRoute.distance / currentRoute.estimatedTimeMinutes) * 60

                        _trainPositions.update { positions ->
                            positions + (train.id to TrainPosition(
                                trainId = train.id,
                                latitude = currentLat,
                                longitude = currentLng,
                                currentRouteId = currentRoute.id,
                                nextDestinationStationId = destStation.id,
                                progress = progress,
                                estimatedTimeRemainingMinutes = totalRemainingMinutes,
                                distanceRemainingKm = totalDistanceRemaining,
                                lastUpdateTime = currentTimeMillis(),
                                bearing = bearing,
                                speedKmH = speed
                            ))
                        }
                        delay(updateInterval.milliseconds)
                    }
                    currentPathIndex++
                }
            } finally {
                activeSimulations.remove(train.id)
                // Keep the final position instead of removing it
            }
        }
    }

    fun startScheduledSimulation(trains: List<Train>, routes: List<Route>) {
        scope.launch {
            while (isActive) {
                // Use a consistent time reference
                // For demo, we use the same baseTime logic as USDataGenerator
                val baseTime = 1718150400000L 
                val now = baseTime + (25 * 25 * 60000L) 
                
                val trainsThatShouldRun = trains.filter { train ->
                    train.schedule.any { it.departureTimeMillis <= now && it.arrivalTimeMillis > now }
                }

                // Process all trains that should be running
                trainsThatShouldRun.forEach { train ->
                    if (!activeSimulations.containsKey(train.id)) {
                        val currentEntry = train.schedule.find { it.departureTimeMillis <= now && it.arrivalTimeMillis > now }
                        if (currentEntry != null) {
                            val route = routes.find { it.id == currentEntry.routeId }
                            if (route != null) {
                                // Calculate how much time has already passed in the journey
                                val elapsedMillis = now - currentEntry.departureTimeMillis
                                startSimulation(train, listOf(route), initialElapsedMillis = elapsedMillis)
                            }
                        }
                    }
                }

                delay(2000.milliseconds)
            }
        }
    }

    fun stopSimulation(trainId: String) {
        activeSimulations[trainId]?.cancel()
        activeSimulations.remove(trainId)
        _trainPositions.update { it - trainId }
    }

    fun isSimulating(trainId: String): Boolean = activeSimulations.containsKey(trainId)
}
