package com.matechmatrix.shopflowpos.core.network.service

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.network.ApiEndpoints
import com.matechmatrix.shopflowpos.core.network.dto.ActivationRequest
import com.matechmatrix.shopflowpos.core.network.dto.ActivationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LicenseApiService(private val client: HttpClient) {

    suspend fun activateLicense(request: ActivationRequest): AppResult<ActivationResponse> {
        return try {
            val response = client.post(ApiEndpoints.ACTIVATE_LICENSE) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            AppResult.Success(response.body())
        } catch (e: Exception) {
            AppResult.Error(
                message = e.message ?: "Network error. Please check your connection.",
                cause = e
            )
        }
    }
}