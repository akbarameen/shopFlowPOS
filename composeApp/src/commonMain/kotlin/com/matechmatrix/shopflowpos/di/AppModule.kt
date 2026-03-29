package com.matechmatrix.shopflowpos.di


import com.matechmatrix.shopflowpos.core.database.DatabaseDriverFactory
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.network.HttpClientFactory
import com.matechmatrix.shopflowpos.core.network.service.LicenseApiService
import org.koin.dsl.module

/**
 * Core Koin module — database, network, shared singletons.
 * Feature modules provide their own Koin modules.
 */
val coreModule = module {
    // ─── Database ────────────────────────────────────────────────────────────
    single { DatabaseProvider(get<DatabaseDriverFactory>()) }

    // ─── Network ─────────────────────────────────────────────────────────────
    single { HttpClientFactory.create() }
    single { LicenseApiService(get()) }
}