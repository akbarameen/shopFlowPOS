package com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.SaleReturn

interface SalesReturnRepository {
    suspend fun getReturnsByDateRange(startMs: Long, endMs: Long): AppResult<List<SaleReturn>>
    suspend fun getSaleByInvoice(invoiceNumber: String): AppResult<Sale?>
//    suspend fun insertReturn(saleReturn: SaleReturn, restockProductId: String?, restockQty: Int): AppResult<Unit>
suspend fun insertReturn(
    saleReturn: SaleReturn,
    restockProductId: String?,
    restockQty: Int
): AppResult<Unit>
    suspend fun getCurrencySymbol(): String
}