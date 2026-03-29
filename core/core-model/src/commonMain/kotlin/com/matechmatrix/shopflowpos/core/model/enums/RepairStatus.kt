package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class RepairStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED,
    CANCELLED
}