package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseOrderItem(
    val id              : String,
    val purchaseOrderId : String,
    val productId       : String,
    val productName     : String,
    val imei            : String? = null,
    val quantity        : Int,
    val unitCost        : Double,
    val totalCost       : Double
)