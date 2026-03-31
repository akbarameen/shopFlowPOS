package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class InstallmentFrequency(val display: String) {
    WEEKLY("Weekly"),
    BIWEEKLY("Bi-Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly")
}