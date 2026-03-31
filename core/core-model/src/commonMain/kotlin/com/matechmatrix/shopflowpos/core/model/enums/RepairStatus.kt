package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class RepairStatus(val display: String) {
    RECEIVED("Received"),
    DIAGNOSING("Diagnosing"),
    WAITING_PARTS("Waiting for Parts"),
    IN_REPAIR("In Repair"),
    READY("Ready for Pickup"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    val isOpen: Boolean get() = this != DELIVERED && this != CANCELLED
}