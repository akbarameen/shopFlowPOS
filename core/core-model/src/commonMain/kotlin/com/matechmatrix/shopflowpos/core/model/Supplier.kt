package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier(
    val id                  : String,
    val name                : String,
    val phone               : String  = "",
    val whatsapp            : String? = null,
    val email               : String  = "",
    val address             : String  = "",
    val city                : String  = "",
    val ntn                 : String? = null,  // National Tax Number (PK)
    val openingBalance      : Double  = 0.0,   // amount owed at setup time
    val outstandingBalance  : Double  = 0.0,   // running amount currently owed to supplier
    val totalPurchased      : Double  = 0.0,
    val notes               : String? = null,
    val isActive            : Boolean = true,
    val createdAt           : Long,
    val updatedAt           : Long
)
