package com.matechmatrix.shopflowpos.core.database

import com.matechmatrix.shopflowpos.db.ShopFlowDatabase
import kotlinx.datetime.Clock

/**
 * Single access point to all SQLDelight query objects.
 * Injected via Koin as a singleton.
 */
class DatabaseProvider(factory: DatabaseDriverFactory) {
    val database: ShopFlowDatabase = ShopFlowDatabase(factory.createDriver())

    init {
        seedDatabase()
    }

    private fun seedDatabase() {
        val now = Clock.System.now().toEpochMilliseconds()
        database.transaction {
            // Seed Invoice Sequences
            val prefixes = listOf("INV", "PO", "RET", "REP", "EMI")
            prefixes.forEach { prefix ->
                database.invoiceSequenceQueries.initSequence(prefix, now)
            }

            // Seed Default Cash Account
            database.ledgerQueriesQueries.initCashAccount(
                id = "default_cash",
                name = "Main Till",
                balance = 0.0,
                created_at = now,
                updated_at = now
            )
        }
    }

    fun <R> transaction(body: () -> R): R = database.transactionWithResult { body() }

    val productQueries        get() = database.productQueriesQueries
    val saleQueries           get() = database.saleQueriesQueries
    val saleReturnQueries     get() = database.saleReturnQueriesQueries
    val customerQueries       get() = database.customerQueriesQueries
    val supplierQueries       get() = database.supplierQueriesQueries
    val expenseQueries        get() = database.expenseQueriesQueries
    val ledgerQueries         get() = database.ledgerQueriesQueries
    val installmentQueries    get() = database.installmentQueriesQueries
    val repairJobQueries      get() = database.repairJobQueriesQueries
    val settingsQueries       get() = database.settingsQueriesQueries
    val purchaseQueries       get() = database.purchaseQueries
    val stockMovementQueries  get() = database.stockMovementQueries
    val invoiceSequenceQueries get() = database.invoiceSequenceQueries
}
