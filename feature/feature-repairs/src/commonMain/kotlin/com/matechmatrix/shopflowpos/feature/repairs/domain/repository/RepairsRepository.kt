package com.matechmatrix.shopflowpos.feature.repairs.domain.repository


import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.RepairJob
import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus

interface RepairsRepository {
    suspend fun getAllRepairs(): AppResult<List<RepairJob>>
    suspend fun getRepairsByStatus(status: RepairStatus): AppResult<List<RepairJob>>
    suspend fun insertRepair(repair: RepairJob): AppResult<Unit>
    suspend fun updateStatus(id: String, status: RepairStatus): AppResult<Unit>
    suspend fun completeRepair(id: String, finalCharge: Long): AppResult<Unit>
    suspend fun deleteRepair(id: String): AppResult<Unit>
    suspend fun getCurrencySymbol(): String
}