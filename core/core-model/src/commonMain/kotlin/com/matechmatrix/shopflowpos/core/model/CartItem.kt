package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product  : Product,
    val quantity : Int,
    val discount : Double = 0.0   // per-item discount amount
) {
    val unitPrice : Double get() = product.sellingPrice
    val lineTotal : Double get() = (unitPrice - discount) * quantity
    val lineCost  : Double get() = product.costPrice * quantity
    val lineProfit: Double get() = lineTotal - lineCost
}
