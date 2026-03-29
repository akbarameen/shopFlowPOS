package com.matechmatrix.shopflows.feature.auth.presentation

data class AuthState(
    val shopName: String     = "",
    val licenseKey: String   = "",
    val isLoading: Boolean   = false,
    val errorMessage: String? = null
)

sealed class AuthIntent {
    data class OnShopNameChanged(val name: String)   : AuthIntent()
    data class OnLicenseKeyChanged(val key: String)  : AuthIntent()
    data object OnActivateClicked                    : AuthIntent()
    data object OnDismissError                       : AuthIntent()
    data object CheckSession                         : AuthIntent()
}

sealed class AuthEffect {
    data object NavigateToDashboard : AuthEffect()
}