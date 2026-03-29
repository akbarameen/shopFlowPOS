package com.matechmatrix.shopflowpos.di

import com.matechmatrix.shopflowpos.core.database.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory() }
}