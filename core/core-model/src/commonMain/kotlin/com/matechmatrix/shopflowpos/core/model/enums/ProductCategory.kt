package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ProductCategory(val displayName: String) {
    PHONE("Phone"),
    TABLET("Tablet"),
    ACCESSORY("Accessory"),
    SPARE_PART("Spare Part"),
    OTHER("Other")
}
