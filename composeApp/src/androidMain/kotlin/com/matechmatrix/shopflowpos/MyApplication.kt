package com.matechmatrix.shopflowpos

import android.app.Application
import com.matechmatrix.shopflowpos.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(allModules)
        }
    }
}