package com.matechmatrix.shopflowpos.feature.transactions.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Sale

interface TransactionsRepository {
    suspend fun getSalesByDateRange(startMs: Long, endMs: Long): AppResult<List<Sale>>
    suspend fun getTodaySales(): AppResult<List<Sale>>
    suspend fun getCurrencySymbol(): String
}
