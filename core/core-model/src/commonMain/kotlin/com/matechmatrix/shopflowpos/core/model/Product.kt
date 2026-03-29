package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id                : String,
    val name              : String,
    val brand             : String  = "",
    val model             : String  = "",
    val imei              : String? = null,
    val barcode           : String? = null,
    val category          : ProductCategory,
    val condition         : ProductCondition,
    val ptaStatus         : String  = "NA",
    val costPrice         : Double,
    val sellingPrice      : Double,
    val stock             : Int     = 0,
    val lowStockThreshold : Int     = 3,
    val description       : String? = null,
    val isActive          : Boolean = true,
    val createdAt         : Long,
    val updatedAt         : Long,
    val imageUri          : String? = null,
    // Specs — applicable to PHONE / LAPTOP / TABLET
    val color             : String? = null,
    val storageGb         : Int?    = null,   // e.g. 128, 256, 512, 1024
    val ramGb             : Int?    = null,   // e.g. 4, 6, 8, 12, 16, 32
    val romGb             : Int?    = null,   // built-in ROM (some devices differ from storage)
    val batteryMah        : Int?    = null,
    val screenSizeInch    : Float?  = null,
    val processor         : String? = null,
) {
    /** True when a unit is tracked by IMEI (each unit is a separate record, stock is always 1) */
    val isImeiTracked: Boolean get() = category.hasImei

    /** Effective stock: IMEI-tracked items show as 1 if active, 0 if sold */
    val effectiveStock: Int get() = if (isImeiTracked) (if (isActive && stock > 0) 1 else 0) else stock

    val isLowStock: Boolean
        get() = if (isImeiTracked) false else stock <= lowStockThreshold && stock > 0

    val isOutOfStock: Boolean
        get() = if (isImeiTracked) !isActive else stock <= 0

    /** Display subtitle – shown in list/grid cards */
    val subtitle: String
        get() = buildString {
            if (brand.isNotBlank()) append(brand)
            if (model.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(model)
            }
        }

    /** Short spec tag line */
    val specLine: String?
        get() = buildList {
            ramGb?.let { add("${it}GB RAM") }
            storageGb?.let { add("${it}GB") }
            color?.let { add(it) }
        }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}