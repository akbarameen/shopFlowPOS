package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseCategory(val displayName: String) {
    RENT("Rent"),
    SALARY("Salary"),
    UTILITIES("Utilities"),
    SUPPLIES("Supplies"),
    MARKETING("Marketing"),
    REPAIR("Repair / Maintenance"),
    TRANSPORT("Transport"),
    TAXES("Taxes / Government"),
    OTHER("Other")
}