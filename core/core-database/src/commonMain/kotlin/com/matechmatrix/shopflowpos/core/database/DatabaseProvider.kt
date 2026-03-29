package com.matechmatrix.shopflowpos.core.database

import com.matechmatrix.shopflowpos.db.ShopFlowDatabase

/**
 * Single access point to all SQLDelight query objects.
 * Injected via Koin as a singleton.
 */
class DatabaseProvider(factory: DatabaseDriverFactory) {
    private val database: ShopFlowDatabase = ShopFlowDatabase(factory.createDriver())
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
}
