package com.matechmatrix.shopflowpos.feature.settings.presentation

import SettingsEffect
import SettingsIntent
import SettingsState
import androidx.lifecycle.viewModelScope
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: SettingsRepository) :
    MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    init {
        // Launch a coroutine to call the suspended function
        viewModelScope.launch {
            onIntent(SettingsIntent.Load)
        }
    }

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Load -> load()

            SettingsIntent.ShowShopNameDialog -> setState { copy(showShopNameDialog = true, shopNameInput = shopName) }
            is SettingsIntent.SetShopNameInput -> setState { copy(shopNameInput = intent.v) }
            SettingsIntent.SaveShopName -> {
                val name = state.value.shopNameInput.trim()
                if (name.isNotBlank()) { repo.setShopName(name); setState { copy(shopName = name, showShopNameDialog = false) } }
            }
            SettingsIntent.DismissShopNameDialog -> setState { copy(showShopNameDialog = false) }

            SettingsIntent.ShowCurrencyDialog -> setState { copy(showCurrencyDialog = true, currencyInput = currencySymbol) }
            is SettingsIntent.SetCurrencyInput -> setState { copy(currencyInput = intent.v) }
            SettingsIntent.SaveCurrency -> {
                val sym = state.value.currencyInput.trim()
                if (sym.isNotBlank()) { repo.setCurrencySymbol(sym); setState { copy(currencySymbol = sym, showCurrencyDialog = false) } }
            }
            SettingsIntent.DismissCurrencyDialog -> setState { copy(showCurrencyDialog = false) }

            is SettingsIntent.SetShowCostPrice -> { repo.setShowCostPrice(intent.v); setState { copy(showCostPrice = intent.v) } }
            is SettingsIntent.SetAnalyticsVisible -> { repo.setAnalyticsVisible(intent.v); setState { copy(analyticsVisible = intent.v) } }
            is SettingsIntent.SetTheme -> { repo.setTheme(intent.v); setState { copy(theme = intent.v) } }

            SettingsIntent.ShowLowStockDialog -> setState { copy(showLowStockDialog = true, lowStockInput = lowStockThreshold.toString()) }
            is SettingsIntent.SetLowStockInput -> setState { copy(lowStockInput = intent.v) }
            SettingsIntent.SaveLowStock -> {
                val v = state.value.lowStockInput.toIntOrNull() ?: return
                repo.setLowStockThreshold(v); setState { copy(lowStockThreshold = v, showLowStockDialog = false) }
            }
            SettingsIntent.DismissLowStockDialog -> setState { copy(showLowStockDialog = false) }

            SettingsIntent.ShowSignOutConfirm -> setState { copy(showSignOutConfirm = true) }
            SettingsIntent.DismissSignOutConfirm -> setState { copy(showSignOutConfirm = false) }
            SettingsIntent.SignOut -> {
                setState { copy(showSignOutConfirm = false) }
                when (repo.signOut()) {
                    is AppResult.Success -> setEffect(SettingsEffect.NavigateToAuth)
                    is AppResult.Error   -> setEffect(SettingsEffect.ShowToast("Sign out failed"))
                    else -> {}
                }
            }
        }
    }

    private suspend fun load() {
        // 1. Show loading state immediately
        setState { copy(isLoading = true) }

        try {
            // 2. Fetch all values from repository (suspended calls)
            val name = repo.getShopName()
            val currency = repo.getCurrencySymbol()
            val currentTheme = repo.getTheme()
            val costVisible = repo.getShowCostPrice()
            val analytics = repo.getAnalyticsVisible()
            val threshold = repo.getLowStockThreshold()

            // 3. Update state once with all final values
            setState {
                copy(
                    isLoading = false,
                    shopName = name,
                    currencySymbol = currency,
                    theme = currentTheme,
                    showCostPrice = costVisible,
                    analyticsVisible = analytics,
                    lowStockThreshold = threshold
                )
            }
        } catch (e: Exception) {
            // 4. Handle potential errors
            setState { copy(isLoading = false) }
            setEffect(SettingsEffect.ShowToast("Failed to load settings"))
        }
    }
}