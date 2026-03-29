package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val phone: String = "",
    val cnic: String? = null,
    val address: String = "",
    val notes: String? = null,
    val dueBalance: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalTransactions: Int = 0,
    val createdAt: Long
)
