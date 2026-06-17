package com.example.railway.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.railway.db.RailwayDatabase
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.util.StateBoundary
import com.example.railway.util.ioDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RailwayRepositoryImpl(
    private val db: RailwayDatabase
) : RailwayRepository {

    private val queries = db.railwayDatabaseQueries

    override fun getAllStations(): Flow<List<Station>> {
        return queries.selectAllStations()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { Station(it.id, it.name, it.latitude, it.longitude, it.designatedTrainId, it.info, it.terminal) }
            }
    }

    override suspend fun insertStation(station: Station) {
        queries.insertStation(station.id, station.name, station.latitude, station.longitude, station.designatedTrainId, station.info, station.terminal)
    }

    override suspend fun deleteStation(id: String) {
        queries.deleteStation(id)
    }

    override fun getAllTrains(): Flow<List<Train>> {
        return queries.selectAllTrains()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { 
                    val carriages = (1..it.carriageCount.toInt()).map { i -> 
                        Carriage(id = "c_${it.id}_$i", number = i, capacity = 50)
                    }
                    Train(it.id, it.name, TrainStatus.valueOf(it.status), carriages) 
                }
            }
    }

    override suspend fun insertTrain(train: Train) {
        queries.insertTrain(train.id, train.name, train.totalSeats.toLong(), train.status.name, train.carriages.size.toLong())
    }

    override suspend fun deleteTrain(id: String) {
        queries.deleteTrain(id)
    }

    override fun getAllRoutes(): Flow<List<Route>> {
        return queries.selectAllRoutes()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { 
                    val waypoints = it.waypoints?.split(";")?.filter { s -> s.isNotEmpty() }?.map { s ->
                        val parts = s.split(",")
                        Waypoint(parts[0].toDouble(), parts[1].toDouble())
                    } ?: emptyList()

                    Route(it.id, it.sourceStationId, it.destinationStationId, it.distance, it.estimatedTime.toInt(), waypoints) 
                }
            }
    }

    override suspend fun insertRoute(route: Route) {
        val waypointsStr = route.waypoints.joinToString(";") { "${it.latitude},${it.longitude}" }
        queries.insertRoute(route.id, route.sourceStationId, route.destinationStationId, route.distance, route.estimatedTimeMinutes.toLong(), waypointsStr)
    }

    override fun getAllBookings(): Flow<List<Booking>> {
        return queries.selectAllBookings()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map {
                    Booking(
                        id = it.id,
                        userId = it.userId,
                        trainId = it.trainId,
                        passengerName = it.passengerName,
                        carriageId = it.carriageId,
                        seatNumber = it.seatNumber,
                        startStationId = it.startStationId,
                        endStationId = it.endStationId,
                        departureDate = it.departureDate,
                        departureTimeMillis = it.departureTimeMillis,
                        arrivalTimeMillis = it.arrivalTimeMillis,
                        paymentMethod = PaymentMethod.valueOf(it.paymentMethod),
                        price = it.price,
                        timestamp = it.timestamp
                    )
                }
            }
    }

    override suspend fun insertBooking(booking: Booking) {
        queries.insertBooking(
            booking.id,
            booking.userId,
            booking.trainId,
            booking.passengerName,
            booking.carriageId,
            booking.seatNumber,
            booking.startStationId,
            booking.endStationId,
            booking.departureDate,
            booking.departureTimeMillis,
            booking.arrivalTimeMillis,
            booking.paymentMethod.name,
            booking.price,
            booking.timestamp
        )
    }

    override fun getAllSchedules(): Flow<List<ScheduleEntry>> {
        return queries.selectAllSchedules()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map {
                    ScheduleEntry(
                        id = it.id,
                        trainId = it.trainId,
                        sourceStationId = it.sourceStationId,
                        destinationStationId = it.destinationStationId,
                        departureTimeMillis = it.departureTimeMillis,
                        arrivalTimeMillis = it.arrivalTimeMillis,
                        routeId = it.routeId,
                        isActive = it.isActive == 1L
                    )
                }
            }
    }

    override suspend fun insertSchedule(schedule: ScheduleEntry) {
        queries.insertSchedule(
            schedule.id,
            schedule.trainId,
            schedule.sourceStationId,
            schedule.destinationStationId,
            schedule.departureTimeMillis,
            schedule.arrivalTimeMillis,
            schedule.routeId,
            if (schedule.isActive) 1L else 0L
        )
    }

    override suspend fun updateScheduleActive(id: String, isActive: Boolean) {
        queries.updateScheduleActive(if (isActive) 1L else 0L, id)
    }

    override suspend fun deleteSchedule(id: String) {
        queries.deleteSchedule(id)
    }

    override fun getWalletBalance(userId: String): Flow<Double> {
        return queries.getWalletBalance(userId)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map { it ?: 0.0 }
    }

    override suspend fun updateWalletBalance(userId: String, balance: Double) {
        queries.updateWalletBalance(balance, userId)
    }

    override suspend fun getUserBookingCount(userId: String): Long {
        return queries.countUserBookings(userId).executeAsOne()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return queries.selectAllUsers()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { User(it.id, it.username, it.password, it.isAdmin == 1L) }
            }
    }

    override suspend fun insertUser(user: User) {
        queries.insertUser(user.id, user.username, user.password, if (user.isAdmin) 1L else 0L, 0.0)
    }

    override suspend fun findUserByUsername(username: String): User? {
        return queries.findUserByUsername(username).executeAsOneOrNull()?.let {
            User(it.id, it.username, it.password, it.isAdmin == 1L)
        }
    }

    override fun getAllStates(): Flow<List<StateBoundary>> {
        return queries.selectAllStates()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map {
                    val points = it.points.split(";").filter { s -> s.isNotEmpty() }.map { s ->
                        val parts = s.split(",")
                        parts[0].toDouble() to parts[1].toDouble()
                    }
                    StateBoundary(it.name, points, it.centroidLat to it.centroidLng)
                }
            }
    }

    override suspend fun insertState(state: StateBoundary) {
        val pointsStr = state.points.joinToString(";") { "${it.first},${it.second}" }
        queries.insertState(state.name, pointsStr, state.centroid.first, state.centroid.second)
    }

    override suspend fun deleteAllStates() {
        queries.deleteAllStates()
    }
}
