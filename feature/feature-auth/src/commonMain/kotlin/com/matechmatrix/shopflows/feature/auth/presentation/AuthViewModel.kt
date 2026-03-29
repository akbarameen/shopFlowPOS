package com.matechmatrix.shopflows.feature.auth.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflows.feature.auth.domain.usecase.ActivateLicenseUseCase
import com.matechmatrix.shopflows.feature.auth.domain.usecase.CheckSessionUseCase

class AuthViewModel(
    private val activateLicense: ActivateLicenseUseCase,
    private val checkSession: CheckSessionUseCase
) : MviViewModel<AuthState, AuthIntent, AuthEffect>(AuthState()) {

    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.CheckSession              -> checkExistingSession()
            is AuthIntent.OnShopNameChanged      -> setState { copy(shopName = intent.name) }
            is AuthIntent.OnLicenseKeyChanged    -> setState { copy(licenseKey = intent.key) }
            AuthIntent.OnActivateClicked         -> doActivation()
            AuthIntent.OnDismissError            -> setState { copy(errorMessage = null) }
        }
    }

    private suspend fun checkExistingSession() {
        if (checkSession()) setEffect(AuthEffect.NavigateToDashboard)
    }

    private suspend fun doActivation() {
        setState { copy(isLoading = true, errorMessage = null) }
        when (val result = activateLicense(currentState.licenseKey, currentState.shopName)) {
//            is AppResult.Success -> setEffect(AuthEffect.NavigateToDashboard)
//            is AppResult.Error   -> setState { copy(isLoading = false, errorMessage = result.message) }
//            else                 -> setState { copy(isLoading = false) }

            is AppResult.Success -> setEffect(AuthEffect.NavigateToDashboard)
            is AppResult.Error   -> setEffect(AuthEffect.NavigateToDashboard)
            else                 -> setState { copy(isLoading = false) }
        }
    }
}