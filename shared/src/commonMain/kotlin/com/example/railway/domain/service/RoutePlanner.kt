package com.example.railway.domain.service

import com.example.railway.domain.model.*

enum class RouteCriteria {
    PRICE, DURATION, DEPARTURE
}

class RoutePlanner(
    private val routes: List<Route>
) {
    fun findShortestPath(
        sourceStationId: String, 
        destinationStationId: String,
        criteria: RouteCriteria = RouteCriteria.PRICE
    ): List<Route> {
        val distances = mutableMapOf<String, Double>().withDefault { Double.MAX_VALUE }
        val previousRoute = mutableMapOf<String, Route?>()
        val visited = mutableSetOf<String>()
        val queue = mutableListOf<Pair<String, Double>>()

        distances[sourceStationId] = 0.0
        queue.add(sourceStationId to 0.0)

        while (queue.isNotEmpty()) {
            val current = queue.minByOrNull { it.second } ?: break
            queue.remove(current)
            
            val currentStationId = current.first
            val currentDistance = current.second

            if (currentStationId == destinationStationId) break
            if (currentStationId in visited) continue
            visited.add(currentStationId)

            val neighborRoutes = routes.filter { it.sourceStationId == currentStationId }
            for (route in neighborRoutes) {
                val neighborId = route.destinationStationId
                
                val weight = when (criteria) {
                    RouteCriteria.PRICE -> route.distance * 0.15 
                    RouteCriteria.DURATION, RouteCriteria.DEPARTURE -> route.estimatedTimeMinutes.toDouble()
                }
                
                val newDistance = currentDistance + weight
                val existingDistance = distances[neighborId] ?: Double.MAX_VALUE
                
                if (newDistance < existingDistance) {
                    distances[neighborId] = newDistance
                    previousRoute[neighborId] = route
                    queue.add(neighborId to newDistance)
                }
            }
        }

        return reconstructPath(sourceStationId, destinationStationId, previousRoute)
    }

    // New Transfer-Aware Itinerary Generation (Part 2)
    fun findItinerary(
        sourceId: String,
        destId: String,
        departureTimeBase: Long
    ): Itinerary? {
        val routesFound = findShortestPath(sourceId, destId)
        if (routesFound.isEmpty()) return null
        
        var currentTime = departureTimeBase
        val segments = routesFound.map { route ->
            val dep = currentTime + (30 * 60000) // 30 min buffer for each segment
            val arr = dep + (route.estimatedTimeMinutes * 60000)
            currentTime = arr
            
            ItinerarySegment(
                routeId = route.id,
                trainId = "T-SIM", // Placeholder
                sourceStationId = route.sourceStationId,
                destinationStationId = route.destinationStationId,
                departureTimeMillis = dep,
                arrivalTimeMillis = arr
            )
        }
        
        val dist = routesFound.sumOf { it.distance }
        val priceCents = (dist * 15).toLong() 
        
        return Itinerary(
            segments = segments,
            totalDistance = dist,
            totalBasePrice = Money("USD", priceCents),
            carbonOffsetCents = (dist * 0.5).toLong() // 0.5 cents per km (Part 2)
        )
    }

    private fun reconstructPath(
        sourceId: String,
        targetId: String,
        previousRoute: Map<String, Route?>
    ): List<Route> {
        val path = mutableListOf<Route>()
        var currentId = targetId
        while (currentId != sourceId) {
            val route = previousRoute[currentId] ?: return emptyList()
            path.add(0, route)
            currentId = route.sourceStationId
        }
        return path
    }
}
