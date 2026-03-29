import com.matechmatrix.shopflowpos.core.model.Installment
import kotlin.compareTo

data class InstallmentsState(
    val isLoading: Boolean = true,
    val installments: List<Installment> = emptyList(),
    val currencySymbol: String = "Rs.",
    val showAddDialog: Boolean = false,
    val showPaymentDialog: Installment? = null,
    val paymentAmount: String = "",
    val formCustomerName: String = "",
    val formDescription: String = "",
    val formTotalAmount: String = "",
    val formInstallmentCount: String = "3",
    val formNotes: String = "",
    val formError: String? = null
) {
    // Corrected: Uses isCompleted flag from your Installment model
    val activeCount get() = installments.count { !it.isCompleted }

    // Corrected: Uses remainingAmount from your Installment model
    // Note: sumOf for Double requires a clear return type
    val totalPending get() = installments.sumOf { it.remainingAmount }
}

sealed class InstallmentsIntent {
    data object Load : InstallmentsIntent()
    data object ShowAddDialog : InstallmentsIntent()
    data object DismissDialog : InstallmentsIntent()
    data object SaveInstallment : InstallmentsIntent()
    data class ShowPaymentDialog(val i: Installment) : InstallmentsIntent()
    data object DismissPaymentDialog : InstallmentsIntent()
    data class SetPaymentAmount(val v: String) : InstallmentsIntent()
    data object RecordPayment : InstallmentsIntent()
    data class DeleteInstallment(val id: String) : InstallmentsIntent()
    data class FormCustomerName(val v: String) : InstallmentsIntent()
    data class FormDescription(val v: String) : InstallmentsIntent()
    data class FormTotalAmount(val v: String) : InstallmentsIntent()
    data class FormInstallmentCount(val v: String) : InstallmentsIntent()
    data class FormNotes(val v: String) : InstallmentsIntent()
}

sealed class InstallmentsEffect { data class Toast(val msg: String) : InstallmentsEffect() }
