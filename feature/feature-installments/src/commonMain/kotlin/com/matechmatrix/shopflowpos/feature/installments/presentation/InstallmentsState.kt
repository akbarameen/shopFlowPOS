package com.matechmatrix.shopflowpos.feature.installments.presentation

import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.InstallmentFrequency

data class InstallmentsState(
    val isLoading       : Boolean               = true,
    val plans           : List<InstallmentPlan> = emptyList(),
    val currencySymbol  : String                = "Rs.",
    val error           : String?               = null,

    // Reference data
    val customers       : List<Customer>        = emptyList(),
    val cashAccounts    : List<CashAccount>     = emptyList(),
    val bankAccounts    : List<BankAccount>     = emptyList(),

    // Filter
    val showCompleted   : Boolean               = false,

    // Add plan dialog
    val showAddDialog   : Boolean               = false,
    // Form — customer
    val selectedCustomer: Customer?             = null,
    val formCustomerName: String                = "",
    val formCustomerPhone: String               = "",
    // Form — product
    val formProductName : String                = "",
    val formImei        : String                = "",
    // Form — financials
    val formTotalAmount : String                = "",
    val formDownPayment : String                = "0",
    val formInstallments: String                = "6",
    val formFrequency   : InstallmentFrequency  = InstallmentFrequency.MONTHLY,
    val formNotes       : String                = "",
    val formError       : String?               = null,
    val isSaving        : Boolean               = false,

    // Payment dialog
    val paymentPlan     : InstallmentPlan?      = null,
    val paymentAmount   : String                = "",
    val paymentAccountType : AccountType        = AccountType.CASH,
    val paymentAccountId: String                = "default_cash",
    val isPaymentProcessing : Boolean           = false,

    // Plan detail bottom sheet
    val detailPlan      : InstallmentPlan?      = null,
    val detailPayments  : List<InstallmentPayment> = emptyList()
) {
    val filtered: List<InstallmentPlan>
        get() = if (showCompleted) plans else plans.filter { !it.isCompleted }

    val activeCount     : Int    get() = plans.count { !it.isCompleted }
    val overdueCount    : Int    get() = plans.count { !it.isCompleted && it.isDefaulted }
    val totalOutstanding: Double get() = plans.filter { !it.isCompleted }.sumOf { it.remainingAmount }
    val totalCollected  : Double get() = plans.sumOf { it.paidAmount }

    val computedInstallmentAmount: Double
        get() {
            val total   = formTotalAmount.toDoubleOrNull() ?: 0.0
            val down    = formDownPayment.toDoubleOrNull() ?: 0.0
            val months  = formInstallments.toIntOrNull()  ?: 1
            val financed = (total - down).coerceAtLeast(0.0)
            return if (months > 0) financed / months else 0.0
        }

    val selectedCashAccount: CashAccount? get() = cashAccounts.find { it.id == paymentAccountId }
    val selectedBankAccount: BankAccount? get() = bankAccounts.find { it.id == paymentAccountId }
}

sealed class InstallmentsIntent {
    data object Load                                            : InstallmentsIntent()
    data class  ToggleShowCompleted(val show: Boolean)         : InstallmentsIntent()

    // Add plan form
    data object ShowAddDialog                                   : InstallmentsIntent()
    data object DismissAddDialog                                : InstallmentsIntent()
    data class  SelectCustomer(val c: Customer?)               : InstallmentsIntent()
    data class  FormCustomerName(val v: String)                : InstallmentsIntent()
    data class  FormCustomerPhone(val v: String)               : InstallmentsIntent()
    data class  FormProductName(val v: String)                 : InstallmentsIntent()
    data class  FormImei(val v: String)                        : InstallmentsIntent()
    data class  FormTotalAmount(val v: String)                 : InstallmentsIntent()
    data class  FormDownPayment(val v: String)                 : InstallmentsIntent()
    data class  FormInstallments(val v: String)                : InstallmentsIntent()
    data class  FormFrequency(val v: InstallmentFrequency)     : InstallmentsIntent()
    data class  FormNotes(val v: String)                       : InstallmentsIntent()
    data object SavePlan                                        : InstallmentsIntent()

    // Payment
    data class  ShowPaymentDialog(val plan: InstallmentPlan)   : InstallmentsIntent()
    data object DismissPaymentDialog                            : InstallmentsIntent()
    data class  SetPaymentAmount(val v: String)                : InstallmentsIntent()
    data class  SetPaymentAccountType(val v: AccountType)      : InstallmentsIntent()
    data class  SetPaymentAccountId(val v: String)             : InstallmentsIntent()
    data object RecordPayment                                   : InstallmentsIntent()

    // Detail
    data class  ShowDetail(val plan: InstallmentPlan)          : InstallmentsIntent()
    data object DismissDetail                                   : InstallmentsIntent()
}

sealed class InstallmentsEffect {
    data class ShowToast(val message: String) : InstallmentsEffect()
}