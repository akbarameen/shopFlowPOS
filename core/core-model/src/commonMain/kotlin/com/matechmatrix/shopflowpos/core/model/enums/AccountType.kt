package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType(val display: String) {
    CASH("Cash"),
    BANK("Bank"),
    MOBILE_WALLET("Mobile Wallet");   // JazzCash, EasyPaisa — future use

    val dbValue: String get() = name
}