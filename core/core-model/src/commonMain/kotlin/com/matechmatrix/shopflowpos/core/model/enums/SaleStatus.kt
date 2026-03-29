package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class SaleStatus {
    PENDING,
    COMPLETED,
    RETURNED,
    PARTIALLY_RETURNED
}