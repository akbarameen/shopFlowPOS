package com.matechmatrix.shopflowpos.feature.repairs.presentation

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
                copy(
                    showAddDialog = true,
                    formCustomerName = "",
                    formCustomerPhone = "",
                    formCustomerCnic = "",
                    formDeviceBrand = "",
                    formDeviceModel = "",
                    formDeviceColor = "",
                    formSerialNumber = "",
                    formImei = "",
                    formProblemDescription = "",
                    formDiagnosisNotes = "",
                    formAccessoriesReceived = "",
                    formEstimatedCost = "",
                    formAdvancePaid = "0",
                    formNotes = "",
                    formError = null
                )
            }
            RepairsIntent.DismissDialog -> setState { copy(showAddDialog = false) }
            RepairsIntent.SaveRepair -> save()
            is RepairsIntent.UpdateStatus -> {
                repo.updateStatus(intent.id, intent.status)
                setEffect(RepairsEffect.Toast("Status updated to ${intent.status.display}"))
                load()
            }
            is RepairsIntent.ShowCompleteDialog -> setState {
                copy(showCompleteDialog = intent.repair, finalChargeInput = intent.repair.estimatedCost.toString())
            }
            RepairsIntent.DismissCompleteDialog -> setState { copy(showCompleteDialog = null) }
            is RepairsIntent.SetFinalCharge -> setState { copy(finalChargeInput = intent.v) }
            RepairsIntent.CompleteRepair -> {
                val r = state.value.showCompleteDialog ?: return
                val charge = state.value.finalChargeInput.toLongOrNull() ?: 0L
                repo.completeRepair(r.id, charge)
                setState { copy(showCompleteDialog = null) }
                setEffect(RepairsEffect.Toast("Repair marked ready"))
                load()
            }
            is RepairsIntent.DeleteRepair -> { repo.deleteRepair(intent.id); load() }
            
            is RepairsIntent.FormCustomerName -> setState { copy(formCustomerName = intent.v) }
            is RepairsIntent.FormCustomerPhone -> setState { copy(formCustomerPhone = intent.v) }
            is RepairsIntent.FormCustomerCnic -> setState { copy(formCustomerCnic = intent.v) }
            is RepairsIntent.FormDeviceBrand -> setState { copy(formDeviceBrand = intent.v) }
            is RepairsIntent.FormDeviceModel -> setState { copy(formDeviceModel = intent.v) }
            is RepairsIntent.FormDeviceColor -> setState { copy(formDeviceColor = intent.v) }
            is RepairsIntent.FormSerialNumber -> setState { copy(formSerialNumber = intent.v) }
            is RepairsIntent.FormImei -> setState { copy(formImei = intent.v) }
            is RepairsIntent.FormProblemDescription -> setState { copy(formProblemDescription = intent.v) }
            is RepairsIntent.FormDiagnosisNotes -> setState { copy(formDiagnosisNotes = intent.v) }
            is RepairsIntent.FormAccessoriesReceived -> setState { copy(formAccessoriesReceived = intent.v) }
            is RepairsIntent.FormEstimatedCost -> setState { copy(formEstimatedCost = intent.v) }
            is RepairsIntent.FormAdvancePaid -> setState { copy(formAdvancePaid = intent.v) }
            is RepairsIntent.FormNotes -> setState { copy(formNotes = intent.v) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val result = repo.getAllRepairs()
        if (result is AppResult.Success) {
            setState { copy(isLoading = false, repairs = result.data.sortedByDescending { it.createdAt }, currencySymbol = currency) }
        } else if (result is AppResult.Error) {
            setState { copy(isLoading = false) }
            setEffect(RepairsEffect.Toast(result.message))
        }
    }

    private suspend fun save() {
        val s = state.value
        if (s.formCustomerName.isBlank()) { setState { copy(formError = "Customer name required") }; return }
        if (s.formDeviceModel.isBlank()) { setState { copy(formError = "Device model required") }; return }
        if (s.formProblemDescription.isBlank()) { setState { copy(formError = "Problem description required") }; return }

        val now = Clock.System.now().toEpochMilliseconds()

        val repair = RepairJob(
            id = IdGenerator.generate(),
            jobNumber = "", // Generated in repository
            customerName = s.formCustomerName.trim(),
            customerPhone = s.formCustomerPhone.trim(),
            customerCnic = s.formCustomerCnic.trim().takeIf { it.isNotBlank() },
            deviceBrand = s.formDeviceBrand.trim(),
            deviceModel = s.formDeviceModel.trim(),
            deviceColor = s.formDeviceColor.trim().takeIf { it.isNotBlank() },
            serialNumber = s.formSerialNumber.trim().takeIf { it.isNotBlank() },
            imei = s.formImei.trim().takeIf { it.isNotBlank() },
            problemDescription = s.formProblemDescription.trim(),
            diagnosisNotes = s.formDiagnosisNotes.trim().takeIf { it.isNotBlank() },
            accessoriesReceived = s.formAccessoriesReceived.trim().takeIf { it.isNotBlank() },
            estimatedCost = s.formEstimatedCost.toDoubleOrNull() ?: 0.0,
            advancePaid = s.formAdvancePaid.toDoubleOrNull() ?: 0.0,
            status = RepairStatus.RECEIVED,
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
