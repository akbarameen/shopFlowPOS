package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleReturnItem(
    val id                  : String,
    val returnId            : String,
    val originalSaleItemId  : String,
    val productId           : String,
    val productName         : String,
    val imei                : String? = null,
    val returnedQuantity    : Int,
    val unitPrice           : Double,
    val restockItem         : Boolean = true,
    val lineRefund          : Double
)