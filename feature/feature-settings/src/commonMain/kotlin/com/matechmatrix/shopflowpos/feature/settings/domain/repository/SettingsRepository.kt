package com.matechmatrix.shopflowpos.feature.settings.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult


interface SettingsRepository {
    suspend fun getShopName(): String
    suspend fun setShopName(name: String)
    suspend fun getCurrencySymbol(): String
    suspend fun setCurrencySymbol(symbol: String)
    suspend fun getTheme(): String
    suspend fun setTheme(theme: String)
    suspend fun getShowCostPrice(): Boolean
    suspend fun setShowCostPrice(show: Boolean)
    suspend fun getAnalyticsVisible(): Boolean
    suspend fun setAnalyticsVisible(visible: Boolean)
    suspend fun getLowStockThreshold(): Int
    suspend fun setLowStockThreshold(value: Int)
    suspend fun signOut(): AppResult<Unit>
}