package com.matechmatrix.shopflowpos.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActivationResponse(
    val token: String,
    val expiresAt: Long,
    val shopName: String,
    val plan: String
)