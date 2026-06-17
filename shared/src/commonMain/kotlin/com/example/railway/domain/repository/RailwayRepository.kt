package com.example.railway.domain.repository

import com.example.railway.domain.model.*
import com.example.railway.util.StateBoundary
import kotlinx.coroutines.flow.Flow

interface RailwayRepository {
    fun getAllStations(): Flow<List<Station>>
    suspend fun insertStation(station: Station)
    suspend fun deleteStation(id: String)

    fun getAllTrains(): Flow<List<Train>>
    suspend fun insertTrain(train: Train)
    suspend fun deleteTrain(id: String)

    fun getAllRoutes(): Flow<List<Route>>
    suspend fun insertRoute(route: Route)

    fun getAllBookings(): Flow<List<Booking>>
    suspend fun insertBooking(booking: Booking)

    fun getAllSchedules(): Flow<List<ScheduleEntry>>
    suspend fun insertSchedule(schedule: ScheduleEntry)
    suspend fun updateScheduleActive(id: String, isActive: Boolean)
    suspend fun deleteSchedule(id: String)

    fun getWalletBalance(userId: String): Flow<Double>
    suspend fun updateWalletBalance(userId: String, balance: Double)
    suspend fun getUserBookingCount(userId: String): Long

    fun getAllUsers(): Flow<List<User>>
    suspend fun insertUser(user: User)
    suspend fun findUserByUsername(username: String): User?

    // State Operations
    fun getAllStates(): Flow<List<StateBoundary>>
    suspend fun insertState(state: StateBoundary)
    suspend fun deleteAllStates()
}
