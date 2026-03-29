package com.matechmatrix.shopflowpos.feature.installments.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Installment

interface InstallmentsRepository {
    suspend fun getAllInstallments(): AppResult<List<Installment>>
    suspend fun insertInstallment(installment: Installment): AppResult<Unit>
    suspend fun recordPayment(installmentId: String, nextDueDate: Long): AppResult<Unit>
    suspend fun deleteInstallment(id: String): AppResult<Unit>
    suspend fun getCurrencySymbol(): String
}