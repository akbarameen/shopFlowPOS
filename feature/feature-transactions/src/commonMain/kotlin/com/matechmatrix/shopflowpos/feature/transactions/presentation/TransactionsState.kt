package com.matechmatrix.shopflowpos.feature.transactions.presentation

import com.matechmatrix.shopflowpos.core.model.Sale

enum class TxDateFilter { TODAY, WEEK, MONTH, ALL }

data class TransactionsState(
    val isLoading: Boolean = true,
    val sales: List<Sale> = emptyList(),
    val dateFilter: TxDateFilter = TxDateFilter.TODAY,
    val currencySymbol: String = "Rs.",
    val totalRevenue: Double = 0.0,
    val totalCount: Int = 0,
    val error: String? = null
)

sealed class TransactionsIntent {
    data object Load : TransactionsIntent()
    data class SetFilter(val filter: TxDateFilter) : TransactionsIntent()
}

sealed class TransactionsEffect
