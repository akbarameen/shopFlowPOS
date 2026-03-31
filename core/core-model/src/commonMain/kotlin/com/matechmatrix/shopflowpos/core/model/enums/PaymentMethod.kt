package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

// Used in POS UI only; actual payment is stored as sale_payment rows (one per account).
@Serializable
enum class PaymentMethod(val display: String) {
    CASH("Cash"),
    BANK("Bank Transfer / Card"),
    SPLIT("Split (Cash + Bank)"),
    CREDIT("Credit / Due");             // full amount deferred
}