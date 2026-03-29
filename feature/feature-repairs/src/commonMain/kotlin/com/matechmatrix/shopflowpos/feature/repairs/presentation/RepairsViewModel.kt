package com.matechmatrix.shopflowpos.feature.repairs.presentation

import RepairsEffect
import RepairsIntent
import RepairsState
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.RepairJob
import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus
import com.matechmatrix.shopflowpos.feature.repairs.domain.repository.RepairsRepository
import kotlinx.datetime.Clock


class RepairsViewModel(private val repo: RepairsRepository) :
    MviViewModel<RepairsState, RepairsIntent, RepairsEffect>(RepairsState()) {

    init { onIntent(RepairsIntent.Load) }

    override suspend fun handleIntent(intent: RepairsIntent) {
        when (intent) {
            RepairsIntent.Load -> load()
            is RepairsIntent.SetFilter -> setState { copy(statusFilter = intent.status) }
            RepairsIntent.ShowAddDialog -> setState {
                copy(showAddDialog = true, formCustomerName = "", formCustomerPhone = "",
                    formDeviceType = "", formDeviceModel = "", formIssue = "",
                    formEstimatedCost = "", formAdvance = "0", formNotes = "", formError = null)
            }
            RepairsIntent.DismissDialog -> setState { copy(showAddDialog = false) }
            RepairsIntent.SaveRepair -> save()
            is RepairsIntent.UpdateStatus -> {
                repo.updateStatus(intent.id, intent.status)
                setEffect(RepairsEffect.Toast("Status updated to ${intent.status.name}"))
                load()
            }
            is RepairsIntent.ShowCompleteDialog -> setState {
                val est = intent.repair.estimatedCost
                copy(showCompleteDialog = intent.repair, finalChargeInput = est?.toString() ?: "")
            }
            RepairsIntent.DismissCompleteDialog -> setState { copy(showCompleteDialog = null) }
            is RepairsIntent.SetFinalCharge -> setState { copy(finalChargeInput = intent.v) }
            RepairsIntent.CompleteRepair -> {
                val r = state.value.showCompleteDialog ?: return
                val charge = state.value.finalChargeInput.toLongOrNull() ?: 0L
                repo.completeRepair(r.id, charge)
                setState { copy(showCompleteDialog = null) }
                setEffect(RepairsEffect.Toast("Repair marked complete"))
                load()
            }
            is RepairsIntent.DeleteRepair -> { repo.deleteRepair(intent.id); load() }
            is RepairsIntent.FormCustomerName  -> setState { copy(formCustomerName = intent.v) }
            is RepairsIntent.FormCustomerPhone -> setState { copy(formCustomerPhone = intent.v) }
            is RepairsIntent.FormDeviceType    -> setState { copy(formDeviceType = intent.v) }
            is RepairsIntent.FormDeviceModel   -> setState { copy(formDeviceModel = intent.v) }
            is RepairsIntent.FormIssue         -> setState { copy(formIssue = intent.v) }
            is RepairsIntent.FormEstimatedCost -> setState { copy(formEstimatedCost = intent.v) }
            is RepairsIntent.FormAdvance       -> setState { copy(formAdvance = intent.v) }
            is RepairsIntent.FormNotes         -> setState { copy(formNotes = intent.v) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val list = (repo.getAllRepairs() as? AppResult.Success)?.data ?: emptyList()
        setState { copy(isLoading = false, repairs = list.sortedByDescending { it.createdAt }, currencySymbol = currency) }
    }

    private suspend fun save() {
        val s = state.value
        if (s.formCustomerName.isBlank()) { setState { copy(formError = "Customer name required") }; return }
        if (s.formDeviceModel.isBlank()) { setState { copy(formError = "Device model required") }; return }
        if (s.formIssue.isBlank()) { setState { copy(formError = "Problem description required") }; return }

        val now = Clock.System.now().toEpochMilliseconds()

        val repair = RepairJob(
            id = IdGenerator.generate(),
            customerName = s.formCustomerName.trim(),
            customerPhone = s.formCustomerPhone.trim(),
            deviceModel = s.formDeviceModel.trim(),
            problem = s.formIssue.trim(),
            estimatedCost = s.formEstimatedCost.toDoubleOrNull() ?: 0.0,
            finalCost = null,
            status = RepairStatus.PENDING,
            notes = s.formNotes.takeIf { it.isNotBlank() },
            createdAt = now,
            updatedAt = now
        )

        when (val r = repo.insertRepair(repair)) {
            is AppResult.Success -> {
                setState { copy(showAddDialog = false) }
                setEffect(RepairsEffect.Toast("Repair job created"))
                load()
            }
            is AppResult.Error -> setState { copy(formError = r.message) }
            else -> {}
        }
    }
}