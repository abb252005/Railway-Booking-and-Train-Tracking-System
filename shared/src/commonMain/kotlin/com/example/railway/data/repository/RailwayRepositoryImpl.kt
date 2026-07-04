package com.example.railway.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.railway.db.*
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.RailwayRepository
import com.example.railway.util.StateBoundary
import com.example.railway.util.ioDispatcher
import com.example.railway.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RailwayRepositoryImpl(
    db: RailwayDatabase
) : RailwayRepository {

    private val queries = db.railwayDatabaseQueries

    override fun getAllStations(): Flow<List<Station>> {
        return queries.selectAllStations()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { Station(
                    id = it.id, 
                    name = it.name, 
                    code = it.code, 
                    city = it.city, 
                    state = it.state, 
                    timezone = it.timezone, 
                    latitude = it.latitude, 
                    longitude = it.longitude, 
                    designatedTrainId = it.designatedTrainId, 
                    info = it.info, 
                    terminal = it.terminal,
                    hasLounge = it.hasLounge == 1L,
                    hasCheckedBaggage = it.hasCheckedBaggage == 1L,
                    hasKiosks = it.hasKiosks == 1L,
                    isHub = it.isHub == 1L
                ) }
            }
    }

    override suspend fun insertStation(station: Station) {
        queries.insertStation(
            station.id, 
            station.name, 
            station.code, 
            station.city, 
            station.state, 
            station.timezone, 
            station.latitude, 
            station.longitude, 
            station.designatedTrainId, 
            station.info, 
            station.terminal,
            if (station.hasLounge) 1L else 0L,
            if (station.hasCheckedBaggage) 1L else 0L,
            if (station.hasKiosks) 1L else 0L,
            if (station.isHub) 1L else 0L
        )
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
                    Train(
                        id = it.id, 
                        name = it.name, 
                        status = TrainStatus.valueOf(it.status), 
                        maxSpeedMph = it.max_speed_mph.toInt(),
                        operatorCode = it.operator_code,
                        carriages = carriages
                    ) 
                }
            }
    }

    override suspend fun insertTrain(train: Train) {
        queries.insertTrain(train.id, train.name, train.totalSeats.toLong(), train.status.name, train.maxSpeedMph.toLong(), train.operatorCode, train.carriages.size.toLong())
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

                    val slowOrders = try {
                        it.slowOrders?.let { s -> Json.decodeFromString<List<SlowOrder>>(s) } ?: emptyList()
                    } catch (_: Exception) { emptyList() }

                    val elevationProfile = try {
                        it.elevationProfile?.let { s -> Json.decodeFromString<List<ElevationPoint>>(s) } ?: emptyList()
                    } catch (_: Exception) { emptyList() }

                    val trackClass = try {
                        TrackClass.valueOf(it.trackClass)
                    } catch (_: Exception) { TrackClass.CLASS_4 }

                    Route(it.id, it.sourceStationId, it.destinationStationId, it.distance, it.estimatedTime.toInt(), waypoints, slowOrders, elevationProfile, trackClass) 
                }
            }
    }

    override suspend fun insertRoute(route: Route) {
        val waypointsStr = route.waypoints.joinToString(";") { "${it.latitude},${it.longitude}" }
        val slowOrdersStr = Json.encodeToString(route.slowOrders)
        val elevationProfileStr = Json.encodeToString(route.elevationProfile)
        queries.insertRoute(route.id, route.sourceStationId, route.destinationStationId, route.distance, route.estimatedTimeMinutes.toLong(), waypointsStr, slowOrdersStr, elevationProfileStr, route.trackClass.name)
    }

    override fun getAllBookings(): Flow<List<Booking>> {
        return queries.selectAllBookings()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map {
                    Booking(
                        id = it.id,
                        reservationId = it.reservationId,
                        userId = it.userId,
                        trainId = it.trainId,
                        publicTrainNumber = it.publicTrainNumber,
                        serviceName = it.serviceName,
                        passengerName = it.passengerName,
                        passengerType = it.passengerType,
                        carriageId = it.carriageId,
                        seatNumber = it.seatNumber,
                        fareClass = FareClass.valueOf(it.fareClass),
                        fareProductName = it.fareProductName,
                        startStationId = it.startStationId,
                        endStationId = it.endStationId,
                        startStationCode = it.startStationCode,
                        endStationCode = it.endStationCode,
                        departureDate = it.departureDate,
                        serviceDate = it.serviceDate,
                        departureTimeMillis = it.departureTimeMillis,
                        arrivalTimeMillis = it.arrivalTimeMillis,
                        timezone = it.timezone,
                        paymentMethod = PaymentMethod.valueOf(it.paymentMethod),
                        paymentStatus = it.paymentStatus,
                        price = it.price,
                        totalPrice = Money(
                            currency = it.currency, 
                            amountCents = it.totalAmountCents,
                            baseAmountCents = it.baseAmountCents,
                            taxAmountCents = it.taxAmountCents
                        ),
                        timestamp = it.timestamp,
                        validityStartsAt = it.validityStartsAt,
                        validityEndsAt = it.validityEndsAt,
                        barcodePayload = it.barcodePayload,
                        status = TicketStatus.valueOf(it.status)
                    )
                }
            }
    }

    override suspend fun insertBooking(booking: Booking) {
        queries.insertBooking(
            booking.id,
            booking.reservationId,
            booking.userId,
            booking.trainId,
            booking.publicTrainNumber,
            booking.serviceName,
            booking.passengerName,
            booking.passengerType,
            booking.carriageId,
            booking.seatNumber,
            booking.fareClass.name,
            booking.fareProductName,
            booking.startStationId,
            booking.endStationId,
            booking.startStationCode,
            booking.endStationCode,
            booking.departureDate,
            booking.serviceDate,
            booking.departureTimeMillis,
            booking.arrivalTimeMillis,
            booking.timezone,
            booking.paymentMethod.name,
            booking.paymentStatus,
            booking.price,
            booking.totalPrice.amountCents,
            booking.totalPrice.baseAmountCents,
            booking.totalPrice.taxAmountCents,
            booking.totalPrice.currency,
            booking.timestamp,
            booking.validityStartsAt,
            booking.validityEndsAt,
            booking.barcodePayload,
            booking.status.name,
            1L // is Reserved = true
        )
    }

    override suspend fun getReservedSeats(trainId: String, departureDate: Long): List<Pair<String, String>> {
        return queries.findReservedSeats(trainId, departureDate).executeAsList().map { it.carriageId to it.seatNumber }
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
                    StateBoundary(it.name, it.abbreviation, it.population, points, it.centroidLat to it.centroidLng)
                }
            }
    }

    override suspend fun insertState(state: StateBoundary) {
        val pointsStr = state.points.joinToString(";") { "${it.first},${it.second}" }
        queries.insertState(state.name, state.abbreviation, state.population, pointsStr, state.centroid.first, state.centroid.second)
    }

    override suspend fun deleteAllStates() {
        queries.deleteAllStates()
    }

    // Chat
    override fun observeSessions(userId: String): Flow<List<ChatSessionEntity>> {
        return queries.selectAllSessions(userId).asFlow().mapToList(ioDispatcher)
    }

    override fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return queries.selectMessagesBySession(sessionId).asFlow().mapToList(ioDispatcher)
    }

    override suspend fun createSession(userId: String, title: String): Long {
        queries.insertSession(userId, title, currentTimeMillis())
        return queries.lastInsertedRowId().executeAsOne()
    }

    override suspend fun updateSessionTitle(sessionId: Long, title: String) {
        queries.updateSessionTitle(title, currentTimeMillis(), sessionId)
    }

    override suspend fun deleteSession(sessionId: Long) {
        queries.deleteMessagesBySession(sessionId)
        queries.deleteSession(sessionId)
    }

    override suspend fun saveChatMessage(sessionId: Long, userId: String, role: String, text: String) {
        queries.insertMessage(sessionId, userId, role, text, currentTimeMillis())
    }

    override suspend fun clearChatHistory(userId: String) {
        queries.clearAllChatHistory(userId)
    }

    override suspend fun getLanguage(): String {
        return try {
            queries.getAppConfig("current_language").executeAsOneOrNull() ?: "English"
        } catch (_: Exception) {
            "English"
        }
    }

    override suspend fun setLanguage(language: String) {
        try {
            queries.setAppConfig("current_language", language)
        } catch (_: Exception) {
            // Log or ignore
        }
    }
}
