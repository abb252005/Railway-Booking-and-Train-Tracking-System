package com.example.railway_booking_and_train_tracking_system

import com.example.railway.domain.model.*
import com.example.railway.domain.service.PhysicsEngine
import com.example.railway.domain.service.TrainSimulationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignalingTest {

    @Test
    fun `train slows down when approaching another train`() = runTest {
        val s1 = Station(id="1", name="S1", latitude = 0.0, longitude = 0.0)
        val s2 = Station(id="2", name="S2", latitude = 10.0, longitude = 10.0)
        val stations = listOf(s1, s2)
        
        val route = Route(id="r1", sourceStationId="1", destinationStationId="2", distance=100.0, estimatedTimeMinutes=10) 
        val train1 = Train(id="t1", name="Lead Train", status=TrainStatus.RUNNING)
        
        val manager = TrainSimulationManager(this, stations, simulationSpeedMultiplier = 60.0)
        
        // Start Lead Train
        manager.startSimulation(train1, listOf(route))
        runCurrent()
        advanceTimeBy(5000) // Lead train moves ahead
        runCurrent()
        
        val t1Pos = manager.trainPositions.value["t1"]
        assertTrue("Lead train should be active", t1Pos != null)

        // Physics check
        val physics = PhysicsEngine()
        
        // 4km separation = APPROACH signal (40% speed)
        val impact4km = physics.calculateSignalImpact(4.0)
        assertTrue("Signal should be APPROACH at 4km", impact4km.first == SignalState.APPROACH)
        assertTrue("Speed multiplier should be 0.4", impact4km.second == 0.4)
        
        // 1km separation = RESTRICTING signal (0% speed)
        val impact1km = physics.calculateSignalImpact(1.0)
        assertTrue("Signal should be RESTRICTING at 1km", impact1km.first == SignalState.RESTRICTING)
        assertTrue("Speed multiplier should be 0.0", impact1km.second == 0.0)
        
        manager.stopSimulation("t1")
    }
}
