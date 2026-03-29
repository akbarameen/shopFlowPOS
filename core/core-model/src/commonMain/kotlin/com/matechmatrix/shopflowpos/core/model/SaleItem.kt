package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable


@Serializable
data class SaleItem(
    val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val costPrice: Double,
    val sellingPrice: Double,
    val discount: Double = 0.0,
    val lineTotal: Double
)