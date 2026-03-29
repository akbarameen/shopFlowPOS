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
    val formDeviceType: String = "",
    val formDeviceModel: String = "",
    val formIssue: String = "",
    val formEstimatedCost: String = "",
    val formAdvance: String = "0",
    val formNotes: String = "",
    val formError: String? = null
) {
    val filtered get() = if (statusFilter == null) repairs else repairs.filter { it.status == statusFilter }
    val pendingCount   get() = repairs.count { it.status == RepairStatus.PENDING }
    val inProgressCount get() = repairs.count { it.status == RepairStatus.IN_PROGRESS }
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
    data class FormDeviceType(val v: String) : RepairsIntent()
    data class FormDeviceModel(val v: String) : RepairsIntent()
    data class FormIssue(val v: String) : RepairsIntent()
    data class FormEstimatedCost(val v: String) : RepairsIntent()
    data class FormAdvance(val v: String) : RepairsIntent()
    data class FormNotes(val v: String) : RepairsIntent()
}

sealed class RepairsEffect { data class Toast(val msg: String) : RepairsEffect() }