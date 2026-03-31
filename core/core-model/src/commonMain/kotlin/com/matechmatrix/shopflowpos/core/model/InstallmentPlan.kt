package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.InstallmentFrequency
import kotlinx.serialization.Serializable

@Serializable
data class InstallmentPlan(
    val id                 : String,
    val planNumber         : String,
    val saleId             : String? = null,
    val customerId         : String? = null,
    val customerName       : String,
    val customerPhone      : String  = "",
    val customerCnic       : String? = null,
    val customerAddress    : String  = "",
    val productId          : String,
    val productName        : String,
    val imei               : String? = null,
    val totalAmount        : Double,
    val downPayment        : Double  = 0.0,
    val financedAmount     : Double,
    val installmentAmount  : Double,
    val totalInstallments  : Int,
    val paidInstallments   : Int     = 0,
    val paidAmount         : Double  = 0.0,
    val remainingAmount    : Double,
    val frequency          : InstallmentFrequency = InstallmentFrequency.MONTHLY,
    val startDate          : Long,
    val nextDueDate        : Long,
    val isCompleted        : Boolean = false,
    val isDefaulted        : Boolean = false,
    val notes              : String? = null,
    val createdAt          : Long,
    val updatedAt          : Long
)