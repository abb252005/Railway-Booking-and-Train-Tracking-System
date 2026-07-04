package com.example.railway.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.railway.db.RailwayDatabase
import android.content.Context

private var appContext: Context? = null

fun initDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun getDriver(): SqlDriver {
    return AndroidSqliteDriver(
        schema = RailwayDatabase.Schema,
        context = appContext ?: throw IllegalStateException("Database not initialized with context"),
        name = "railway_v17.db"
    )
}
