package com.example.railway_booking_and_train_tracking_system

import com.example.railway.domain.model.*
import com.example.railway.domain.service.RoutePlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MultiSegmentTest {

    @Test
    fun `planner finds itinerary with transfers`() {
        // S1 -> S2 -> S3
        val r1 = Route(id="r1", sourceStationId="1", destinationStationId="2", distance=100.0, estimatedTimeMinutes=60)
        val r2 = Route(id="r2", sourceStationId="2", destinationStationId="3", distance=100.0, estimatedTimeMinutes=60)
        
        val planner = RoutePlanner(listOf(r1, r2))
        
        val itinerary = planner.findItinerary("1", "3", 1000000L)
        
        assertNotNull("Itinerary should be found", itinerary)
        assertEquals(2, itinerary?.segments?.size)
        assertEquals("r1", itinerary?.segments?.get(0)?.routeId)
        assertEquals("r2", itinerary?.segments?.get(1)?.routeId)
        
        // Price should be distance (200) * 15 cents = 3000
        assertEquals(3000L, itinerary?.totalBasePrice?.amountCents)
        
        // Carbon offset should be 200 * 0.5 cents = 100
        assertEquals(100L, itinerary?.carbonOffsetCents)
    }
}
