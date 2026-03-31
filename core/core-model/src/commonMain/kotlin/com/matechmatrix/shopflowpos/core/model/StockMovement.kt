package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.MovementType
import kotlinx.serialization.Serializable

@Serializable
data class StockMovement(
    val id             : String,
    val productId      : String,
    val productName    : String,
    val movementType   : MovementType,
    val referenceType  : String? = null,
    val referenceId    : String? = null,
    val quantityBefore : Int,
    val quantityChange : Int,      // positive = in, negative = out
    val quantityAfter  : Int,
    val unitCost       : Double?  = null,
    val notes          : String?  = null,
    val createdAt      : Long
)