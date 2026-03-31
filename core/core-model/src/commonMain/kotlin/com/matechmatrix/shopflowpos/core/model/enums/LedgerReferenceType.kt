package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

/** What caused a ledger entry to be created. Stored as TEXT in DB. */
@Serializable
enum class LedgerReferenceType {
    SALE_PAYMENT,
    PURCHASE_PAYMENT,
    EXPENSE,
    TRANSFER_IN,
    TRANSFER_OUT,
    REPAIR_PAYMENT,
    INSTALLMENT_PAYMENT,
    OPENING,
    ADJUSTMENT
}