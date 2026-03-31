package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable


@Serializable
data class SaleItem(
    val id          : String,
    val saleId      : String,
    val productId   : String,
    val productName : String,
    val imei        : String? = null,   // snapshot for IMEI-tracked units
    val category    : String  = "",
    val quantity    : Int,
    val unitCost    : Double,           // cost at time of sale (for COGS)
    val unitPrice   : Double,           // selling price at time of sale
    val discount    : Double  = 0.0,
    val lineTotal   : Double            // (unitPrice - discount) * quantity
)