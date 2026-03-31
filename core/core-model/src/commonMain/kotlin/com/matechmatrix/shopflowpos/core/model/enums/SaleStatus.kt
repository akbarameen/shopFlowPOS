package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class SaleStatus(val display: String) {
    COMPLETED("Completed"),
    PARTIALLY_RETURNED("Partially Returned"),
    RETURNED("Returned"),
    CANCELLED("Cancelled")
}