package com.matechmatrix.shopflowpos.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.matechmatrix.shopflowpos.db.ShopFlowDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".shopflowpos")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        val dbFile = File(dbDir, "shopflow.db")
        val needsCreate = !dbFile.exists()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        
        if (needsCreate) {
            ShopFlowDatabase.Schema.create(driver)
        }
        
        // Ensure foreign keys are enabled for SQLite on Desktop
        driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        
        return driver
    }
}
