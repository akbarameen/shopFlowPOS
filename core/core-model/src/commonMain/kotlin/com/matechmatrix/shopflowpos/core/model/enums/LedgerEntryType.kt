package com.matechmatrix.shopflowpos.core.model.enums

import kotlinx.serialization.Serializable

/** Direction of a ledger entry (money in vs money out of an account). */
@Serializable
enum class LedgerEntryType { CREDIT, DEBIT }