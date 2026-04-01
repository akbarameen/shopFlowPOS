package com.matechmatrix.shopflowpos.feature.dues.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.dues.domain.repository.DuesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DuesRepositoryImpl(private val db: DatabaseProvider) : DuesRepository {

    // ── Mappers ───────────────────────────────────────────────────────────────
    private fun mapCustomer(r: com.matechmatrix.shopflowpos.db.Customer) = Customer(
        id = r.id, name = r.name, phone = r.phone, whatsapp = r.whatsapp, cnic = r.cnic,
        email = r.email, address = r.address, city = r.city, creditLimit = r.credit_limit,
        openingBalance = r.opening_balance, outstandingBalance = r.outstanding_balance,
        totalPurchases = r.total_purchases, totalTransactions = r.total_transactions.toInt(),
        notes = r.notes, isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapSupplier(r: com.matechmatrix.shopflowpos.db.Supplier) = Supplier(
        id = r.id, name = r.name, phone = r.phone, whatsapp = r.whatsapp,
        email = r.email, address = r.address, city = r.city, ntn = r.ntn,
        openingBalance = r.opening_balance, outstandingBalance = r.outstanding_balance,
        totalPurchased = r.total_purchased, notes = r.notes,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapSale(r: com.matechmatrix.shopflowpos.db.Sale) = Sale(
        id = r.id, invoiceNumber = r.invoice_number, customerId = r.customer_id,
        customerName = r.customer_name, customerPhone = r.customer_phone,
        customerCnic = r.customer_cnic, customerAddress = r.customer_address,
        subtotal = r.subtotal, discountAmount = r.discount_amount, taxAmount = r.tax_amount,
        totalAmount = r.total_amount, costTotal = r.cost_total, grossProfit = r.gross_profit,
        paidAmount = r.paid_amount, dueAmount = r.due_amount,
        paymentStatus = runCatching { PaymentStatus.valueOf(r.payment_status) }.getOrDefault(PaymentStatus.UNPAID),
        dueDate = r.due_date,
        status = runCatching { SaleStatus.valueOf(r.status) }.getOrDefault(SaleStatus.COMPLETED),
        notes = r.notes, soldAt = r.sold_at, updatedAt = r.updated_at
    )

    private fun mapPO(r: com.matechmatrix.shopflowpos.db.Purchase_order) = PurchaseOrder(
        id = r.id, poNumber = r.po_number, supplierId = r.supplier_id,
        supplierName = r.supplier_name, supplierPhone = r.supplier_phone,
        subtotal = r.subtotal, discountAmount = r.discount_amount, totalAmount = r.total_amount,
        paidAmount = r.paid_amount, dueAmount = r.due_amount,
        paymentStatus = runCatching { PaymentStatus.valueOf(r.payment_status) }.getOrDefault(PaymentStatus.UNPAID),
        supplierInvoiceRef = r.supplier_invoice_ref,
        goodsStatus = runCatching { GoodsStatus.valueOf(r.goods_status) }.getOrDefault(GoodsStatus.RECEIVED),
        notes = r.notes, purchasedAt = r.purchased_at, updatedAt = r.updated_at
    )

    private fun mapCash(r: com.matechmatrix.shopflowpos.db.Cash_account) = CashAccount(
        id = r.id, name = r.name, balance = r.balance, isActive = r.is_active == 1L,
        createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapBank(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id = r.id, bankName = r.bank_name, accountTitle = r.account_title,
        accountNumber = r.account_number, iban = r.iban, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    // ── Customer Receivables ──────────────────────────────────────────────────

    override suspend fun getCustomersWithDues(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.customerQueries.getCustomersWithDues().executeAsList().map(::mapCustomer))
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getSalesWithDue(customerId: String?): AppResult<List<Sale>> =
        withContext(Dispatchers.Default) {
            runCatching {
                // getSalesWithDue queries: due_amount > 0 AND status NOT IN ('RETURNED','CANCELLED')
                val rows = db.saleQueries.getSalesWithDue().executeAsList()
                val filtered = if (customerId != null) rows.filter { it.customer_id == customerId } else rows
                AppResult.Success(filtered.sortedByDescending { it.sold_at }.map(::mapSale))
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun collectSaleDue(
        sale        : Sale,
        amount      : Double,
        accountType : AccountType,
        accountId   : String
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val now   = Clock.System.now().toEpochMilliseconds()
            val payId = IdGenerator.generate()

            db.database.transaction {
                // Credit the account (money coming IN from customer)
                when (accountType) {
                    AccountType.CASH -> {
                        val acc = db.ledgerQueries.getCashAccountById(accountId).executeAsOne()
                        val nb  = acc.balance + amount
                        db.ledgerQueries.updateCashBalance(nb, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "CASH", account_id = accountId,
                            entry_type = "CREDIT", reference_type = "SALE_PAYMENT", reference_id = sale.id,
                            amount = amount, balance_after = nb,
                            description = "Due collection — ${sale.customerName} (${sale.invoiceNumber})", created_at = now
                        )
                    }
                    AccountType.BANK -> {
                        val acc = db.ledgerQueries.getBankAccountById(accountId).executeAsOne()
                        val nb  = acc.balance + amount
                        db.ledgerQueries.updateBankBalance(nb, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "BANK", account_id = accountId,
                            entry_type = "CREDIT", reference_type = "SALE_PAYMENT", reference_id = sale.id,
                            amount = amount, balance_after = nb,
                            description = "Due collection — ${sale.customerName} (${sale.invoiceNumber})", created_at = now
                        )
                    }
                    else -> rollback()
                }
                // sale_payment row
                db.saleQueries.insertSalePayment(
                    id = payId, sale_id = sale.id, amount = amount,
                    account_type = accountType.name, account_id = accountId,
                    reference_number = null, notes = "Due collection", paid_at = now
                )
                // Update sale running balance (due_amount -= amount, recalculate status)
                db.saleQueries.updateSaleOnPayment(amount = amount, now = now, saleId = sale.id)
                // Decrement customer outstanding
                if (!sale.customerId.isNullOrBlank()) {
                    db.customerQueries.decrementCustomerOutstanding(amount, now, sale.customerId!!)
                }
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Failed to collect due") }
    }

    // ── Supplier / Contact Payables ───────────────────────────────────────────

    override suspend fun getSuppliersWithDues(): AppResult<List<Supplier>> =
        withContext(Dispatchers.Default) {
            runCatching {
                // Use the master Supplier table as the primary source for balances.
                // The supplier.outstanding_balance is expected to include both opening balance and cumulative PO dues.
                val suppliers = db.supplierQueries.getSuppliersWithDues().executeAsList().map(::mapSupplier)
                AppResult.Success(suppliers)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load supplier dues") }
        }

    override suspend fun getPurchaseOrdersWithDue(supplierId: String?): AppResult<List<PurchaseOrder>> =
        withContext(Dispatchers.Default) {
            runCatching {
                // Query purchase_order for UNPAID or PARTIAL orders
                val rows = db.purchaseQueries.getUnpaidPurchaseOrders().executeAsList()
                val filtered = if (supplierId != null) rows.filter { it.supplier_id == supplierId } else rows
                AppResult.Success(filtered.sortedBy { it.purchased_at }.map(::mapPO))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load purchase dues") }
        }

    override suspend fun payPurchaseDue(
        purchaseOrder: PurchaseOrder,
        amount       : Double,
        accountType  : AccountType,
        accountId    : String
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val now   = Clock.System.now().toEpochMilliseconds()
            val payId = IdGenerator.generate()

            if (amount > purchaseOrder.dueAmount + 0.01)
                return@withContext AppResult.Error("Amount exceeds due (${purchaseOrder.dueAmount.toLong()})")

            db.database.transaction {
                // Debit the account (money going OUT to supplier)
                when (accountType) {
                    AccountType.CASH -> {
                        val acc = db.ledgerQueries.getCashAccountById(accountId).executeAsOne()
                        if (acc.balance < amount) rollback()
                        val nb = acc.balance - amount
                        db.ledgerQueries.updateCashBalance(nb, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "CASH", account_id = accountId,
                            entry_type = "DEBIT", reference_type = "PURCHASE_PAYMENT", reference_id = purchaseOrder.id,
                            amount = amount, balance_after = nb,
                            description = "Due payment — ${purchaseOrder.supplierName} (${purchaseOrder.poNumber})", created_at = now
                        )
                    }
                    AccountType.BANK -> {
                        val acc = db.ledgerQueries.getBankAccountById(accountId).executeAsOne()
                        if (acc.balance < amount) rollback()
                        val nb = acc.balance - amount
                        db.ledgerQueries.updateBankBalance(nb, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "BANK", account_id = accountId,
                            entry_type = "DEBIT", reference_type = "PURCHASE_PAYMENT", reference_id = purchaseOrder.id,
                            amount = amount, balance_after = nb,
                            description = "Due payment — ${purchaseOrder.supplierName} (${purchaseOrder.poNumber})", created_at = now
                        )
                    }
                    else -> rollback()
                }
                // Record the payment
                db.purchaseQueries.insertPurchasePayment(
                    id = payId, purchase_order_id = purchaseOrder.id, amount = amount,
                    account_type = accountType.name, account_id = accountId,
                    reference_number = null, notes = "Due payment from Dues screen", paid_at = now
                )
                // Update purchase_order.paid_amount, due_amount, payment_status
                db.purchaseQueries.updatePurchaseOrderOnPayment(amount, now, purchaseOrder.id)
                // Also try to update supplier table if the contact exists there
                try {
                    db.supplierQueries.decrementSupplierOutstanding(amount, now, purchaseOrder.supplierId)
                } catch (_: Exception) {
                    // Supplier might be a customer — no-op is fine
                }
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Failed to pay due") }
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    override suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching { AppResult.Success(db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCash)) }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching { AppResult.Success(db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBank)) }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}