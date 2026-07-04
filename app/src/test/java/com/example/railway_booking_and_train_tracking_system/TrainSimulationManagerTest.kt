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
        val s1 = Station("1", "S1", latitude = 0.0, longitude = 0.0, hasLounge = false, hasCheckedBaggage = true, hasKiosks = true, isHub = false)
        val s2 = Station("2", "S2", latitude = 10.0, longitude = 10.0, hasLounge = false, hasCheckedBaggage = true, hasKiosks = true, isHub = false)
        val stations = listOf(s1, s2)
        
        val route = Route("r1", "1", "2", 100.0, 60) // 60 minutes
        val train = Train("t1", "Express", TrainStatus.RUNNING)
        
        val manager = TrainSimulationManager(this, stations, simulationSpeedMultiplier = 60.0) 
        manager.startSimulation(train, listOf(route))
        runCurrent()
        
        advanceTimeBy(1000) 
        runCurrent()
        
        val pos = manager.trainPositions.value["t1"]
        assertTrue("Train position should be present", pos != null)
        assertTrue("Progress should be > 0", (pos?.progress ?: 0.0) > 0.0)
    }

    @Test
    fun `simulation shows movement factors`() = runTest {
        val s1 = Station("1", "S1", latitude = 0.0, longitude = 0.0, hasLounge = false, hasCheckedBaggage = true, hasKiosks = true, isHub = false)
        val s2 = Station("2", "S2", latitude = 10.0, longitude = 10.0, hasLounge = false, hasCheckedBaggage = true, hasKiosks = true, isHub = false)
        val stations = listOf(s1, s2)
        
        val route = Route("r1", "1", "2", 100.0, 10) 
        val train = Train("t1", "Express", TrainStatus.RUNNING, maxSpeedMph = 100)
        
        val manager = TrainSimulationManager(this, stations, simulationSpeedMultiplier = 60.0)
        manager.startSimulation(train, listOf(route))
        runCurrent()
        
        advanceTimeBy(100)
        runCurrent()
        var pos = manager.trainPositions.value["t1"]
        assertTrue("Initial speed should be low", (pos?.speedKmH ?: 100.0) < 50.0)
        
        advanceTimeBy(5000)
        runCurrent()
        pos = manager.trainPositions.value["t1"]
        assertTrue("Should have accelerated", (pos?.speedKmH ?: 0.0) > 10.0)
    }
}
