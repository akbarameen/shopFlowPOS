package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ProductCondition {
    NEW,
    USED,
    REFURBISHED,
    LIKE_NEW,
    GOOD,
    FAIR


}