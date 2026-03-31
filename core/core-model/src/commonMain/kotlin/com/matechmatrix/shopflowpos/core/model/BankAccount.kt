package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class BankAccount(
    val id            : String,
    val bankName      : String,
    val accountTitle  : String,
    val accountNumber : String,
    val iban          : String? = null,
    val balance       : Double  = 0.0,
    val isActive      : Boolean = true,
    val createdAt     : Long,
    val updatedAt     : Long
)
