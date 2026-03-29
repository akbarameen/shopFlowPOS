package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    SALE_CASH,
    SALE_BANK,
    SALE_SPLIT_CASH,
    SALE_SPLIT_BANK,
    EXPENSE,
    RETURN_REFUND,
    RETURN_DEDUCTION,
    MANUAL_ADJUSTMENT,
    CREDIT
}
