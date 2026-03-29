package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class BankAccount(
    val id: String,
    val bankName: String,
    val accountTitle: String,
    val accountNumber: String,
    val balance: Double = 0.0,
    val updatedAt: Long
)