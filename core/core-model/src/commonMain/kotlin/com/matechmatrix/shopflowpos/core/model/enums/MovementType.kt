package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

/** Reason for a stock_movement row. Stored as TEXT in DB. */
@Serializable
enum class MovementType {
    OPENING,            // product created with initial stock
    PURCHASE,           // stock received from supplier
    SALE,               // stock sold to customer
    SALE_RETURN,        // customer returned item, stock back in
    PURCHASE_RETURN,    // item returned to supplier, stock out
    ADJUSTMENT_IN,      // manual positive correction
    ADJUSTMENT_OUT      // manual negative correction (damage, loss, theft)
}
