package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val brand: String = "",
    val model: String = "",
    val imei: String? = null,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stock: Int = 0,
    val lowStockThreshold: Int = 3,
    val condition: ProductCondition = ProductCondition.NEW,
    val category: ProductCategory = ProductCategory.PHONE,
    val ptaStatus: String = "NA", // NA, APPROVED, NON_PTA, JV
    val imageUri: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
