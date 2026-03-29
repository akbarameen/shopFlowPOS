package com.matechmatrix.shopflowpos.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.matechmatrix.shopflowpos.db.ShopFlowDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema  = ShopFlowDatabase.Schema,
            context = context,
            name    = "shopflow.db"
        )
    }
}