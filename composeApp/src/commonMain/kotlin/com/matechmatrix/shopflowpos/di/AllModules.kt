package com.matechmatrix.shopflowpos.di

import com.matechmatrix.shopflowpos.feature.customers.di.customersModule
import com.matechmatrix.shopflowpos.feature.dashboard.di.dashboardModule
import com.matechmatrix.shopflowpos.feature.dues.di.duesModule
import com.matechmatrix.shopflowpos.feature.expenses.di.expensesModule
import com.matechmatrix.shopflowpos.feature.installments.di.installmentsModule
import com.matechmatrix.shopflowpos.feature.inventory.di.inventoryModule
import com.matechmatrix.shopflowpos.feature.ledger.di.ledgerModule
import com.matechmatrix.shopflowpos.feature.pos.di.posModule
import com.matechmatrix.shopflowpos.feature.purchase.di.purchaseModule
import com.matechmatrix.shopflowpos.feature.repairs.di.repairsModule
import com.matechmatrix.shopflowpos.feature.reports.di.reportsModule
import com.matechmatrix.shopflowpos.feature.salesreturn.di.salesReturnModule
import com.matechmatrix.shopflowpos.feature.settings.di.settingsModule
import com.matechmatrix.shopflowpos.feature.suppliers.di.suppliersModule
import com.matechmatrix.shopflowpos.feature.transactions.di.transactionsModule
import com.matechmatrix.shopflows.feature.auth.di.authModule

val allModules = listOf(
    platformModule,
    coreModule,
    authModule,
    dashboardModule,
    posModule,
    inventoryModule,
    transactionsModule,
    salesReturnModule,
    customersModule,
    suppliersModule,
    expensesModule,
    ledgerModule,
    reportsModule,
    installmentsModule,
    repairsModule,
    settingsModule,
    purchaseModule,
    duesModule

)