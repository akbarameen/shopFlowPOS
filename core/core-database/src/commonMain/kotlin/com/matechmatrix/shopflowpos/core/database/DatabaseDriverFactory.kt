package com.matechmatrix.shopflowpos.core.database

import app.cash.sqldelight.db.SqlDriver

/**
 * expect/actual pattern:
 * - This file is the CONTRACT (in commonMain).
 * - androidMain, iosMain, jvmMain each provide their own actual implementation.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}