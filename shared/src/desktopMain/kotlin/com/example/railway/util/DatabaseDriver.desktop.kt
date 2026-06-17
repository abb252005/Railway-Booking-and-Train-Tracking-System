package com.example.railway.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.railway.db.RailwayDatabase

actual fun getDriver(): SqlDriver {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:railway_v8.db")
    try {
        RailwayDatabase.Schema.create(driver)
    } catch (e: Exception) {
        // Driver already created or table exists
    }
    return driver
}
