package com.matechmatrix.shopflowpos.feature.purchase.domain.model

import kotlinx.serialization.Serializable

enum class PurchaseSourceType { SUPPLIER, CUSTOMER }

@Serializable
data class PurchaseSource(
    val type        : PurchaseSourceType,
    val id          : String?   = null,   // null = new contact (not yet in DB)
    val name        : String,
    val phone       : String    = "",
    val address     : String    = "",
    val city        : String    = "",
    val email       : String    = "",
    val isNew       : Boolean   = false   // true = insert to table on purchase
)