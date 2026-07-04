package com.example.railway.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.railway.db.RailwayDatabase

actual fun getDriver(): SqlDriver {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:railway_v17.db")
    try {
        RailwayDatabase.Schema.create(driver)
    } catch (_: Exception) {
        // Already created
    }
    return driver
}
