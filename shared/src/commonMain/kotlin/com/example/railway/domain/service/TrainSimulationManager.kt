package com.example.railway.domain.service

import com.example.railway.domain.model.*
import com.example.railway.util.currentTimeMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

class TrainSimulationManager(
    private val scope: CoroutineScope,
    private val stations: List<Station>,
    private val simulationSpeedMultiplier: Double = 1.0
) {
    companion object {
        private const val ACCELERATION_KMH_PER_SEC = 1.0 // User requirement: 1km/h per second
    }

    private val _trainPositions = MutableStateFlow<Map<String, TrainPosition>>(emptyMap())
    val trainPositions: StateFlow<Map<String, TrainPosition>> = _trainPositions.asStateFlow()

    private val activeSimulations = mutableMapOf<String, Job>()
    private var socketClient: SocketClient? = null
    
    private val physicsEngine = PhysicsEngine()
    private val weatherService = WeatherService()

    fun connectToServer(host: String = "192.168.1.39", port: Int = 8080) {
// ... existing connectToServer code ...
        val client = SocketClient(host, port)
        socketClient = client
        scope.launch {
            client.trainUpdates.collect { update ->
                _trainPositions.update { it + (update.trainId to update) }
            }
        }
        scope.launch {
            client.connect()
        }
    }

    fun startSimulation(train: Train, routePath: List<Route>, initialElapsedMillis: Long = 0L, isUserBooked: Boolean = false) {
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

                    val segmentDurationMillis = currentRoute.estimatedTimeMinutes * 60 * 1000L
                    val updateInterval = 50L
                    
                    // 1. Station Dwell Time / Boarding Phase (Part 1)
                    // If at the start of a route, wait for 5 minutes (300,000 ms) in BOARDING state
                    if ((totalElapsedForManager == 0L) && (currentPathIndex == 0)) {
                        val boardingTimeMillis = 5 * 60 * 1000.0
                        var boardingElapsed = 0.0
                        while (boardingElapsed < boardingTimeMillis) {
                            _trainPositions.update { positions ->
                                positions + (train.id to TrainPosition(
                                    trainId = train.id,
                                    latitude = sourceStation.latitude,
                                    longitude = sourceStation.longitude,
                                    currentRouteId = currentRoute.id,
                                    nextDestinationStationId = destStation.id,
                                    progress = 0.0,
                                    estimatedTimeRemainingMinutes = currentRoute.estimatedTimeMinutes.toDouble(),
                                    distanceRemainingKm = currentRoute.distance,
                                    lastUpdateTime = currentTimeMillis(),
                                    bearing = 0.0,
                                    speedKmH = 0.0,
                                    status = DetailedTrainStatus.BOARDING,
                                    isUserBooked = isUserBooked
                                ))
                            }
                            boardingElapsed += updateInterval * simulationSpeedMultiplier
                            delay(updateInterval.milliseconds)
                        }
                    }

                    // 2. Movement Phase
                    // Initial Progress estimate if starting mid-trip
                    var currentProgress = (totalElapsedForManager.toDouble() / segmentDurationMillis).coerceIn(0.0, 1.0)
                    var simulatedTimeInSegment = totalElapsedForManager.toDouble()
                    var totalEnergyKwH = 0.0
                    
                    // Initialize speed: 0 if starting at station, else assume we were at max speed
                    var currentActualSpeedKmH = if (totalElapsedForManager > 0) {
                        val baseRawSpeedKmH = (currentRoute.distance / currentRoute.estimatedTimeMinutes) * 60
                        val maxTrainSpeedKmH = train.maxSpeedMph * 1.60934
                        minOf(baseRawSpeedKmH, maxTrainSpeedKmH)
                    } else 0.0
                    
                    totalElapsedForManager = 0

                    while (currentProgress < 1.0) {
                        val currentLat = interpolateLat(fullPathPoints, currentProgress)
                        val currentLng = interpolateLng(fullPathPoints, currentProgress)
                        
                        val weather = weatherService.getCurrentWeather(currentLat, currentLng)
                        
                        // Base Speed (Schedule-based)
                        val baseRawSpeedKmH = (currentRoute.distance / currentRoute.estimatedTimeMinutes) * 60
                        val maxTrainSpeedKmH = train.maxSpeedMph * 1.60934
                        val baseMaxSpeed = minOf(baseRawSpeedKmH, maxTrainSpeedKmH)
                        
                        // Physics-Adjusted Max Speed
                        val vMax = physicsEngine.calculateCurrentMaxSpeed(baseMaxSpeed, currentRoute, currentProgress, weather, train.id)
                        
                        // Signaling Impact (PTC Awareness)
                        val otherTrains = trainPositions.value.filter { it.key != train.id && it.value.currentRouteId == currentRoute.id }
                        val trainAhead = otherTrains.values.filter { it.progress > currentProgress }.minByOrNull { it.progress }
                        
                        val (signalState, signalMultiplier) = if (trainAhead != null) {
                            val distanceAheadKm = (trainAhead.progress - currentProgress) * currentRoute.distance
                            physicsEngine.calculateSignalImpact(distanceAheadKm)
                        } else {
                            SignalState.CLEAR to 1.0
                        }

                        // Acceleration/Deceleration Logic
                        val deltaTimeSeconds = (updateInterval * simulationSpeedMultiplier) / 1000.0
                        val maxDeltaSpeed = ACCELERATION_KMH_PER_SEC * deltaTimeSeconds
                        
                        val gradeImpact = physicsEngine.calculateGradeImpact(currentRoute, currentProgress)
                        val tractionMultiplier = physicsEngine.calculateAccelerationMultiplier(weather, gradeImpact)
                        
                        // Target speed based on infrastructure and signaling
                        val targetSpeed = vMax * tractionMultiplier * signalMultiplier
                        
                        // Braking curve for destination: stop from speed v at 1km/h/s takes v^2/7200 km
                        val distanceRemainingInSegment = currentRoute.distance * (1.0 - currentProgress)
                        val targetSpeedForStopping = kotlin.math.sqrt(distanceRemainingInSegment * 7200.0)
                        
                        val finalTargetSpeed = minOf(targetSpeed, targetSpeedForStopping)
                        
                        // Incrementally adjust speed towards target
                        if (currentActualSpeedKmH < finalTargetSpeed) {
                            currentActualSpeedKmH = (currentActualSpeedKmH + maxDeltaSpeed).coerceAtMost(finalTargetSpeed)
                        } else if (currentActualSpeedKmH > finalTargetSpeed) {
                            currentActualSpeedKmH = (currentActualSpeedKmH - maxDeltaSpeed).coerceAtLeast(finalTargetSpeed)
                        }
                        
                        val currentSpeed = currentActualSpeedKmH.coerceAtLeast(1.0)

                        // Update Progress: deltaDistance = speed * deltaTime
                        val deltaTimeHours = deltaTimeSeconds / 3600.0
                        val deltaDistance = currentSpeed * deltaTimeHours
                        currentProgress = (currentProgress + (deltaDistance / currentRoute.distance)).coerceAtMost(1.0)
                        
                        // Energy Consumption
                        val isAcela = train.id.contains("2151") || train.id.contains("2163")
                        totalEnergyKwH += physicsEngine.calculateEnergyConsumption(currentSpeed, gradeImpact, isAcela, deltaTimeHours)

                        simulatedTimeInSegment += updateInterval * simulationSpeedMultiplier

                        // Calculate bearing
                        val bearing = calculateBearing(fullPathPoints, currentProgress)

                        // Calculate total remaining time
                        val distanceRemainingInFutureSegments = routePath.drop(currentPathIndex + 1).sumOf { it.distance }
                        val totalDistanceRemaining = distanceRemainingInSegment + distanceRemainingInFutureSegments
                        
                        val etaMinutes = if (currentSpeed > 5.0) {
                            (totalDistanceRemaining / currentSpeed) * 60.0
                        } else {
                            val baseSpeedMinutes = (currentRoute.distance / currentRoute.estimatedTimeMinutes) / 60.0
                            totalDistanceRemaining / baseSpeedMinutes.coerceAtLeast(0.1)
                        }

                        _trainPositions.update { positions ->
                            positions + (train.id to TrainPosition(
                                trainId = train.id,
                                latitude = currentLat,
                                longitude = currentLng,
                                currentRouteId = currentRoute.id,
                                nextDestinationStationId = destStation.id,
                                progress = currentProgress,
                                estimatedTimeRemainingMinutes = etaMinutes,
                                distanceRemainingKm = totalDistanceRemaining,
                                lastUpdateTime = currentTimeMillis(),
                                bearing = bearing,
                                speedKmH = currentSpeed,
                                weather = weather,
                                status = DetailedTrainStatus.EN_ROUTE,
                                energyConsumedKwH = totalEnergyKwH,
                                signalState = signalState,
                                isUserBooked = isUserBooked
                            ))
                        }
                        
                        if (currentProgress >= 1.0) break
                        
                        delay(updateInterval.milliseconds)
                    }
                    currentPathIndex++
                }
            } finally {
                activeSimulations.remove(train.id)
            }
        }
    }

    private fun interpolateLat(points: List<Waypoint>, progress: Double): Double {
        val pointProgress = progress * (points.size - 1)
        val fromIndex = pointProgress.toInt().coerceAtMost(points.size - 2)
        val toIndex = fromIndex + 1
        val localProgress = (pointProgress - fromIndex).toFloat()
        val p1 = points[fromIndex]
        val p2 = points[toIndex]
        return p1.latitude + (p2.latitude - p1.latitude) * localProgress
    }

    private fun interpolateLng(points: List<Waypoint>, progress: Double): Double {
        val pointProgress = progress * (points.size - 1)
        val fromIndex = pointProgress.toInt().coerceAtMost(points.size - 2)
        val toIndex = fromIndex + 1
        val localProgress = (pointProgress - fromIndex).toFloat()
        val p1 = points[fromIndex]
        val p2 = points[toIndex]
        return p1.longitude + (p2.longitude - p1.longitude) * localProgress
    }

    private fun calculateBearing(points: List<Waypoint>, progress: Double): Double {
        val pointProgress = progress * (points.size - 1)
        val fromIndex = pointProgress.toInt().coerceAtMost(points.size - 2)
        val toIndex = fromIndex + 1
        val p1 = points[fromIndex]
        val p2 = points[toIndex]
        return kotlin.math.atan2(
            p2.longitude - p1.longitude,
            p2.latitude - p1.latitude
        ) * 180 / kotlin.math.PI
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
