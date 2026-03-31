package com.matechmatrix.shopflowpos.feature.repairs.presentation

import com.matechmatrix.shopflowpos.core.model.RepairJob
import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus

data class RepairsState(
    val isLoading: Boolean = true,
    val repairs: List<RepairJob> = emptyList(),
    val statusFilter: RepairStatus? = null,
    val currencySymbol: String = "Rs.",
    val showAddDialog: Boolean = false,
    val showCompleteDialog: RepairJob? = null,
    val finalChargeInput: String = "",
    // Form fields
    val formCustomerName: String = "",
    val formCustomerPhone: String = "",
    val formCustomerCnic: String = "",
    val formDeviceBrand: String = "",
    val formDeviceModel: String = "",
    val formDeviceColor: String = "",
    val formSerialNumber: String = "",
    val formImei: String = "",
    val formProblemDescription: String = "",
    val formDiagnosisNotes: String = "",
    val formAccessoriesReceived: String = "",
    val formEstimatedCost: String = "",
    val formAdvancePaid: String = "0",
    val formNotes: String = "",
    val formError: String? = null
) {
    val filtered get() = if (statusFilter == null) repairs else repairs.filter { it.status == statusFilter }
    
    val receivedCount   get() = repairs.count { it.status == RepairStatus.RECEIVED }
    val diagnosingCount get() = repairs.count { it.status == RepairStatus.DIAGNOSING }
    val inRepairCount   get() = repairs.count { it.status == RepairStatus.IN_REPAIR }
    val readyCount      get() = repairs.count { it.status == RepairStatus.READY }
}

sealed class RepairsIntent {
    data object Load : RepairsIntent()
    data class SetFilter(val status: RepairStatus?) : RepairsIntent()
    data object ShowAddDialog : RepairsIntent()
    data object DismissDialog : RepairsIntent()
    data object SaveRepair : RepairsIntent()
    data class UpdateStatus(val id: String, val status: RepairStatus) : RepairsIntent()
    data class ShowCompleteDialog(val repair: RepairJob) : RepairsIntent()
    data object DismissCompleteDialog : RepairsIntent()
    data class SetFinalCharge(val v: String) : RepairsIntent()
    data object CompleteRepair : RepairsIntent()
    data class DeleteRepair(val id: String) : RepairsIntent()
    
    data class FormCustomerName(val v: String) : RepairsIntent()
    data class FormCustomerPhone(val v: String) : RepairsIntent()
    data class FormCustomerCnic(val v: String) : RepairsIntent()
    data class FormDeviceBrand(val v: String) : RepairsIntent()
    data class FormDeviceModel(val v: String) : RepairsIntent()
    data class FormDeviceColor(val v: String) : RepairsIntent()
    data class FormSerialNumber(val v: String) : RepairsIntent()
    data class FormImei(val v: String) : RepairsIntent()
    data class FormProblemDescription(val v: String) : RepairsIntent()
    data class FormDiagnosisNotes(val v: String) : RepairsIntent()
    data class FormAccessoriesReceived(val v: String) : RepairsIntent()
    data class FormEstimatedCost(val v: String) : RepairsIntent()
    data class FormAdvancePaid(val v: String) : RepairsIntent()
    data class FormNotes(val v: String) : RepairsIntent()
}

sealed class RepairsEffect { 
    data class Toast(val msg: String) : RepairsEffect() 
}
