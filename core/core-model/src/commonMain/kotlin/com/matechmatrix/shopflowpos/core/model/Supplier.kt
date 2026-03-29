package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier(
    val id: String,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val balance: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long
)
