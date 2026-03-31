package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id                  : String,
    val name                : String,
    val phone               : String  = "",
    val whatsapp            : String? = null,
    val cnic                : String? = null,
    val email               : String? = null,
    val address             : String  = "",
    val city                : String  = "",
    val creditLimit         : Double  = 0.0,   // 0 = no limit enforced
    val openingBalance      : Double  = 0.0,   // amount owed at setup time
    val outstandingBalance  : Double  = 0.0,   // running amount currently owed by customer
    val totalPurchases      : Double  = 0.0,
    val totalTransactions   : Int     = 0,
    val notes               : String? = null,
    val isActive            : Boolean = true,
    val createdAt           : Long,
    val updatedAt           : Long
) {
    val hasCredit: Boolean get() = creditLimit > 0
    val canExtendCredit: Boolean get() = !hasCredit || outstandingBalance < creditLimit
}