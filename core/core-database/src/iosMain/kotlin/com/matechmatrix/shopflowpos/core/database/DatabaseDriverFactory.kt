package com.matechmatrix.shopflowpos.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.matechmatrix.shopflowpos.db.ShopFlowDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema   = ShopFlowDatabase.Schema,
            name     = "shopflow.db"
        )
    }
}