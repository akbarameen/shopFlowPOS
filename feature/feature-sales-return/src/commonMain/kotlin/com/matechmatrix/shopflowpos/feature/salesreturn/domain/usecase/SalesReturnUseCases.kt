package com.matechmatrix.shopflowpos.feature.salesreturn.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.ReturnRequest
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.SalesReturnRepository

class GetReturnsByDateRangeUseCase(private val repo: SalesReturnRepository) {
    suspend operator fun invoke(startMs: Long, endMs: Long): AppResult<List<SaleReturn>> =
        repo.getReturnsByDateRange(startMs, endMs)
}

class LookupSaleByInvoiceUseCase(private val repo: SalesReturnRepository) {
    suspend operator fun invoke(invoice: String): AppResult<Pair<Sale, List<SaleItem>>?> {
        if (invoice.isBlank()) return AppResult.Error("Enter an invoice number")
        val saleResult = repo.getSaleByInvoice(invoice.trim().uppercase())
        val sale = when (saleResult) {
            is AppResult.Success -> saleResult.data ?: return AppResult.Error("Invoice \"$invoice\" not found")
            is AppResult.Error   -> return AppResult.Error(saleResult.message)
            else                 -> return AppResult.Error("Unknown error")
        }
        val itemsResult = repo.getSaleItems(sale.id)
        val items = when (itemsResult) {
            is AppResult.Success -> itemsResult.data
            else                 -> emptyList()
        }
        return AppResult.Success(sale to items)
    }
}

class ProcessSaleReturnUseCase(private val repo: SalesReturnRepository) {
    suspend operator fun invoke(request: ReturnRequest): AppResult<SaleReturn> {
        if (request.items.isEmpty()) return AppResult.Error("Select at least one item to return")
        if (request.items.all { it.returnedQty <= 0 }) return AppResult.Error("Enter quantity to return")
        if (request.returnReason.isBlank()) return AppResult.Error("Return reason is required")
        request.items.forEach { ri ->
            if (ri.returnedQty > ri.saleItem.quantity)
                return AppResult.Error("Cannot return more than sold qty for ${ri.saleItem.productName}")
        }
        if (request.refundMethod != com.matechmatrix.shopflowpos.core.model.enums.RefundMethod.STORE_CREDIT
            && request.accountId.isNullOrBlank())
            return AppResult.Error("Select a refund account")

        return repo.processReturn(request)
    }
}

data class SalesReturnSettings(
    val currencySymbol     : String,
    val defaultDeduction   : Double,
    val cashAccounts       : List<CashAccount>,
    val bankAccounts       : List<BankAccount>
)

class GetSalesReturnSettingsUseCase(private val repo: SalesReturnRepository) {
    suspend operator fun invoke(): SalesReturnSettings = SalesReturnSettings(
        currencySymbol   = repo.getCurrencySymbol(),
        defaultDeduction = repo.getDefaultDeductionPercent(),
        cashAccounts     = (repo.getActiveCashAccounts() as? AppResult.Success)?.data ?: emptyList(),
        bankAccounts     = (repo.getActiveBankAccounts() as? AppResult.Success)?.data ?: emptyList()
    )
}