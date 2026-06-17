package com.example.railway.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class RailwayDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllUsers(mapper: (
    id: String,
    username: String,
    password: String,
    isAdmin: Long,
    balance: Double,
  ) -> T): Query<T> = Query(1_846_860_367, arrayOf("UserEntity"), driver, "RailwayDatabase.sq",
      "selectAllUsers",
      "SELECT UserEntity.id, UserEntity.username, UserEntity.password, UserEntity.isAdmin, UserEntity.balance FROM UserEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!
    )
  }

  public fun selectAllUsers(): Query<UserEntity> = selectAllUsers(::UserEntity)

  public fun <T : Any> findUserById(id: String, mapper: (
    id: String,
    username: String,
    password: String,
    isAdmin: Long,
    balance: Double,
  ) -> T): Query<T> = FindUserByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!
    )
  }

  public fun findUserById(id: String): Query<UserEntity> = findUserById(id, ::UserEntity)

  public fun <T : Any> findUserByUsername(username: String, mapper: (
    id: String,
    username: String,
    password: String,
    isAdmin: Long,
    balance: Double,
  ) -> T): Query<T> = FindUserByUsernameQuery(username) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!
    )
  }

  public fun findUserByUsername(username: String): Query<UserEntity> = findUserByUsername(username,
      ::UserEntity)

  public fun getWalletBalance(id: String): Query<Double> = GetWalletBalanceQuery(id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun <T : Any> selectAllStations(mapper: (
    id: String,
    name: String,
    latitude: Double,
    longitude: Double,
    designatedTrainId: String?,
    info: String,
    terminal: String,
  ) -> T): Query<T> = Query(-1_424_842_728, arrayOf("StationEntity"), driver, "RailwayDatabase.sq",
      "selectAllStations",
      "SELECT StationEntity.id, StationEntity.name, StationEntity.latitude, StationEntity.longitude, StationEntity.designatedTrainId, StationEntity.info, StationEntity.terminal FROM StationEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!,
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getString(6)!!
    )
  }

  public fun selectAllStations(): Query<StationEntity> = selectAllStations(::StationEntity)

  public fun <T : Any> findStationById(id: String, mapper: (
    id: String,
    name: String,
    latitude: Double,
    longitude: Double,
    designatedTrainId: String?,
    info: String,
    terminal: String,
  ) -> T): Query<T> = FindStationByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!,
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getString(6)!!
    )
  }

  public fun findStationById(id: String): Query<StationEntity> = findStationById(id,
      ::StationEntity)

  public fun <T : Any> selectAllTrains(mapper: (
    id: String,
    name: String,
    totalSeats: Long,
    status: String,
    carriageCount: Long,
  ) -> T): Query<T> = Query(1_388_416_004, arrayOf("TrainEntity"), driver, "RailwayDatabase.sq",
      "selectAllTrains",
      "SELECT TrainEntity.id, TrainEntity.name, TrainEntity.totalSeats, TrainEntity.status, TrainEntity.carriageCount FROM TrainEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectAllTrains(): Query<TrainEntity> = selectAllTrains(::TrainEntity)

  public fun <T : Any> findTrainById(id: String, mapper: (
    id: String,
    name: String,
    totalSeats: Long,
    status: String,
    carriageCount: Long,
  ) -> T): Query<T> = FindTrainByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun findTrainById(id: String): Query<TrainEntity> = findTrainById(id, ::TrainEntity)

  public fun <T : Any> selectAllRoutes(mapper: (
    id: String,
    sourceStationId: String,
    destinationStationId: String,
    distance: Double,
    estimatedTime: Long,
    waypoints: String?,
  ) -> T): Query<T> = Query(1_328_993_251, arrayOf("RouteEntity"), driver, "RailwayDatabase.sq",
      "selectAllRoutes",
      "SELECT RouteEntity.id, RouteEntity.sourceStationId, RouteEntity.destinationStationId, RouteEntity.distance, RouteEntity.estimatedTime, RouteEntity.waypoints FROM RouteEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)
    )
  }

  public fun selectAllRoutes(): Query<RouteEntity> = selectAllRoutes(::RouteEntity)

  public fun <T : Any> findRouteById(id: String, mapper: (
    id: String,
    sourceStationId: String,
    destinationStationId: String,
    distance: Double,
    estimatedTime: Long,
    waypoints: String?,
  ) -> T): Query<T> = FindRouteByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)
    )
  }

  public fun findRouteById(id: String): Query<RouteEntity> = findRouteById(id, ::RouteEntity)

  public fun <T : Any> findRoutesByStations(
    sourceStationId: String,
    destinationStationId: String,
    mapper: (
      id: String,
      sourceStationId: String,
      destinationStationId: String,
      distance: Double,
      estimatedTime: Long,
      waypoints: String?,
    ) -> T,
  ): Query<T> = FindRoutesByStationsQuery(sourceStationId, destinationStationId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)
    )
  }

  public fun findRoutesByStations(sourceStationId: String, destinationStationId: String):
      Query<RouteEntity> = findRoutesByStations(sourceStationId, destinationStationId,
      ::RouteEntity)

  public fun <T : Any> selectAllBookings(mapper: (
    id: String,
    userId: String,
    trainId: String,
    passengerName: String,
    carriageId: String,
    seatNumber: String,
    startStationId: String,
    endStationId: String,
    departureDate: Long,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    paymentMethod: String,
    price: String,
    timestamp: Long,
  ) -> T): Query<T> = Query(-737_903_213, arrayOf("BookingEntity"), driver, "RailwayDatabase.sq",
      "selectAllBookings",
      "SELECT BookingEntity.id, BookingEntity.userId, BookingEntity.trainId, BookingEntity.passengerName, BookingEntity.carriageId, BookingEntity.seatNumber, BookingEntity.startStationId, BookingEntity.endStationId, BookingEntity.departureDate, BookingEntity.departureTimeMillis, BookingEntity.arrivalTimeMillis, BookingEntity.paymentMethod, BookingEntity.price, BookingEntity.timestamp FROM BookingEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getLong(13)!!
    )
  }

  public fun selectAllBookings(): Query<BookingEntity> = selectAllBookings(::BookingEntity)

  public fun <T : Any> findBookingById(id: String, mapper: (
    id: String,
    userId: String,
    trainId: String,
    passengerName: String,
    carriageId: String,
    seatNumber: String,
    startStationId: String,
    endStationId: String,
    departureDate: Long,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    paymentMethod: String,
    price: String,
    timestamp: Long,
  ) -> T): Query<T> = FindBookingByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getLong(13)!!
    )
  }

  public fun findBookingById(id: String): Query<BookingEntity> = findBookingById(id,
      ::BookingEntity)

  public fun <T : Any> findBookingsByUser(userId: String, mapper: (
    id: String,
    userId: String,
    trainId: String,
    passengerName: String,
    carriageId: String,
    seatNumber: String,
    startStationId: String,
    endStationId: String,
    departureDate: Long,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    paymentMethod: String,
    price: String,
    timestamp: Long,
  ) -> T): Query<T> = FindBookingsByUserQuery(userId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getString(11)!!,
      cursor.getString(12)!!,
      cursor.getLong(13)!!
    )
  }

  public fun findBookingsByUser(userId: String): Query<BookingEntity> = findBookingsByUser(userId,
      ::BookingEntity)

  public fun countUserBookings(userId: String): Query<Long> = CountUserBookingsQuery(userId) {
      cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectAllSchedules(mapper: (
    id: String,
    trainId: String,
    sourceStationId: String,
    destinationStationId: String,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    routeId: String,
    isActive: Long,
  ) -> T): Query<T> = Query(700_223_875, arrayOf("ScheduleEntity"), driver, "RailwayDatabase.sq",
      "selectAllSchedules",
      "SELECT ScheduleEntity.id, ScheduleEntity.trainId, ScheduleEntity.sourceStationId, ScheduleEntity.destinationStationId, ScheduleEntity.departureTimeMillis, ScheduleEntity.arrivalTimeMillis, ScheduleEntity.routeId, ScheduleEntity.isActive FROM ScheduleEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun selectAllSchedules(): Query<ScheduleEntity> = selectAllSchedules(::ScheduleEntity)

  public fun <T : Any> findScheduleById(id: String, mapper: (
    id: String,
    trainId: String,
    sourceStationId: String,
    destinationStationId: String,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    routeId: String,
    isActive: Long,
  ) -> T): Query<T> = FindScheduleByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun findScheduleById(id: String): Query<ScheduleEntity> = findScheduleById(id,
      ::ScheduleEntity)

  public fun <T : Any> findSchedulesByTrain(trainId: String, mapper: (
    id: String,
    trainId: String,
    sourceStationId: String,
    destinationStationId: String,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    routeId: String,
    isActive: Long,
  ) -> T): Query<T> = FindSchedulesByTrainQuery(trainId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun findSchedulesByTrain(trainId: String): Query<ScheduleEntity> =
      findSchedulesByTrain(trainId, ::ScheduleEntity)

  public fun <T : Any> findActiveSchedules(mapper: (
    id: String,
    trainId: String,
    sourceStationId: String,
    destinationStationId: String,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    routeId: String,
    isActive: Long,
  ) -> T): Query<T> = Query(1_044_853_905, arrayOf("ScheduleEntity"), driver, "RailwayDatabase.sq",
      "findActiveSchedules",
      "SELECT ScheduleEntity.id, ScheduleEntity.trainId, ScheduleEntity.sourceStationId, ScheduleEntity.destinationStationId, ScheduleEntity.departureTimeMillis, ScheduleEntity.arrivalTimeMillis, ScheduleEntity.routeId, ScheduleEntity.isActive FROM ScheduleEntity WHERE isActive = 1") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun findActiveSchedules(): Query<ScheduleEntity> = findActiveSchedules(::ScheduleEntity)

  public fun <T : Any> findSchedulesForStation(
    sourceStationId: String,
    destinationStationId: String,
    mapper: (
      id: String,
      trainId: String,
      sourceStationId: String,
      destinationStationId: String,
      departureTimeMillis: Long,
      arrivalTimeMillis: Long,
      routeId: String,
      isActive: Long,
    ) -> T,
  ): Query<T> = FindSchedulesForStationQuery(sourceStationId, destinationStationId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun findSchedulesForStation(sourceStationId: String, destinationStationId: String):
      Query<ScheduleEntity> = findSchedulesForStation(sourceStationId, destinationStationId,
      ::ScheduleEntity)

  public fun <T : Any> selectAllStates(mapper: (
    name: String,
    points: String,
    centroidLat: Double,
    centroidLng: Double,
  ) -> T): Query<T> = Query(1_361_644_187, arrayOf("StateEntity"), driver, "RailwayDatabase.sq",
      "selectAllStates",
      "SELECT StateEntity.name, StateEntity.points, StateEntity.centroidLat, StateEntity.centroidLng FROM StateEntity") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!
    )
  }

  public fun selectAllStates(): Query<StateEntity> = selectAllStates(::StateEntity)

  /**
   * @return The number of rows updated.
   */
  public fun insertUser(
    id: String,
    username: String,
    password: String,
    isAdmin: Long,
    balance: Double,
  ): QueryResult<Long> {
    val result = driver.execute(-588_701_584, """
        |INSERT OR REPLACE INTO UserEntity(id, username, password, isAdmin, balance)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, username)
          bindString(parameterIndex++, password)
          bindLong(parameterIndex++, isAdmin)
          bindDouble(parameterIndex++, balance)
        }
    notifyQueries(-588_701_584) { emit ->
      emit("UserEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteUser(id: String): QueryResult<Long> {
    val result = driver.execute(209_721_954, """DELETE FROM UserEntity WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(209_721_954) { emit ->
      emit("BookingEntity")
      emit("UserEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateWalletBalance(balance: Double, id: String): QueryResult<Long> {
    val result = driver.execute(-1_758_260_850,
        """UPDATE UserEntity SET balance = ? WHERE id = ?""", 2) {
          var parameterIndex = 0
          bindDouble(parameterIndex++, balance)
          bindString(parameterIndex++, id)
        }
    notifyQueries(-1_758_260_850) { emit ->
      emit("UserEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertStation(
    id: String,
    name: String,
    latitude: Double,
    longitude: Double,
    designatedTrainId: String?,
    info: String,
    terminal: String,
  ): QueryResult<Long> {
    val result = driver.execute(887_639_663, """
        |INSERT OR REPLACE INTO StationEntity(id, name, latitude, longitude, designatedTrainId, info, terminal)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, name)
          bindDouble(parameterIndex++, latitude)
          bindDouble(parameterIndex++, longitude)
          bindString(parameterIndex++, designatedTrainId)
          bindString(parameterIndex++, info)
          bindString(parameterIndex++, terminal)
        }
    notifyQueries(887_639_663) { emit ->
      emit("StationEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteStation(id: String): QueryResult<Long> {
    val result = driver.execute(1_194_374_973, """DELETE FROM StationEntity WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(1_194_374_973) { emit ->
      emit("BookingEntity")
      emit("RouteEntity")
      emit("ScheduleEntity")
      emit("StationEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertTrain(
    id: String,
    name: String,
    totalSeats: Long,
    status: String,
    carriageCount: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-1_070_837_245, """
        |INSERT OR REPLACE INTO TrainEntity(id, name, totalSeats, status, carriageCount)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, name)
          bindLong(parameterIndex++, totalSeats)
          bindString(parameterIndex++, status)
          bindLong(parameterIndex++, carriageCount)
        }
    notifyQueries(-1_070_837_245) { emit ->
      emit("TrainEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteTrain(id: String): QueryResult<Long> {
    val result = driver.execute(-2_089_511_343, """DELETE FROM TrainEntity WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(-2_089_511_343) { emit ->
      emit("BookingEntity")
      emit("ScheduleEntity")
      emit("TrainEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateTrainStatus(status: String, id: String): QueryResult<Long> {
    val result = driver.execute(175_202_245, """UPDATE TrainEntity SET status = ? WHERE id = ?""",
        2) {
          var parameterIndex = 0
          bindString(parameterIndex++, status)
          bindString(parameterIndex++, id)
        }
    notifyQueries(175_202_245) { emit ->
      emit("TrainEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertRoute(
    id: String,
    sourceStationId: String,
    destinationStationId: String,
    distance: Double,
    estimatedTime: Long,
    waypoints: String?,
  ): QueryResult<Long> {
    val result = driver.execute(-1_072_754_108, """
        |INSERT OR REPLACE INTO RouteEntity(id, sourceStationId, destinationStationId, distance, estimatedTime, waypoints)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, sourceStationId)
          bindString(parameterIndex++, destinationStationId)
          bindDouble(parameterIndex++, distance)
          bindLong(parameterIndex++, estimatedTime)
          bindString(parameterIndex++, waypoints)
        }
    notifyQueries(-1_072_754_108) { emit ->
      emit("RouteEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteRoute(id: String): QueryResult<Long> {
    val result = driver.execute(-2_091_428_206, """DELETE FROM RouteEntity WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(-2_091_428_206) { emit ->
      emit("RouteEntity")
      emit("ScheduleEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertBooking(
    id: String,
    userId: String,
    trainId: String,
    passengerName: String,
    carriageId: String,
    seatNumber: String,
    startStationId: String,
    endStationId: String,
    departureDate: Long,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    paymentMethod: String,
    price: String,
    timestamp: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-1_445_505_644, """
        |INSERT OR REPLACE INTO BookingEntity(id, userId, trainId, passengerName, carriageId, seatNumber, startStationId, endStationId, departureDate, departureTimeMillis, arrivalTimeMillis, paymentMethod, price, timestamp)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 14) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, userId)
          bindString(parameterIndex++, trainId)
          bindString(parameterIndex++, passengerName)
          bindString(parameterIndex++, carriageId)
          bindString(parameterIndex++, seatNumber)
          bindString(parameterIndex++, startStationId)
          bindString(parameterIndex++, endStationId)
          bindLong(parameterIndex++, departureDate)
          bindLong(parameterIndex++, departureTimeMillis)
          bindLong(parameterIndex++, arrivalTimeMillis)
          bindString(parameterIndex++, paymentMethod)
          bindString(parameterIndex++, price)
          bindLong(parameterIndex++, timestamp)
        }
    notifyQueries(-1_445_505_644) { emit ->
      emit("BookingEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteBooking(id: String): QueryResult<Long> {
    val result = driver.execute(-1_138_770_334, """DELETE FROM BookingEntity WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(-1_138_770_334) { emit ->
      emit("BookingEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertSchedule(
    id: String,
    trainId: String,
    sourceStationId: String,
    destinationStationId: String,
    departureTimeMillis: Long,
    arrivalTimeMillis: Long,
    routeId: String,
    isActive: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-269_226_820, """
        |INSERT OR REPLACE INTO ScheduleEntity(id, trainId, sourceStationId, destinationStationId, departureTimeMillis, arrivalTimeMillis, routeId, isActive)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 8) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
          bindString(parameterIndex++, trainId)
          bindString(parameterIndex++, sourceStationId)
          bindString(parameterIndex++, destinationStationId)
          bindLong(parameterIndex++, departureTimeMillis)
          bindLong(parameterIndex++, arrivalTimeMillis)
          bindString(parameterIndex++, routeId)
          bindLong(parameterIndex++, isActive)
        }
    notifyQueries(-269_226_820) { emit ->
      emit("ScheduleEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateScheduleActive(isActive: Long, id: String): QueryResult<Long> {
    val result = driver.execute(-1_459_981_070,
        """UPDATE ScheduleEntity SET isActive = ? WHERE id = ?""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, isActive)
          bindString(parameterIndex++, id)
        }
    notifyQueries(-1_459_981_070) { emit ->
      emit("ScheduleEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteSchedule(id: String): QueryResult<Long> {
    val result = driver.execute(649_633_198, """DELETE FROM ScheduleEntity WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindString(parameterIndex++, id)
        }
    notifyQueries(649_633_198) { emit ->
      emit("ScheduleEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertState(
    name: String,
    points: String,
    centroidLat: Double,
    centroidLng: Double,
  ): QueryResult<Long> {
    val result = driver.execute(-1_071_700_852, """
        |INSERT OR REPLACE INTO StateEntity(name, points, centroidLat, centroidLng)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, points)
          bindDouble(parameterIndex++, centroidLat)
          bindDouble(parameterIndex++, centroidLng)
        }
    notifyQueries(-1_071_700_852) { emit ->
      emit("StateEntity")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAllStates(): QueryResult<Long> {
    val result = driver.execute(-1_607_463_220, """DELETE FROM StateEntity""", 0)
    notifyQueries(-1_607_463_220) { emit ->
      emit("StateEntity")
    }
    return result
  }

  private inner class FindUserByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("UserEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("UserEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(262_803_042,
        """SELECT UserEntity.id, UserEntity.username, UserEntity.password, UserEntity.isAdmin, UserEntity.balance FROM UserEntity WHERE id = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:findUserById"
  }

  private inner class FindUserByUsernameQuery<out T : Any>(
    public val username: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("UserEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("UserEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_435_944_739,
        """SELECT UserEntity.id, UserEntity.username, UserEntity.password, UserEntity.isAdmin, UserEntity.balance FROM UserEntity WHERE username = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, username)
    }

    override fun toString(): String = "RailwayDatabase.sq:findUserByUsername"
  }

  private inner class GetWalletBalanceQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("UserEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("UserEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(616_156_729, """SELECT balance FROM UserEntity WHERE id = ?""", mapper,
        1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:getWalletBalance"
  }

  private inner class FindStationByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("StationEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("StationEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(667_859_169,
        """SELECT StationEntity.id, StationEntity.name, StationEntity.latitude, StationEntity.longitude, StationEntity.designatedTrainId, StationEntity.info, StationEntity.terminal FROM StationEntity WHERE id = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:findStationById"
  }

  private inner class FindTrainByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("TrainEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("TrainEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(147_935_861,
        """SELECT TrainEntity.id, TrainEntity.name, TrainEntity.totalSeats, TrainEntity.status, TrainEntity.carriageCount FROM TrainEntity WHERE id = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:findTrainById"
  }

  private inner class FindRouteByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("RouteEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("RouteEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-588_772_810,
        """SELECT RouteEntity.id, RouteEntity.sourceStationId, RouteEntity.destinationStationId, RouteEntity.distance, RouteEntity.estimatedTime, RouteEntity.waypoints FROM RouteEntity WHERE id = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:findRouteById"
  }

  private inner class FindRoutesByStationsQuery<out T : Any>(
    public val sourceStationId: String,
    public val destinationStationId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("RouteEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("RouteEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_834_686_139,
        """SELECT RouteEntity.id, RouteEntity.sourceStationId, RouteEntity.destinationStationId, RouteEntity.distance, RouteEntity.estimatedTime, RouteEntity.waypoints FROM RouteEntity WHERE sourceStationId = ? AND destinationStationId = ?""",
        mapper, 2) {
      var parameterIndex = 0
      bindString(parameterIndex++, sourceStationId)
      bindString(parameterIndex++, destinationStationId)
    }

    override fun toString(): String = "RailwayDatabase.sq:findRoutesByStations"
  }

  private inner class FindBookingByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("BookingEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("BookingEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-236_214_906,
        """SELECT BookingEntity.id, BookingEntity.userId, BookingEntity.trainId, BookingEntity.passengerName, BookingEntity.carriageId, BookingEntity.seatNumber, BookingEntity.startStationId, BookingEntity.endStationId, BookingEntity.departureDate, BookingEntity.departureTimeMillis, BookingEntity.arrivalTimeMillis, BookingEntity.paymentMethod, BookingEntity.price, BookingEntity.timestamp FROM BookingEntity WHERE id = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:findBookingById"
  }

  private inner class FindBookingsByUserQuery<out T : Any>(
    public val userId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("BookingEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("BookingEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_380_533_505,
        """SELECT BookingEntity.id, BookingEntity.userId, BookingEntity.trainId, BookingEntity.passengerName, BookingEntity.carriageId, BookingEntity.seatNumber, BookingEntity.startStationId, BookingEntity.endStationId, BookingEntity.departureDate, BookingEntity.departureTimeMillis, BookingEntity.arrivalTimeMillis, BookingEntity.paymentMethod, BookingEntity.price, BookingEntity.timestamp FROM BookingEntity WHERE userId = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, userId)
    }

    override fun toString(): String = "RailwayDatabase.sq:findBookingsByUser"
  }

  private inner class CountUserBookingsQuery<out T : Any>(
    public val userId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("BookingEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("BookingEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(2_064_641_800,
        """SELECT COUNT(*) FROM BookingEntity WHERE userId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, userId)
    }

    override fun toString(): String = "RailwayDatabase.sq:countUserBookings"
  }

  private inner class FindScheduleByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ScheduleEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ScheduleEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(558_027_950,
        """SELECT ScheduleEntity.id, ScheduleEntity.trainId, ScheduleEntity.sourceStationId, ScheduleEntity.destinationStationId, ScheduleEntity.departureTimeMillis, ScheduleEntity.arrivalTimeMillis, ScheduleEntity.routeId, ScheduleEntity.isActive FROM ScheduleEntity WHERE id = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, id)
    }

    override fun toString(): String = "RailwayDatabase.sq:findScheduleById"
  }

  private inner class FindSchedulesByTrainQuery<out T : Any>(
    public val trainId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ScheduleEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ScheduleEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(992_746_170,
        """SELECT ScheduleEntity.id, ScheduleEntity.trainId, ScheduleEntity.sourceStationId, ScheduleEntity.destinationStationId, ScheduleEntity.departureTimeMillis, ScheduleEntity.arrivalTimeMillis, ScheduleEntity.routeId, ScheduleEntity.isActive FROM ScheduleEntity WHERE trainId = ?""",
        mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, trainId)
    }

    override fun toString(): String = "RailwayDatabase.sq:findSchedulesByTrain"
  }

  private inner class FindSchedulesForStationQuery<out T : Any>(
    public val sourceStationId: String,
    public val destinationStationId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("ScheduleEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("ScheduleEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-596_287_294,
        """SELECT ScheduleEntity.id, ScheduleEntity.trainId, ScheduleEntity.sourceStationId, ScheduleEntity.destinationStationId, ScheduleEntity.departureTimeMillis, ScheduleEntity.arrivalTimeMillis, ScheduleEntity.routeId, ScheduleEntity.isActive FROM ScheduleEntity WHERE sourceStationId = ? OR destinationStationId = ?""",
        mapper, 2) {
      var parameterIndex = 0
      bindString(parameterIndex++, sourceStationId)
      bindString(parameterIndex++, destinationStationId)
    }

    override fun toString(): String = "RailwayDatabase.sq:findSchedulesForStation"
  }
}
