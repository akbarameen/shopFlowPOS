data class SettingsState(
    val isLoading: Boolean = true,
    val shopName: String = "",
    val currencySymbol: String = "Rs.",
    val theme: String = "system",
    val showCostPrice: Boolean = false,
    val analyticsVisible: Boolean = true,
    val lowStockThreshold: Int = 5,
    // Dialogs
    val showShopNameDialog: Boolean = false,
    val showCurrencyDialog: Boolean = false,
    val showLowStockDialog: Boolean = false,
    val showSignOutConfirm: Boolean = false,
    val shopNameInput: String = "",
    val currencyInput: String = "",
    val lowStockInput: String = "",
    val successMessage: String? = null
)

sealed class SettingsIntent {
    data object Load : SettingsIntent()
    // Shop name
    data object ShowShopNameDialog : SettingsIntent()
    data class SetShopNameInput(val v: String) : SettingsIntent()
    data object SaveShopName : SettingsIntent()
    data object DismissShopNameDialog : SettingsIntent()
    // Currency
    data object ShowCurrencyDialog : SettingsIntent()
    data class SetCurrencyInput(val v: String) : SettingsIntent()
    data object SaveCurrency : SettingsIntent()
    data object DismissCurrencyDialog : SettingsIntent()
    // Toggles
    data class SetShowCostPrice(val v: Boolean) : SettingsIntent()
    data class SetAnalyticsVisible(val v: Boolean) : SettingsIntent()
    data class SetTheme(val v: String) : SettingsIntent()
    // Low stock
    data object ShowLowStockDialog : SettingsIntent()
    data class SetLowStockInput(val v: String) : SettingsIntent()
    data object SaveLowStock : SettingsIntent()
    data object DismissLowStockDialog : SettingsIntent()
    // Sign out
    data object ShowSignOutConfirm : SettingsIntent()
    data object DismissSignOutConfirm : SettingsIntent()
    data object SignOut : SettingsIntent()
}

sealed class SettingsEffect {
    data class ShowToast(val message: String) : SettingsEffect()
    data object NavigateToAuth : SettingsEffect()
}