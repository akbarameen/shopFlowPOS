package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.PaymentStatus
import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus
import kotlinx.serialization.Serializable

@Serializable
data class RepairJob(
    val id                  : String,
    val jobNumber           : String,
    val customerName        : String,
    val customerPhone       : String,
    val customerCnic        : String? = null,
    val deviceBrand         : String  = "",
    val deviceModel         : String,
    val deviceColor         : String? = null,
    val serialNumber        : String? = null,
    val imei                : String? = null,
    val problemDescription  : String,
    val diagnosisNotes      : String? = null,
    val accessoriesReceived : String? = null,
    val estimatedCost       : Double  = 0.0,
    val partsCost           : Double  = 0.0,
    val labourCost          : Double  = 0.0,
    val finalCost           : Double  = 0.0,
    val advancePaid         : Double  = 0.0,
    val balanceDue          : Double  = 0.0,
    val paymentStatus       : PaymentStatus = PaymentStatus.UNPAID,
    val status              : RepairStatus  = RepairStatus.RECEIVED,
    val notes               : String? = null,
    val createdAt           : Long,
    val updatedAt           : Long
)