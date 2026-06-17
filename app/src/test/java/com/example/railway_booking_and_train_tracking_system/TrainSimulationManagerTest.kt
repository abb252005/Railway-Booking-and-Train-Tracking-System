package com.example.railway_booking_and_train_tracking_system

import com.example.railway.domain.model.*
import com.example.railway.domain.service.TrainSimulationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainSimulationManagerTest {

    @Test
    fun `simulation updates positions and ETA correctly`() = runTest {
        val s1 = Station("1", "S1", 0.0, 0.0)
        val s2 = Station("2", "S2", 10.0, 10.0)
        val stations = listOf(s1, s2)
        
        val route = Route("r1", "1", "2", 100.0, 60) // 60 minutes
        val train = Train("t1", "Express", TrainStatus.RUNNING)
        
        // Use a very high speed multiplier for testing to avoid long delays
        val manager = TrainSimulationManager(this, stations, simulationSpeedMultiplier = 3600.0) // 1 hour simulation in 1 second
        
        manager.startSimulation(train, listOf(route))
        
        // Wait a bit for the simulation to start and make some progress
        advanceTimeBy(500) // Halfway through the 1-second simulation
        
        val positions = manager.trainPositions.value
        val pos = positions["t1"]
        
        assertTrue("Train position should be present", pos != null)
        pos?.let {
            assertTrue("Progress should be between 0 and 1", it.progress > 0.0 && it.progress < 1.0)
            assertTrue("ETA should be less than 60", it.estimatedTimeRemainingMinutes < 60.0)
            println("Progress: ${it.progress}, ETA: ${it.estimatedTimeRemainingMinutes}")
        }

        advanceTimeBy(600) // Finish the simulation
        val finalPos = manager.trainPositions.value["t1"]
        assertEquals(1.0, finalPos?.progress ?: 0.0, 0.1)
        assertEquals(0.0, finalPos?.estimatedTimeRemainingMinutes ?: -1.0, 0.1)
    }

    @Test
    fun `simulation handles multiple segments`() = runTest {
        val s1 = Station("1", "S1", 0.0, 0.0)
        val s2 = Station("2", "S2", 1.0, 1.0)
        val s3 = Station("3", "S3", 2.0, 2.0)
        val stations = listOf(s1, s2, s3)

        val r1 = Route("r1", "1", "2", 10.0, 30)
        val r2 = Route("r2", "2", "3", 10.0, 30)
        val train = Train("t1", "Express", TrainStatus.RUNNING)

        val manager = TrainSimulationManager(this, stations, simulationSpeedMultiplier = 3600.0)
        manager.startSimulation(train, listOf(r1, r2))

        // Check initial ETA
        advanceTimeBy(10)
        val initialEta = manager.trainPositions.value["t1"]?.estimatedTimeRemainingMinutes ?: 0.0
        assertTrue("Initial ETA should be around 60 minutes, got $initialEta", initialEta > 59.0 && initialEta <= 60.0)

        // Advance past first segment
        advanceTimeBy(600) // First segment takes 500ms at 3600x speed. 600ms = 36 minutes elapsed.
        val midEta = manager.trainPositions.value["t1"]?.estimatedTimeRemainingMinutes ?: 0.0
        assertTrue("Mid ETA should be around 24 minutes, got $midEta", midEta > 20.0 && midEta < 28.0)
        assertEquals("r2", manager.trainPositions.value["t1"]?.currentRouteId)
    }
}
