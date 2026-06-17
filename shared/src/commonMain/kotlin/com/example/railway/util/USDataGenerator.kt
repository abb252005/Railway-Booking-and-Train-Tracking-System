package com.example.railway.util

import com.example.railway.domain.model.*
import kotlin.random.Random

object USDataGenerator {
    fun generateStations(): List<Station> {
        val terminals = listOf("Terminal A", "Terminal B", "Platform 4", "Main Hall", "East Wing", "Gate 7")
        return StateBoundaries.allStates.mapIndexed { index, boundary ->
            val isIsolated = boundary.name == "Alaska" || boundary.name == "Hawaii"
            val info = if (isIsolated) {
                "Major isolated station in the state of ${boundary.name}. Serves as a key hub for regional/in-state transport."
            } else {
                "Major station in the state of ${boundary.name}. Serves as a key hub for regional transport."
            }
            Station(
                id = "s${index + 1}",
                name = boundary.name,
                latitude = boundary.centroid.first,
                longitude = boundary.centroid.second,
                designatedTrainId = "t${index + 1}",
                info = info,
                terminal = terminals.random()
            )
        }
    }

    fun generateTrains(routes: List<Route>): List<Train> {
        // baseTime represents 00:00 AM of the current simulated day
        val baseTime = 1718150400000L // 2024-06-12 00:00:00 UTC
        
        val trains = mutableListOf<Train>()

        StateBoundaries.allStates.forEachIndexed { index, boundary ->
            val trainId = "t${index + 1}"
            val schedule = mutableListOf<ScheduleEntry>()
            
            val sourceStationId = "s${index + 1}"
            
            // Alaska and Hawaii are isolated, but they still have a station and should have a train.
            // However, they won't have routes to other states.
            // Let's give them a mock internal route if we want them to "move", 
            // or just let them stay at the station if no routes exist.
            val possibleRoutes = routes.filter { it.sourceStationId == sourceStationId }
            val route = possibleRoutes.randomOrNull()
            
            if (route != null) {
                var departureTime = baseTime + (index * 25 * 60000L)
                var duration = route.estimatedTimeMinutes * 60000L
                
                // Special schedules for Alaska and Hawaii
                if (boundary.name == "Hawaii") {
                    // Hawaii Skyline: 4:00 AM to 10:30 PM, every 10-15 mins
                    // For the demo, let's pick a random time in that window
                    val startWindow = 4 * 60 * 60000L
                    val endWindow = 22 * 60 * 60000L + 30 * 60000L
                    departureTime = baseTime + Random.nextLong(startWindow, endWindow)
                    duration = Random.nextInt(10, 15) * 60000L
                } else if (boundary.name == "Alaska") {
                    // Alaska Railroad: Daytime, 2-12 hours
                    val startWindow = 6 * 60 * 60000L // 6 AM
                    val endWindow = 18 * 60 * 60000L // 6 PM
                    departureTime = baseTime + Random.nextLong(startWindow, endWindow)
                    duration = Random.nextInt(120, 720) * 60000L // 2 to 12 hours
                }

                val arrivalTime = departureTime + duration
                
                // On launch, we don't set isRunning here anymore, 
                // the ScheduleManager will handle the random 15-20.
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
                // Isolated station with no outgoing routes (Alaska/Hawaii)
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

        // Mainland connections
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
                
                routes.add(Route(
                    id = "r_${s1.id}_${s2.id}",
                    sourceStationId = s1.id,
                    destinationStationId = s2.id,
                    distance = dist,
                    estimatedTimeMinutes = estimatedTime
                ))
            }
        }

        // Alaska and Hawaii isolated routes (circular within their own state)
        listOf("Alaska", "Hawaii").forEach { stateName ->
            val station = stations.find { it.name == stateName }
            if (station != null) {
                val dist = 100.0 // Mock distance for internal route
                val estimatedTime = if (stateName == "Hawaii") {
                    Random.nextInt(10, 15) // Skyline frequency/duration
                } else {
                    Random.nextInt(120, 480) // Alaska Railroad daytime tours (2-8 hours)
                }

                // Generate Curvy Waypoints
                val waypoints = mutableListOf<Waypoint>()
                val radius = 0.5 // lat/lng offset
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
                    destinationStationId = station.id, // Circular/Internal loop
                    distance = dist,
                    estimatedTimeMinutes = estimatedTime,
                    waypoints = waypoints
                ))
            }
        }

        return routes
    }

    /**
     * Calculates the actual geographical distance between two stations using the Haversine formula.
     * Returns distance in kilometers.
     */
    private fun calculateDistance(s1: Station, s2: Station): Double {
        val r = 6371.0 // Earth's radius in kilometers
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
