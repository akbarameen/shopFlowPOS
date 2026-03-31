package com.matechmatrix.shopflowpos.feature.installments.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.InstallmentPlan
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.InstallmentFrequency
import com.matechmatrix.shopflowpos.feature.installments.domain.usecase.*
import kotlinx.datetime.Clock

class InstallmentsViewModel(
    private val getAllPlans    : GetAllPlansUseCase,
    private val getSettings   : GetInstallmentSettingsUseCase,
    private val createPlan    : CreateInstallmentPlanUseCase,
    private val recordPayment : RecordInstallmentPaymentUseCase,
    private val getPlanPayments: GetPlanPaymentsUseCase,
) : MviViewModel<InstallmentsState, InstallmentsIntent, InstallmentsEffect>(InstallmentsState()) {

    init { onIntent(InstallmentsIntent.Load) }

    override suspend fun handleIntent(intent: InstallmentsIntent) {
        when (intent) {
            InstallmentsIntent.Load -> loadAll()

            is InstallmentsIntent.ToggleShowCompleted ->
                setState { copy(showCompleted = intent.show) }

            // ── Add dialog ────────────────────────────────────────────────────
            InstallmentsIntent.ShowAddDialog -> setState {
                copy(
                    showAddDialog = true, selectedCustomer = null,
                    formCustomerName = "", formCustomerPhone = "", formProductName = "",
                    formImei = "", formTotalAmount = "", formDownPayment = "0",
                    formInstallments = "6", formFrequency = InstallmentFrequency.MONTHLY,
                    formNotes = "", formError = null
                )
            }
            InstallmentsIntent.DismissAddDialog ->
                setState { copy(showAddDialog = false, formError = null) }

            is InstallmentsIntent.SelectCustomer -> setState {
                copy(
                    selectedCustomer  = intent.c,
                    formCustomerName  = intent.c?.name  ?: "",
                    formCustomerPhone = intent.c?.phone ?: ""
                )
            }
            is InstallmentsIntent.FormCustomerName  -> setState { copy(formCustomerName = intent.v) }
            is InstallmentsIntent.FormCustomerPhone -> setState { copy(formCustomerPhone = intent.v) }
            is InstallmentsIntent.FormProductName   -> setState { copy(formProductName = intent.v) }
            is InstallmentsIntent.FormImei          -> setState { copy(formImei = intent.v) }
            is InstallmentsIntent.FormTotalAmount   -> setState { copy(formTotalAmount = intent.v) }
            is InstallmentsIntent.FormDownPayment   -> setState { copy(formDownPayment = intent.v) }
            is InstallmentsIntent.FormInstallments  -> setState { copy(formInstallments = intent.v) }
            is InstallmentsIntent.FormFrequency     -> setState { copy(formFrequency = intent.v) }
            is InstallmentsIntent.FormNotes         -> setState { copy(formNotes = intent.v) }

            InstallmentsIntent.SavePlan -> doCreatePlan()

            // ── Payment ────────────────────────────────────────────────────────
            is InstallmentsIntent.ShowPaymentDialog -> setState {
                val defaultCash = cashAccounts.firstOrNull()?.id ?: "default_cash"
                copy(
                    paymentPlan        = intent.plan,
                    paymentAmount      = "%.0f".format(intent.plan.installmentAmount),
                    paymentAccountType = AccountType.CASH,
                    paymentAccountId   = defaultCash
                )
            }
            InstallmentsIntent.DismissPaymentDialog ->
                setState { copy(paymentPlan = null, paymentAmount = "") }

            is InstallmentsIntent.SetPaymentAmount      -> setState { copy(paymentAmount = intent.v) }
            is InstallmentsIntent.SetPaymentAccountType -> setState {
                val defaultId = when (intent.v) {
                    AccountType.CASH -> cashAccounts.firstOrNull()?.id ?: "default_cash"
                    AccountType.BANK -> bankAccounts.firstOrNull()?.id ?: ""
                    else             -> ""
                }
                copy(paymentAccountType = intent.v, paymentAccountId = defaultId)
            }
            is InstallmentsIntent.SetPaymentAccountId   -> setState { copy(paymentAccountId = intent.v) }

            InstallmentsIntent.RecordPayment -> doRecordPayment()

            // ── Detail ─────────────────────────────────────────────────────────
            is InstallmentsIntent.ShowDetail -> {
                setState { copy(detailPlan = intent.plan, detailPayments = emptyList()) }
                val r = getPlanPayments(intent.plan.id)
                if (r is AppResult.Success) setState { copy(detailPayments = r.data) }
            }
            InstallmentsIntent.DismissDetail -> setState { copy(detailPlan = null, detailPayments = emptyList()) }
        }
    }

    private suspend fun loadAll() {
        setState { copy(isLoading = true) }
        val settings = getSettings()
        when (val r = getAllPlans()) {
            is AppResult.Success -> setState {
                copy(
                    isLoading      = false,
                    plans          = r.data.sortedBy { it.nextDueDate },
                    currencySymbol = settings.currencySymbol,
                    customers      = settings.customers,
                    cashAccounts   = settings.cashAccounts,
                    bankAccounts   = settings.bankAccounts,
                    error          = null
                )
            }
            is AppResult.Error -> setState { copy(isLoading = false, error = r.message) }
            else               -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun doCreatePlan() {
        val s = state.value
        setState { copy(isSaving = true, formError = null) }

        val now          = Clock.System.now().toEpochMilliseconds()
        val total        = s.formTotalAmount.toDoubleOrNull() ?: 0.0
        val downPayment  = s.formDownPayment.toDoubleOrNull() ?: 0.0
        val installments = s.formInstallments.toIntOrNull() ?: 1
        val financed     = (total - downPayment).coerceAtLeast(0.0)
        val monthly      = if (installments > 0) financed / installments else 0.0

        val plan = InstallmentPlan(
            id                = IdGenerator.generate(),
            planNumber        = "",  // generated in repo
            saleId            = null,
            customerId        = s.selectedCustomer?.id,
            customerName      = s.formCustomerName.trim().ifBlank { s.selectedCustomer?.name ?: "" },
            customerPhone     = s.formCustomerPhone.trim().ifBlank { s.selectedCustomer?.phone ?: "" },
            customerCnic      = s.selectedCustomer?.cnic,
            customerAddress   = s.selectedCustomer?.address ?: "",
            productId         = "STANDALONE",
            productName       = s.formProductName.trim(),
            imei              = s.formImei.takeIf { it.isNotBlank() },
            totalAmount       = total,
            downPayment       = downPayment,
            financedAmount    = financed,
            installmentAmount = monthly,
            totalInstallments = installments,
            paidInstallments  = 0,
            paidAmount        = downPayment,
            remainingAmount   = financed,
            frequency         = s.formFrequency,
            startDate         = now,
            nextDueDate       = now + frequencyMs(s.formFrequency),
            notes             = s.formNotes.takeIf { it.isNotBlank() },
            createdAt         = now,
            updatedAt         = now
        )

        when (val r = createPlan(plan)) {
            is AppResult.Success -> {
                setState { copy(isSaving = false, showAddDialog = false) }
                setEffect(InstallmentsEffect.ShowToast("Installment plan created"))
                loadAll()
            }
            is AppResult.Error -> setState { copy(isSaving = false, formError = r.message) }
            else               -> setState { copy(isSaving = false) }
        }
    }

    private suspend fun doRecordPayment() {
        val s    = state.value
        val plan = s.paymentPlan ?: return
        val amt  = s.paymentAmount.toDoubleOrNull()

        if (amt == null || amt <= 0) {
            setEffect(InstallmentsEffect.ShowToast("Enter a valid amount"))
            return
        }

        setState { copy(isPaymentProcessing = true) }
        when (val r = recordPayment(plan, amt, s.paymentAccountType, s.paymentAccountId)) {
            is AppResult.Success -> {
                setState { copy(isPaymentProcessing = false, paymentPlan = null, paymentAmount = "") }
                setEffect(InstallmentsEffect.ShowToast("Payment of ${s.currencySymbol} ${amt.toLong()} recorded"))
                loadAll()
            }
            is AppResult.Error -> {
                setState { copy(isPaymentProcessing = false) }
                setEffect(InstallmentsEffect.ShowToast(r.message))
            }
            else -> setState { copy(isPaymentProcessing = false) }
        }
    }

    private fun frequencyMs(freq: InstallmentFrequency): Long = when (freq) {
        InstallmentFrequency.WEEKLY    -> 7L  * 24 * 60 * 60 * 1000
        InstallmentFrequency.BIWEEKLY  -> 14L * 24 * 60 * 60 * 1000
        InstallmentFrequency.MONTHLY   -> 30L * 24 * 60 * 60 * 1000
        InstallmentFrequency.QUARTERLY -> 90L * 24 * 60 * 60 * 1000
    }
}