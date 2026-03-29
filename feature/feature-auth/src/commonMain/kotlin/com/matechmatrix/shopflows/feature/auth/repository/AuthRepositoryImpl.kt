package com.matechmatrix.shopflows.feature.auth.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.network.dto.ActivationRequest
import com.matechmatrix.shopflowpos.core.network.service.LicenseApiService
import com.matechmatrix.shopflows.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class AuthRepositoryImpl(
    private val db: DatabaseProvider,
    private val api: LicenseApiService
) : AuthRepository {

    override suspend fun activateLicense(
        licenseKey: String,
        shopName: String
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        val result = api.activateLicense(
            ActivationRequest(
                licenseKey = licenseKey,
                deviceId = "device_${Clock.System.now().toEpochMilliseconds()}",
                shopName   = shopName
            )
        )
        when (result) {
            is AppResult.Success -> {
                db.settingsQueries.upsertSetting("activation_token", result.data.token)
                db.settingsQueries.upsertSetting("shop_name", shopName)
                AppResult.Success(Unit)
            }
            is AppResult.Error   -> result
            is AppResult.Loading -> result
        }
    }

    override suspend fun isActivated(): Boolean = withContext(Dispatchers.Default) {
        val token = db.settingsQueries.getSetting("activation_token").executeAsOneOrNull()
        !token.isNullOrBlank()
    }

    override suspend fun logout(): Unit = withContext(Dispatchers.Default) {
        db.settingsQueries.upsertSetting("activation_token", "")
    }
}