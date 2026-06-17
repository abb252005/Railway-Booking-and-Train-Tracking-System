package com.example.railway.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.railway.db.RailwayDatabase

actual fun getDriver(): SqlDriver {
    return NativeSqliteDriver(RailwayDatabase.Schema, "railway_v8.db")
}
