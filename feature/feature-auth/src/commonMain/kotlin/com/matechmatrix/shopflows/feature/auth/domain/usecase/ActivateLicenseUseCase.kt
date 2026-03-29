package com.matechmatrix.shopflows.feature.auth.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflows.feature.auth.domain.repository.AuthRepository

class ActivateLicenseUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(licenseKey: String, shopName: String): AppResult<Unit> {
        if (licenseKey.isBlank()) return AppResult.Error("License key cannot be empty")
        if (shopName.isBlank())   return AppResult.Error("Shop name cannot be empty")
        return repository.activateLicense(licenseKey.trim(), shopName.trim())
    }
}