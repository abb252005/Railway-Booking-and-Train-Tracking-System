package com.example.railway.util

import com.example.railway.domain.model.*
import kotlin.random.Random

object USDataGenerator {
    fun generateStations(): List<Station> {
        val terminals = listOf("Terminal A", "Terminal B", "Platform 4", "Main Hall", "East Wing", "Gate 7")
        return StateBoundaries.allStates.mapIndexed { index, boundary ->
            val isIsolated = boundary.name == "Alaska" || boundary.name == "Hawaii"
            val isHub = boundary.population.replace(",", "").toLongOrNull()?.let { it > 5000000 } ?: false
            
            val info = if (isIsolated) {
                "Major isolated station in the state of ${boundary.name}. Serves as a key hub for regional/in-state transport."
            } else {
                "Major station in the state of ${boundary.name}. Serves as a key hub for regional transport."
            }

            val timezone = when (boundary.abbreviation) {
                "HI" -> "Pacific/Honolulu"
                "AK" -> "America/Anchorage"
                "CA", "OR", "WA", "NV" -> "America/Los_Angeles"
                "AZ", "CO", "MT", "UT", "WY", "NM" -> "America/Denver"
                "AL", "AR", "IL", "IA", "KS", "LA", "MN", "MS", "MO", "NE", "ND", "OK", "SD", "TN", "TX", "WI" -> "America/Chicago"
                else -> "America/New_York"
            }

            Station(
                id = "s${index + 1}",
                name = boundary.name,
                code = boundary.abbreviation,
                city = boundary.name,
                state = boundary.abbreviation,
                timezone = timezone,
                latitude = boundary.centroid.first,
                longitude = boundary.centroid.second,
                designatedTrainId = "t${index + 1}",
                info = info,
                terminal = terminals.random(),
                hasLounge = isHub,
                hasCheckedBaggage = true,
                hasKiosks = true,
                isHub = isHub
            )
        }
    }

    fun generateTrains(routes: List<Route>): List<Train> {
        val baseTime = 1718150400000L // 2024-06-12 00:00:00 UTC
        val trains = mutableListOf<Train>()

        StateBoundaries.allStates.forEachIndexed { index, boundary ->
            val trainId = "t${index + 1}"
            val schedule = mutableListOf<ScheduleEntry>()
            val sourceStationId = "s${index + 1}"
            val possibleRoutes = routes.filter { it.sourceStationId == sourceStationId }
            val route = possibleRoutes.randomOrNull()
            
            if (route != null) {
                var departureTime = baseTime + (index * 25 * 60000L)
                var duration = route.estimatedTimeMinutes * 60000L
                
                if (boundary.name == "Hawaii") {
                    val startWindow = 4 * 60 * 60000L
                    val endWindow = 22 * 60 * 60000L + 30 * 60000L
                    departureTime = baseTime + Random.nextLong(startWindow, endWindow)
                    duration = Random.nextInt(10, 15) * 60000L
                } else if (boundary.name == "Alaska") {
                    val startWindow = 6 * 60 * 60000L
                    val endWindow = 18 * 60 * 60000L
                    departureTime = baseTime + Random.nextLong(startWindow, endWindow)
                    duration = Random.nextInt(120, 720) * 60000L
                }

                val arrivalTime = departureTime + duration
                val isRunning = false

                schedule.add(ScheduleEntry(
                    id = "sch_${trainId}_${index}",
                    trainId = trainId,
                    departureTimeMillis = departureTime,
                    arrivalTimeMillis = arrivalTime,
                    sourceStationId = sourceStationId,
                    destinationStationId = route.destinationStationId,
                    routeId = route.id,
                    isActive = isRunning
                ))

                val carriageCount = (3..10).random()
                val carriages = (1..carriageCount).map { i ->
                    Carriage(id = "c_${trainId}_$i", number = i, capacity = 50)
                }

                trains.add(Train(
                    id = trainId,
                    name = "${boundary.name} Express",
                    carriages = carriages,
                    status = if (isRunning) TrainStatus.RUNNING else TrainStatus.PENDING,
                    schedule = schedule
                ))
            } else {
                trains.add(Train(
                    id = trainId,
                    name = "${boundary.name} Hub Service",
                    carriages = emptyList(),
                    status = TrainStatus.WAITING,
                    schedule = emptyList()
                ))
            }
        }
        return trains
    }

