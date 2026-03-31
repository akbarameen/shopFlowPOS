package com.matechmatrix.shopflowpos.feature.repairs.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.RepairJob
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.repairs.domain.repository.RepairsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class RepairsRepositoryImpl(private val db: DatabaseProvider) : RepairsRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Repair_job) = RepairJob(
        id = r.id,
        jobNumber = r.job_number,
        customerName = r.customer_name,
        customerPhone = r.customer_phone,
        customerCnic = r.customer_cnic,
        deviceBrand = r.device_brand,
        deviceModel = r.device_model,
        deviceColor = r.device_color,
        serialNumber = r.serial_number,
        imei = r.imei,
        problemDescription = r.problem_description,
        diagnosisNotes = r.diagnosis_notes,
        accessoriesReceived = r.accessories_received,
        estimatedCost = r.estimated_cost,
        partsCost = r.parts_cost,
        labourCost = r.labour_cost,
        finalCost = r.final_cost,
        advancePaid = r.advance_paid,
        balanceDue = r.balance_due,
        paymentStatus = runCatching { PaymentStatus.valueOf(r.payment_status) }.getOrDefault(PaymentStatus.UNPAID),
        status = runCatching { RepairStatus.valueOf(r.status) }.getOrDefault(RepairStatus.RECEIVED),
        notes = r.notes,
        createdAt = r.created_at,
        updatedAt = r.updated_at
    )

    override suspend fun getAllRepairs(): AppResult<List<RepairJob>> = withContext(Dispatchers.Default) {
        try {
            val list = db.repairJobQueries.getAllRepairJobs(limit = 100, offset = 0).executeAsList().map(::mapRow)
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load repairs")
        }
    }

    override suspend fun getRepairsByStatus(status: RepairStatus): AppResult<List<RepairJob>> = withContext(Dispatchers.Default) {
        try {
            val list = db.repairJobQueries.getRepairJobsByStatus(status.name).executeAsList().map(::mapRow)
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load repairs by status")
        }
    }

    override suspend fun insertRepair(repair: RepairJob): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val todayCount = db.repairJobQueries.getRepairJobsByDateRange(0, now).executeAsList().size // Placeholder for job number sequence
            val jobNumber = "REP-${IdGenerator.shortId()}" // Better than simple counter for repairs

            db.repairJobQueries.insertRepairJob(
                id = IdGenerator.generate(),
                job_number = jobNumber,
                customer_name = repair.customerName,
                customer_phone = repair.customerPhone,
                customer_cnic = repair.customerCnic,
                device_brand = repair.deviceBrand,
                device_model = repair.deviceModel,
                device_color = repair.deviceColor,
                serial_number = repair.serialNumber,
                imei = repair.imei,
                problem_description = repair.problemDescription,
                diagnosis_notes = repair.diagnosisNotes,
                accessories_received = repair.accessoriesReceived,
                estimated_cost = repair.estimatedCost,
                parts_cost = repair.partsCost,
                labour_cost = repair.labourCost,
                final_cost = repair.finalCost,
                advance_paid = repair.advancePaid,
                balance_due = repair.finalCost - repair.advancePaid,
                payment_status = if (repair.advancePaid >= repair.finalCost && repair.finalCost > 0) "PAID" else if (repair.advancePaid > 0) "PARTIAL" else "UNPAID",
                status = repair.status.name,
                notes = repair.notes,
                created_at = now,
                updated_at = now
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to save repair job")
        }
    }

    override suspend fun updateStatus(id: String, status: RepairStatus): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val current = db.repairJobQueries.getRepairJobById(id).executeAsOne()
            db.repairJobQueries.updateRepairStatus(
                status = status.name,
                diagnosis_notes = current.diagnosis_notes,
                parts_cost = current.parts_cost,
                labour_cost = current.labour_cost,
                final_cost = current.final_cost,
                advance_paid = current.advance_paid,
   //             balance_due = current.final_cost, // updateRepairStatus handles advance_paid subtraction
                notes = current.notes,
                updated_at = now,
                id = id
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update status")
        }
    }

    override suspend fun completeRepair(id: String, finalCharge: Long): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.transaction {
                val now = Clock.System.now().toEpochMilliseconds()
                val repair = db.repairJobQueries.getRepairJobById(id).executeAsOne()

                db.repairJobQueries.updateRepairStatus(
                    status = RepairStatus.READY.name,
                    diagnosis_notes = repair.diagnosis_notes,
                    parts_cost = repair.parts_cost,
                    labour_cost = repair.labour_cost,
                    final_cost = finalCharge.toDouble(),
                    advance_paid = repair.advance_paid,
                 //   balance_due = finalCharge.toDouble(),
                    notes = repair.notes,
                    updated_at = now,
                    id = id
                )
                
                // Note: Delivery and payment are usually separate steps in the UI, 
                // but this completes the "Work" part.
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to complete repair")
        }
    }

    override suspend fun deleteRepair(id: String): AppResult<Unit> = withContext(Dispatchers.Default) {
        // Soft delete or real delete? current schema doesn't have is_active for repairs.
        // Assuming real delete for now if query exists, else Error.
        AppResult.Error("Delete query not yet implemented in .sq")
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}
