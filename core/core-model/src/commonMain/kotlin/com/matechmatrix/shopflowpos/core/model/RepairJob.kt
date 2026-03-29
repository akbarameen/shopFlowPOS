package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus
import kotlinx.serialization.Serializable

@Serializable
data class RepairJob(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val deviceModel: String,
    val problem: String,
    val estimatedCost: Double,
    val finalCost: Double? = null,
    val status: RepairStatus = RepairStatus.PENDING,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)