    fun generateRoutes(stations: List<Station>): List<Route> {
        val routes = mutableListOf<Route>()
        val mainlandStations = stations.filter { it.name != "Alaska" && it.name != "Hawaii" }

        mainlandStations.forEach { s1 ->
            val nearest = mainlandStations.filter { it.id != s1.id }
                .sortedBy { s2 ->
                    val dx = s1.longitude - s2.longitude
                    val dy = s1.latitude - s2.latitude
                    dx * dx + dy * dy
                }
                .take(3)
            
            nearest.forEach { s2 ->
                val dist = calculateDistance(s1, s2)
                val averageSpeedKmH = Random.nextDouble(177.0, 322.0)
                val estimatedTime = (dist / averageSpeedKmH * 60.0).toInt().coerceAtLeast(15)
                
                val slowOrders = mutableListOf<SlowOrder>()
                if (Random.nextInt(10) == 0) { // 10% chance of a slow order
                    val start = Random.nextDouble(0.2, 0.5)
                    slowOrders.add(SlowOrder(
                        startProgress = start,
                        endProgress = start + 0.1,
                        speedLimitKmH = 80.0,
                        reason = "Track Maintenance"
                    ))
                }

                val elevationProfile = listOf(
                    ElevationPoint(0.0, Random.nextDouble(10.0, 50.0)),
                    ElevationPoint(0.5, Random.nextDouble(100.0, 300.0)),
                    ElevationPoint(1.0, Random.nextDouble(10.0, 50.0))
                )

                val trackClass = when {
                    dist > 500 -> TrackClass.CLASS_7 // Long distance regional
                    dist > 200 -> TrackClass.CLASS_5
                    else -> TrackClass.CLASS_4
                }

                routes.add(Route(
                    id = "r_${s1.id}_${s2.id}",
                    sourceStationId = s1.id,
                    destinationStationId = s2.id,
                    distance = dist,
                    estimatedTimeMinutes = estimatedTime,
                    slowOrders = slowOrders,
                    elevationProfile = elevationProfile,
                    trackClass = trackClass
                ))
            }
        }

        listOf("Alaska", "Hawaii").forEach { stateName ->
            val station = stations.find { it.name == stateName }
            if (station != null) {
                val dist = 100.0
                val estimatedTime = if (stateName == "Hawaii") {
                    Random.nextInt(10, 15)
                } else {
                    Random.nextInt(120, 480)
                }

                val waypoints = mutableListOf<Waypoint>()
                val radius = 0.5
                for (angle in 0 until 360 step 45) {
                    val rad = angle * kotlin.math.PI / 180.0
                    waypoints.add(Waypoint(
                        station.latitude + kotlin.math.sin(rad) * radius,
                        station.longitude + kotlin.math.cos(rad) * radius
                    ))
                }

                routes.add(Route(
                    id = "r_${station.id}_internal",
                    sourceStationId = station.id,
                    destinationStationId = station.id,
                    distance = dist,
                    estimatedTimeMinutes = estimatedTime,
                    waypoints = waypoints
                ))
            }
        }

        return routes
    }

    private fun calculateDistance(s1: Station, s2: Station): Double {
        val r = 6371.0
        val lat1 = s1.latitude * kotlin.math.PI / 180.0
        val lat2 = s2.latitude * kotlin.math.PI / 180.0
        val dLat = (s2.latitude - s1.latitude) * kotlin.math.PI / 180.0
        val dLon = (s2.longitude - s1.longitude) * kotlin.math.PI / 180.0

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return r * c
    }
}
