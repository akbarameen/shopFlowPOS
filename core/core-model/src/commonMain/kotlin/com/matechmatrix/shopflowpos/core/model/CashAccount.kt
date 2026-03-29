package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CashAccount(
    val id: String = "default_cash",
    val name: String = "Cash in Hand",
    val balance: Double = 0.0,
    val updatedAt: Long
)