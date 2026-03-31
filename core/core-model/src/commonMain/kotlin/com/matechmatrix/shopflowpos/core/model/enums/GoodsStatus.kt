package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

/** Delivery state of a purchase order. */
@Serializable
enum class GoodsStatus(val display: String) {
    ORDERED("Ordered"),
    PARTIALLY_RECEIVED("Partially Received"),
    RECEIVED("Received"),
    CANCELLED("Cancelled")
}