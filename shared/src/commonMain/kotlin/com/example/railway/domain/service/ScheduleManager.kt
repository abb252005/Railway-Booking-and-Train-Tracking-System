package com.example.railway.domain.service

import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.minutes

class ScheduleManager(
    private val scope: CoroutineScope,
    private val repository: RailwayRepository
) {
    private val _activeSchedules = MutableStateFlow<List<ScheduleEntry>>(emptyList())
    val activeSchedules: StateFlow<List<ScheduleEntry>> = _activeSchedules.asStateFlow()

    init {
        scope.launch {
            repository.getAllSchedules().collect { schedules ->
                val active = schedules.filter { it.isActive }
                _activeSchedules.value = active
            }
        }
    }

    fun startRotationCycle() {
        scope.launch {
            while (isActive) {
                // Rotate every 5 minutes for more dynamic simulation
                delay(5.minutes)
                rotateSchedules()
            }
        }
    }

    private suspend fun rotateSchedules() {
        val allSchedules = repository.getAllSchedules().first()
        val currentlyActive = allSchedules.filter { it.isActive }
        val currentlyInactive = allSchedules.filter { !it.isActive }

        // Ensure we always have at least 15 active trains if possible
        if (currentlyActive.size < 15 && currentlyInactive.isNotEmpty()) {
            val toActivate = currentlyInactive.shuffled().take(min(15 - currentlyActive.size, currentlyInactive.size))
            toActivate.forEach { repository.updateScheduleActive(it.id, true) }
            return
        }

        if (currentlyActive.size < 4 || currentlyInactive.size < 4) return

        // Select 4 to disappear
        val toDisappear = currentlyActive.shuffled().take(4)
        // Select 4 to appear
        val toAppear = currentlyInactive.shuffled().take(4)

        toDisappear.forEach {
            repository.updateScheduleActive(it.id, false)
        }
        toAppear.forEach {
            repository.updateScheduleActive(it.id, true)
        }
        
        println("ROTATION: 4 trains reached destination, 4 new trains departed.")
    }

    suspend fun initializeSchedules(trains: List<Train>) {
        val existing = repository.getAllSchedules().first()
        if (existing.isNotEmpty()) {
            // If already initialized but low count, boost it
            if (existing.count { it.isActive } < 15) {
                val inactive = existing.filter { !it.isActive }
                val toActivate = inactive.shuffled().take(15 - existing.count { it.isActive })
                toActivate.forEach { repository.updateScheduleActive(it.id, true) }
            }
            return
        }

        val schedules = mutableListOf<ScheduleEntry>()

        trains.forEach { train ->
            // Use all schedule entries from the train model if any, otherwise skip
            train.schedule.forEach { schedules.add(it) }
        }

        // Randomly activate 18-22 on launch for a busy map
        val activeCount = (18..22).random()
        val activeIndices = schedules.indices.shuffled().take(activeCount).toSet()
        
        schedules.forEachIndexed { index, schedule ->
            val finalSchedule = if (activeIndices.contains(index)) schedule.copy(isActive = true) else schedule
            repository.insertSchedule(finalSchedule)
        }
    }
    
    private fun min(a: Int, b: Int): Int = if (a < b) a else b
}
