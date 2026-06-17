package com.example.railway_booking_and_train_tracking_system

import com.example.railway.domain.model.Route
import com.example.railway.domain.model.Station
import com.example.railway.domain.service.RoutePlanner
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutePlannerTest {

    @Test
    fun `findShortestPath returns correct path`() {
        val s1 = Station("1", "Station 1", 0.0, 0.0)
        val s2 = Station("2", "Station 2", 1.0, 1.0)
        val s3 = Station("3", "Station 3", 2.0, 2.0)

        val r1 = Route("r1", "1", "2", 10.0, 10)
        val r2 = Route("r2", "2", "3", 10.0, 10)
        val r3 = Route("r3", "1", "3", 25.0, 20)

        val planner = RoutePlanner(listOf(r1, r2, r3))
        
        val path = planner.findShortestPath("1", "3")
        
        assertEquals(2, path.size)
        assertEquals("r1", path[0].id)
        assertEquals("r2", path[1].id)
    }

    @Test
    fun `findShortestPath returns correct path based on criteria`() {
        val r1 = Route("r1", "1", "2", 10.0, 30)
        val r2 = Route("r2", "2", "3", 10.0, 30)
        val r3 = Route("r3", "1", "3", 25.0, 20)

        val planner = RoutePlanner(listOf(r1, r2, r3))
        
        // By price (linked to distance), r1+r2 (20.0 * 0.15) is cheaper than r3 (25.0 * 0.15)
        val pathByPrice = planner.findShortestPath("1", "3", com.example.railway.domain.service.RouteCriteria.PRICE)
        assertEquals(2, pathByPrice.size)
        assertEquals("r1", pathByPrice[0].id)

        // By duration, r3 (20) is shorter than r1+r2 (60)
        val pathByDuration = planner.findShortestPath("1", "3", com.example.railway.domain.service.RouteCriteria.DURATION)
        assertEquals(1, pathByDuration.size)
        assertEquals("r3", pathByDuration[0].id)
    }
}
