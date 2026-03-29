package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    CARD,
    CREDIT,
    SPLIT,
    PARTIAL

}
