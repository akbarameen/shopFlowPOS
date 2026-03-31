package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class RefundMethod(val display: String) {
    CASH("Cash Refund"),
    BANK("Bank Transfer"),
    STORE_CREDIT("Store Credit")
}