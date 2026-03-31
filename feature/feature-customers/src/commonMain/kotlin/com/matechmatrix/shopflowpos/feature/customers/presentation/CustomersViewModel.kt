package com.matechmatrix.shopflowpos.feature.customers.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.CollectCustomerDueUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.GetAllCustomersUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.GetCustomerSettingsUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.SaveCustomerUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.SoftDeleteCustomerUseCase
import kotlinx.datetime.Clock

class CustomersViewModel(
    private val getAllCustomers  : GetAllCustomersUseCase,
    private val getSettings     : GetCustomerSettingsUseCase,
    private val saveCustomer    : SaveCustomerUseCase,
    private val deleteCustomer  : SoftDeleteCustomerUseCase,
    private val collectDue      : CollectCustomerDueUseCase,
) : MviViewModel<CustomersState, CustomersIntent, CustomersEffect>(CustomersState()) {

    init { onIntent(CustomersIntent.Load) }

    override suspend fun handleIntent(intent: CustomersIntent) {
        when (intent) {

            CustomersIntent.Load -> loadAll()

            is CustomersIntent.Search ->
                setState { copy(searchQuery = intent.q) }

            // ── Form ──────────────────────────────────────────────────────────

            CustomersIntent.ShowAddDialog -> setState {
                copy(
                    showFormDialog = true, editingCustomer = null,
                    formName = "", formPhone = "", formWhatsapp = "", formEmail = "",
                    formCnic = "", formAddress = "", formCity = "", formCreditLimit = "",
                    formNotes = "", formError = null
                )
            }

            is CustomersIntent.ShowEditDialog -> {
                val c = intent.customer
                setState {
                    copy(
                        showFormDialog = true, editingCustomer = c,
                        formName        = c.name,
                        formPhone       = c.phone,
                        formWhatsapp    = c.whatsapp ?: "",
                        formEmail       = c.email    ?: "",
                        formCnic        = c.cnic     ?: "",
                        formAddress     = c.address,
                        formCity        = c.city,
                        formCreditLimit = if (c.creditLimit > 0) c.creditLimit.toString() else "",
                        formNotes       = c.notes    ?: "",
                        formError       = null
                    )
                }
            }

            CustomersIntent.DismissDialog ->
                setState { copy(showFormDialog = false, editingCustomer = null, formError = null) }

            CustomersIntent.SaveCustomer -> doSave()

            // ── Delete ────────────────────────────────────────────────────────

            is CustomersIntent.ConfirmDelete ->
                setState { copy(showDeleteId = intent.id.ifBlank { null }) }

            CustomersIntent.DeleteCustomer -> {
                val id = state.value.showDeleteId ?: return
                setState { copy(showDeleteId = null) }
                when (val r = deleteCustomer(id)) {
                    is AppResult.Success -> {
                        setEffect(CustomersEffect.ShowToast("Customer removed"))
                        loadAll()
                    }
                    is AppResult.Error -> setEffect(CustomersEffect.ShowToast(r.message))
                    else -> {}
                }
            }

            // ── Due Collection ────────────────────────────────────────────────

            is CustomersIntent.ShowDueDialog ->
                setState {
                    copy(
                        showDueDialog    = intent.customer,
                        dueCollectAmount = "",
                        dueAccountType   = AccountType.CASH,
                        dueAccountId     = cashAccounts.firstOrNull()?.id ?: "default_cash"
                    )
                }

            CustomersIntent.DismissDueDialog ->
                setState { copy(showDueDialog = null, dueCollectAmount = "") }

            is CustomersIntent.SetDueAmount     -> setState { copy(dueCollectAmount = intent.v) }
            is CustomersIntent.SetDueAccountType -> setState {
                // Reset account ID when type changes
                val defaultId = when (intent.v) {
                    AccountType.CASH -> cashAccounts.firstOrNull()?.id ?: "default_cash"
                    AccountType.BANK -> bankAccounts.firstOrNull()?.id ?: ""
                    else             -> ""
                }
                copy(dueAccountType = intent.v, dueAccountId = defaultId)
            }
            is CustomersIntent.SetDueAccountId  -> setState { copy(dueAccountId = intent.v) }

            CustomersIntent.CollectDue -> doCollectDue()

            // ── Form Field Bindings ───────────────────────────────────────────
            is CustomersIntent.FormName        -> setState { copy(formName        = intent.v) }
            is CustomersIntent.FormPhone       -> setState { copy(formPhone       = intent.v) }
            is CustomersIntent.FormWhatsapp    -> setState { copy(formWhatsapp    = intent.v) }
            is CustomersIntent.FormEmail       -> setState { copy(formEmail       = intent.v) }
            is CustomersIntent.FormCnic        -> setState { copy(formCnic        = intent.v) }
            is CustomersIntent.FormAddress     -> setState { copy(formAddress     = intent.v) }
            is CustomersIntent.FormCity        -> setState { copy(formCity        = intent.v) }
            is CustomersIntent.FormCreditLimit -> setState { copy(formCreditLimit = intent.v) }
            is CustomersIntent.FormNotes       -> setState { copy(formNotes       = intent.v) }
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private suspend fun loadAll() {
        setState { copy(isLoading = true) }
        val settings = getSettings()
        when (val r = getAllCustomers()) {
            is AppResult.Success -> setState {
                copy(
                    isLoading    = false,
                    customers    = r.data,
                    currencySymbol = settings.currencySymbol,
                    cashAccounts = settings.cashAccounts,
                    bankAccounts = settings.bankAccounts,
                    error        = null
                )
            }
            is AppResult.Error -> setState { copy(isLoading = false, error = r.message) }
            else               -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun doSave() {
        val s = state.value
        setState { copy(isSaving = true, formError = null) }

        val customer = Customer(
            id             = s.editingCustomer?.id ?: IdGenerator.generate(),
            name           = s.formName.trim(),
            phone          = s.formPhone.trim(),
            whatsapp       = s.formWhatsapp.takeIf { it.isNotBlank() },
            email          = s.formEmail.takeIf { it.isNotBlank() },
            cnic           = s.formCnic.takeIf { it.isNotBlank() },
            address        = s.formAddress.trim(),
            city           = s.formCity.trim(),
            creditLimit    = s.formCreditLimit.toDoubleOrNull() ?: 0.0,
            openingBalance = s.editingCustomer?.openingBalance ?: 0.0,
            outstandingBalance = s.editingCustomer?.outstandingBalance ?: 0.0,
            totalPurchases = s.editingCustomer?.totalPurchases ?: 0.0,
            totalTransactions = s.editingCustomer?.totalTransactions ?: 0,
            notes          = s.formNotes.takeIf { it.isNotBlank() },
            isActive       = true,
            createdAt      = s.editingCustomer?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
            updatedAt      = Clock.System.now().toEpochMilliseconds()
        )

        val isNew = s.editingCustomer == null
        when (val r = saveCustomer(customer, isNew)) {
            is AppResult.Success -> {
                setState { copy(isSaving = false, showFormDialog = false, editingCustomer = null) }
                setEffect(CustomersEffect.ShowToast(if (isNew) "Customer added" else "Customer updated"))
                loadAll()
            }
            is AppResult.Error -> setState { copy(isSaving = false, formError = r.message) }
            else               -> setState { copy(isSaving = false) }
        }
    }

    private suspend fun doCollectDue() {
        val s        = state.value
        val customer = s.showDueDialog ?: return
        val amount   = s.dueCollectAmount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            setState { copy(formError = "Enter a valid amount") }
            return
        }
        if (amount > customer.outstandingBalance) {
            setState { copy(formError = "Amount exceeds outstanding balance (${customer.outstandingBalance})") }
            return
        }

        setState { copy(isSaving = true) }
        when (val r = collectDue(
            customerId   = customer.id,
            customerName = customer.name,
            amount       = amount,
            accountType  = s.dueAccountType,
            accountId    = s.dueAccountId
        )) {
            is AppResult.Success -> {
                setState { copy(isSaving = false, showDueDialog = null, dueCollectAmount = "") }
                setEffect(CustomersEffect.ShowToast("Collected ${s.currencySymbol} $amount from ${customer.name}"))
                loadAll()
            }
            is AppResult.Error -> {
                setState { copy(isSaving = false) }
                setEffect(CustomersEffect.ShowToast(r.message))
            }
            else -> setState { copy(isSaving = false) }
        }
    }
}