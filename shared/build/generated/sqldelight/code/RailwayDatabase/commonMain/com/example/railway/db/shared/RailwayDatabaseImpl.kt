package com.example.railway.db.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.railway.db.RailwayDatabase
import com.example.railway.db.RailwayDatabaseQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<RailwayDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = RailwayDatabaseImpl.Schema

internal fun KClass<RailwayDatabase>.newInstance(driver: SqlDriver): RailwayDatabase =
    RailwayDatabaseImpl(driver)

private class RailwayDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), RailwayDatabase {
  override val railwayDatabaseQueries: RailwayDatabaseQueries = RailwayDatabaseQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE UserEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    username TEXT NOT NULL UNIQUE,
          |    password TEXT NOT NULL,
          |    isAdmin INTEGER NOT NULL DEFAULT 0,
          |    balance REAL NOT NULL DEFAULT 0.0
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE StationEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    name TEXT NOT NULL,
          |    latitude REAL NOT NULL,
          |    longitude REAL NOT NULL,
          |    designatedTrainId TEXT,
          |    info TEXT NOT NULL DEFAULT '',
          |    terminal TEXT NOT NULL DEFAULT 'Main Terminal'
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE TrainEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    name TEXT NOT NULL,
          |    totalSeats INTEGER NOT NULL,
          |    status TEXT NOT NULL,
          |    carriageCount INTEGER NOT NULL DEFAULT 3
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE RouteEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    sourceStationId TEXT NOT NULL,
          |    destinationStationId TEXT NOT NULL,
          |    distance REAL NOT NULL,
          |    estimatedTime INTEGER NOT NULL,
          |    waypoints TEXT,
          |    FOREIGN KEY (sourceStationId) REFERENCES StationEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (destinationStationId) REFERENCES StationEntity(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE BookingEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    userId TEXT NOT NULL,
          |    trainId TEXT NOT NULL,
          |    passengerName TEXT NOT NULL,
          |    carriageId TEXT NOT NULL,
          |    seatNumber TEXT NOT NULL,
          |    startStationId TEXT NOT NULL,
          |    endStationId TEXT NOT NULL,
          |    departureDate INTEGER NOT NULL,
          |    departureTimeMillis INTEGER NOT NULL DEFAULT 0,
          |    arrivalTimeMillis INTEGER NOT NULL DEFAULT 0,
          |    paymentMethod TEXT NOT NULL,
          |    price TEXT NOT NULL,
          |    timestamp INTEGER NOT NULL,
          |    FOREIGN KEY (userId) REFERENCES UserEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (trainId) REFERENCES TrainEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (startStationId) REFERENCES StationEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (endStationId) REFERENCES StationEntity(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE ScheduleEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    trainId TEXT NOT NULL,
          |    sourceStationId TEXT NOT NULL,
          |    destinationStationId TEXT NOT NULL,
          |    departureTimeMillis INTEGER NOT NULL,
          |    arrivalTimeMillis INTEGER NOT NULL,
          |    routeId TEXT NOT NULL,
          |    isActive INTEGER NOT NULL DEFAULT 1,
          |    FOREIGN KEY (trainId) REFERENCES TrainEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (routeId) REFERENCES RouteEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (sourceStationId) REFERENCES StationEntity(id) ON DELETE CASCADE,
          |    FOREIGN KEY (destinationStationId) REFERENCES StationEntity(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE StateEntity (
          |    name TEXT NOT NULL PRIMARY KEY,
          |    points TEXT NOT NULL, -- Serialized List<Pair<Double, Double>> "lat,lng;lat,lng"
          |    centroidLat REAL NOT NULL,
          |    centroidLng REAL NOT NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
