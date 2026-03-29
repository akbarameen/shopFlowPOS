package com.matechmatrix.shopflowpos.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActivationRequest(
    val licenseKey: String,
    val deviceId: String,
    val shopName: String
)