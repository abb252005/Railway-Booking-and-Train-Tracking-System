package com.example.railway.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.railway.db.shared.newInstance
import com.example.railway.db.shared.schema
import kotlin.Unit

public interface RailwayDatabase : Transacter {
  public val railwayDatabaseQueries: RailwayDatabaseQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = RailwayDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): RailwayDatabase =
        RailwayDatabase::class.newInstance(driver)
  }
}
