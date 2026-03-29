package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleReturn(
    val id: String,
    val originalSaleId: String,
    val productId: String,
    val productName: String,
    val returnedQuantity: Int,
    val originalSellingPrice: Double,
    val deductionAmount: Double = 0.0,
    val refundAmount: Double,
    val deductionAddedToProfit: Boolean = true,
    val restockedToInventory: Boolean = true,
    val returnReason: String,
    val returnedAt: Long
)