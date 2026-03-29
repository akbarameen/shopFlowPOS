package com.matechmatrix.shopflowpos.feature.settings.data.repository
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(private val db: DatabaseProvider) : SettingsRepository {

    private suspend fun get(key: String, default: String = ""): String =
        withContext(Dispatchers.Default) {
            db.settingsQueries.getSetting(key).executeAsOneOrNull() ?: default
        }

    private suspend fun set(key: String, value: String) = withContext(Dispatchers.Default) {
        db.settingsQueries.upsertSetting(key, value)
    }

    override suspend fun getShopName()                  = get("shop_name", "My Shop")
    override suspend fun setShopName(name: String)      = set("shop_name", name)
    override suspend fun getCurrencySymbol()            = get("currency_symbol", "Rs.")
    override suspend fun setCurrencySymbol(s: String)   = set("currency_symbol", s)
    override suspend fun getTheme()                     = get("theme", "system")
    override suspend fun setTheme(theme: String)        = set("theme", theme)
    override suspend fun getShowCostPrice()             = get("show_cost_price", "false") == "true"
    override suspend fun setShowCostPrice(show: Boolean) = set("show_cost_price", show.toString())
    override suspend fun getAnalyticsVisible()          = get("analytics_visible", "true") != "false"
    override suspend fun setAnalyticsVisible(v: Boolean) = set("analytics_visible", v.toString())
    override suspend fun getLowStockThreshold()         = get("low_stock_threshold", "5").toIntOrNull() ?: 5
    override suspend fun setLowStockThreshold(v: Int)  = set("low_stock_threshold", v.toString())

    override suspend fun signOut(): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.settingsQueries.upsertSetting("activation_token", "")
            AppResult.Success(Unit)
        } catch (e: Exception) { AppResult.Error(e.message ?: "Failed") }
    }
}