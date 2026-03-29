package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    CASH,
    BANK,
    MOBILE_WALLET
}
