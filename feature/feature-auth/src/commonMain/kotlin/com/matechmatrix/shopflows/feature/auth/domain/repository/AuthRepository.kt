package com.matechmatrix.shopflows.feature.auth.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult

interface AuthRepository {
    suspend fun activateLicense(licenseKey: String, shopName: String): AppResult<Unit>
    suspend fun isActivated(): Boolean
    suspend fun logout()
}