package com.matechmatrix.shopflowpos.feature.repairs.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.RepairJob
import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus
import com.matechmatrix.shopflowpos.feature.repairs.domain.repository.RepairsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class RepairsRepositoryImpl(private val db: DatabaseProvider) : RepairsRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Repair_job) = RepairJob(
        id = r.id,
        customerName = r.customer_name,
        customerPhone = r.customer_phone,
        deviceModel = r.device_model,
        problem = r.problem,
        estimatedCost = r.estimated_cost,
        finalCost = r.final_cost,
        status = runCatching { RepairStatus.valueOf(r.status) }.getOrDefault(RepairStatus.PENDING),
        notes = r.notes,
        createdAt = r.created_at,
        updatedAt = r.updated_at
    )

    override suspend fun getAllRepairs(): AppResult<List<RepairJob>> = withContext(Dispatchers.IO) {
        try {
            val list = db.repairJobQueries.getAllRepairJobs().executeAsList().map(::mapRow)
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load repairs")
        }
    }

    override suspend fun getRepairsByStatus(status: RepairStatus): AppResult<List<RepairJob>> = withContext(Dispatchers.IO) {
        try {
            // Re-using the filter logic or adding a specific query to .sq
            val list = db.repairJobQueries.getAllRepairJobs().executeAsList()
                .filter { it.status == status.name }
                .map(::mapRow)
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed")
        }
    }

    override suspend fun insertRepair(repair: RepairJob): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.repairJobQueries.insertRepairJob(
                id = IdGenerator.generate(),
                customer_name = repair.customerName,
                customer_phone = repair.customerPhone,
                device_model = repair.deviceModel,
                problem = repair.problem,
                estimated_cost = repair.estimatedCost,
                final_cost = repair.finalCost,
                status = repair.status.name,
                notes = repair.notes,
                created_at = now,
                updated_at = now
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to save")
        }
    }

    override suspend fun updateStatus(id: String, status: RepairStatus): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val current = db.repairJobQueries.getRepairJobById(id).executeAsOne()
            db.repairJobQueries.updateRepairStatus(
                status = status.name,
                final_cost = current.final_cost,
                notes = current.notes,
                updated_at = now,
                id = id
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed update")
        }
    }

    override suspend fun completeRepair(id: String, finalCharge: Long): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.transaction {
                val now = Clock.System.now().toEpochMilliseconds()
                val repair = db.repairJobQueries.getRepairJobById(id).executeAsOne()

                // 1. Update Repair Status
                db.repairJobQueries.updateRepairStatus(
                    status = RepairStatus.COMPLETED.name,
                    final_cost = finalCharge.toDouble(),
                    notes = repair.notes,
                    updated_at = now,
                    id = id
                )

                // 2. Add to Ledger
                val currentCash = db.ledgerQueries.getCashBalance().executeAsOneOrNull() ?: 0.0
                val newBalance = currentCash + finalCharge
                db.ledgerQueries.updateCashBalance(newBalance, now)
                db.ledgerQueries.insertLedgerEntry(
                    id = IdGenerator.generate(),
                    type = "CREDIT",
                    amount = finalCharge.toDouble(),
                    account_type = "CASH",
                    bank_account_id = null,
                    reference_id = id,
                    description = "Repair Income: ${repair.customer_name} (${repair.device_model})",
                    balance_after = newBalance,
                    created_at = now
                )
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed completion")
        }
    }

    override suspend fun deleteRepair(id: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        // Ensure you add a deleteRepair query to your .sq file
        AppResult.Error("Delete query missing in .sq")
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.IO) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}