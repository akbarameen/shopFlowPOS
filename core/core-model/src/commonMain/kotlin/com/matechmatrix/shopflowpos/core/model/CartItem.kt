package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Double get() = product.sellingPrice * quantity.toDouble()
}
