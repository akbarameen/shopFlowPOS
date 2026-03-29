package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PurchaseStatus(val display: String) {
    PAID("Paid"),
    PARTIAL("Partial"),
    UNPAID("Unpaid");
}