package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentStatus(val display: String) {
    UNPAID("Unpaid"),
    PARTIAL("Partial"),
    PAID("Paid"),
    RETURNED("Returned"),
    CANCELLED("Cancelled");

    val isFullyPaid: Boolean get() = this == PAID
    val hasDue: Boolean      get() = this == UNPAID || this == PARTIAL
}