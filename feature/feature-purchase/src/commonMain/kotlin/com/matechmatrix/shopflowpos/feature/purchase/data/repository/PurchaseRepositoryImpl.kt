package com.matechmatrix.shopflowpos.feature.purchase.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSource
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSourceType
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.CreatePurchaseRequest
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseRepository
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class PurchaseRepositoryImpl(private val db: DatabaseProvider) : PurchaseRepository {

    // ── Mappers ───────────────────────────────────────────────────────────────
    private fun mapProduct(r: com.matechmatrix.shopflowpos.db.Product) = Product(
        id = r.id, name = r.name, brand = r.brand, model = r.model, imei = r.imei,
        barcode = r.barcode,
        category = runCatching { ProductCategory.valueOf(r.category) }.getOrDefault(ProductCategory.OTHER),
        condition = runCatching { ProductCondition.valueOf(r.condition_type) }.getOrDefault(
            ProductCondition.NEW
        ),
        ptaStatus = r.pta_status, costPrice = r.cost_price, sellingPrice = r.selling_price,
        stock = r.stock.toInt(), lowStockThreshold = r.low_stock_threshold.toInt(),
        description = r.notes, isActive = r.is_active == 1L,
        createdAt = r.created_at, updatedAt = r.updated_at,
        imageUri = r.image_uri, color = r.color,
        storageGb = r.storage_gb?.toInt(), ramGb = r.ram_gb?.toInt(),
        romGb = r.rom_gb?.toInt(), batteryMah = r.battery_mah?.toInt(),
        screenSizeInch = r.screen_size_inch?.toFloat(), processor = r.processor
    )

    private fun mapSupplier(r: com.matechmatrix.shopflowpos.db.Supplier) = Supplier(
        id = r.id, name = r.name, phone = r.phone, whatsapp = r.whatsapp,
        email = r.email, address = r.address, city = r.city, ntn = r.ntn,
        openingBalance = r.opening_balance, outstandingBalance = r.outstanding_balance,
        totalPurchased = r.total_purchased, notes = r.notes,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapCustomer(r: com.matechmatrix.shopflowpos.db.Customer) = Customer(
        id = r.id, name = r.name, phone = r.phone, whatsapp = r.whatsapp,
        cnic = r.cnic, email = r.email, address = r.address, city = r.city,
        creditLimit = r.credit_limit, openingBalance = r.opening_balance,
        outstandingBalance = r.outstanding_balance, totalPurchases = r.total_purchases,
        totalTransactions = r.total_transactions.toInt(), notes = r.notes,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapPO(r: com.matechmatrix.shopflowpos.db.Purchase_order) = PurchaseOrder(
        id = r.id, poNumber = r.po_number, supplierId = r.supplier_id,
        supplierName = r.supplier_name, supplierPhone = r.supplier_phone,
        subtotal = r.subtotal, discountAmount = r.discount_amount, totalAmount = r.total_amount,
        paidAmount = r.paid_amount, dueAmount = r.due_amount,
        paymentStatus = runCatching { PaymentStatus.valueOf(r.payment_status) }.getOrDefault(
            PaymentStatus.UNPAID
        ),
        supplierInvoiceRef = r.supplier_invoice_ref,
        goodsStatus = runCatching { GoodsStatus.valueOf(r.goods_status) }.getOrDefault(GoodsStatus.RECEIVED),
        notes = r.notes, purchasedAt = r.purchased_at, updatedAt = r.updated_at
    )

    private fun mapCash(r: com.matechmatrix.shopflowpos.db.Cash_account) = CashAccount(
        id = r.id, name = r.name, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapBank(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id = r.id, bankName = r.bank_name, accountTitle = r.account_title,
        accountNumber = r.account_number, iban = r.iban, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    // ── Reference data ────────────────────────────────────────────────────────

    override suspend fun getAllSuppliers(): AppResult<List<Supplier>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.supplierQueries.getAllActiveSuppliers().executeAsList().map(::mapSupplier)
                )
            }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getAllCustomers(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.customerQueries.getAllActiveCustomers().executeAsList().map(::mapCustomer)
                )
            }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun searchProducts(query: String): AppResult<List<Product>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val rows = if (query.isBlank())
                    db.productQueries.getAllActive(50L, 0L).executeAsList()
                else
                    db.productQueries.searchProducts(query, 50L, 0L).executeAsList()
                AppResult.Success(rows.map(::mapProduct))
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCash)
                )
            }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBank)
                )
            }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getAllPurchaseOrders(): AppResult<List<PurchaseOrder>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.purchaseQueries.getAllPurchaseOrders(
                        Long.MAX_VALUE,
                        0L
                    ).executeAsList().map(::mapPO)
                )
            }
                .getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    // ── Create Purchase (fully atomic) ────────────────────────────────────────
    override suspend fun createPurchaseOrder(request: CreatePurchaseRequest): AppResult<PurchaseReceiptData> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                val orderId = IdGenerator.generate()

                val subtotal = request.items.sumOf { it.totalCost }
                val totalAmount = (subtotal - request.discountAmount).coerceAtLeast(0.0)
                val totalPaid = request.cashAmount + request.bankAmount
                val dueAmount = (totalAmount - totalPaid).coerceAtLeast(0.0)
                val payStatus = when {
                    dueAmount <= 0 -> PaymentStatus.PAID
                    totalPaid > 0 -> PaymentStatus.PARTIAL
                    else -> PaymentStatus.UNPAID
                }

                var poNumber = ""
                var cashAccName = "Cash"
                val bankAccMap = mutableMapOf<String, String>()
                var resolvedContactId = request.source.id ?: ""

                db.database.transaction {

                    // ── 1. PO number ──────────────────────────────────────────
                    db.invoiceSequenceQueries.incrementSequence(now, "PO")
                    val seq = db.invoiceSequenceQueries.getSequence("PO").executeAsOne()
                    poNumber = "PO-${seq.toString().padStart(5, '0')}"

                    // ── 2. Insert new contact if needed ───────────────────────
                    if (request.source.isNew) {
                        resolvedContactId = IdGenerator.generate()
                        when (request.source.type) {
                            PurchaseSourceType.SUPPLIER -> {
                                db.supplierQueries.insertSupplier(
                                    id = resolvedContactId,
                                    name = request.source.name,
                                    phone = request.source.phone,
                                    whatsapp = null,
                                    email = request.source.email,
                                    address = request.source.address,
                                    city = request.source.city,
                                    ntn = null,
                                    opening_balance = 0.0,
                                    outstanding_balance = dueAmount,
                                    notes = null,
                                    created_at = now,
                                    updated_at = now
                                )
                            }

                            PurchaseSourceType.CUSTOMER -> {
                                db.customerQueries.insertCustomer(
                                    id = resolvedContactId,
                                    name = request.source.name,
                                    phone = request.source.phone,
                                    whatsapp = null,
                                    cnic = null,
                                    email = request.source.email.takeIf { it.isNotBlank() },
                                    address = request.source.address,
                                    city = request.source.city,
                                    credit_limit = 0.0,
                                    opening_balance = 0.0,
                                    outstanding_balance = 0.0,
                                    notes = null,
                                    created_at = now,
                                    updated_at = now
                                )
                            }
                        }
                    }

                    // ── 3. Insert purchase order ──────────────────────────────
                    db.purchaseQueries.insertPurchaseOrder(
                        id = orderId,
                        po_number = poNumber,
                        supplier_id = resolvedContactId,
                        supplier_name = request.source.name,
                        supplier_phone = request.source.phone,
                        subtotal = subtotal,
                        discount_amount = request.discountAmount,
                        total_amount = totalAmount,
                        paid_amount = totalPaid,
                        due_amount = dueAmount,
                        payment_status = payStatus.name,
                        supplier_invoice_ref = request.supplierInvoiceRef.takeIf { it.isNotBlank() },
                        goods_status = GoodsStatus.RECEIVED.name,
                        notes = request.notes.takeIf { it.isNotBlank() },
                        purchased_at = now,
                        updated_at = now
                    )

                    // ── 4. Insert items + stock + movements ───────────────────
                    request.items.forEach { item ->
                        var productId = item.productId
                        val stockBefore: Long

                        if (item.isNewProduct || productId.isBlank()) {
                            // Insert new product to inventory
                            productId = IdGenerator.generate()
                            db.productQueries.insertProduct(
                                id = productId, name = item.productName, brand = item.brand,
                                model = "", imei = item.imei, cost_price = item.unitCost,
                                selling_price = item.unitCost * 1.2,  // default 20% markup
                                stock = item.quantity.toLong(),
                                low_stock_threshold = 3L,
                                condition_type = ProductCondition.NEW.name,
                                category = item.category,
                                pta_status = "NA", image_uri = null, barcode = null,
                                notes = null, is_active = 1L, created_at = now, updated_at = now,
                                color = null, storage_gb = null, ram_gb = null, rom_gb = null,
                                battery_mah = null, screen_size_inch = null, processor = null
                            )
                            stockBefore = 0L
                        } else {
                            val existing = db.productQueries.getById(productId).executeAsOneOrNull()
                            stockBefore = existing?.stock ?: 0L
                            // Increment existing stock
                            if (item.imei.isNullOrBlank()) {
                                db.productQueries.incrementStock(
                                    item.quantity.toLong(),
                                    now,
                                    productId
                                )
                            } else {
                                db.productQueries.updateStock(1L, now, productId)
                            }
                            // Update cost price to this purchase's negotiated price
                            db.productQueries.updatePrices(
                                item.unitCost,
                                existing?.selling_price ?: item.unitCost * 1.2,
                                now,
                                productId
                            )
                        }

                        db.purchaseQueries.insertPurchaseOrderItem(
                            id = IdGenerator.generate(),
                            purchase_order_id = orderId,
                            product_id = productId,
                            product_name = item.productName,
                            imei = item.imei,
                            quantity = item.quantity.toLong(),
                            unit_cost = item.unitCost,
                            total_cost = item.totalCost
                        )

                        db.stockMovementQueries.insertStockMovement(
                            id = IdGenerator.generate(),
                            product_id = productId,
                            product_name = item.productName,
                            movement_type = MovementType.PURCHASE.name,
                            reference_type = "PURCHASE_ORDER",
                            reference_id = orderId,
                            quantity_before = stockBefore,
                            quantity_change = item.quantity.toLong(),
                            quantity_after = stockBefore + item.quantity,
                            unit_cost = item.unitCost,
                            notes = poNumber,
                            created_at = now
                        )
                    }

                    // ── 5. Cash payment ───────────────────────────────────────
                    if (request.cashAmount > 0) {
                        val cashAcc = db.ledgerQueries.getCashAccountById(request.cashAccountId)
                            .executeAsOne()
                        if (cashAcc.balance < request.cashAmount) rollback()   // enforce balance
                        val newBal = cashAcc.balance - request.cashAmount
                        cashAccName = cashAcc.name
                        db.ledgerQueries.updateCashBalance(newBal, now, request.cashAccountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "CASH",
                            account_id = request.cashAccountId, entry_type = "DEBIT",
                            reference_type = "PURCHASE_PAYMENT", reference_id = orderId,
                            amount = request.cashAmount, balance_after = newBal,
                            description = "Purchase $poNumber — ${request.source.name} (Cash)",
                            created_at = now
                        )
                        db.purchaseQueries.insertPurchasePayment(
                            id = IdGenerator.generate(), purchase_order_id = orderId,
                            amount = request.cashAmount, account_type = "CASH",
                            account_id = request.cashAccountId,
                            reference_number = poNumber, notes = null, paid_at = now
                        )
                    }

                    // ── 6. Bank payment ───────────────────────────────────────
                    if (request.bankAmount > 0 && !request.bankAccountId.isNullOrBlank()) {
                        val bankAcc = db.ledgerQueries.getBankAccountById(request.bankAccountId)
                            .executeAsOne()
                        if (bankAcc.balance < request.bankAmount) rollback()   // enforce balance
                        val newBal = bankAcc.balance - request.bankAmount
                        bankAccMap[request.bankAccountId] =
                            "${bankAcc.bank_name} · ${bankAcc.account_number}"
                        db.ledgerQueries.updateBankBalance(newBal, now, request.bankAccountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "BANK",
                            account_id = request.bankAccountId, entry_type = "DEBIT",
                            reference_type = "PURCHASE_PAYMENT", reference_id = orderId,
                            amount = request.bankAmount, balance_after = newBal,
                            description = "Purchase $poNumber — ${request.source.name} (Bank)",
                            created_at = now
                        )
                        db.purchaseQueries.insertPurchasePayment(
                            id = IdGenerator.generate(), purchase_order_id = orderId,
                            amount = request.bankAmount, account_type = "BANK",
                            account_id = request.bankAccountId,
                            reference_number = poNumber, notes = null, paid_at = now
                        )
                    }

                    // ── 7. Update purchase order paid/due ─────────────────────
                    db.purchaseQueries.updatePurchaseOrderOnPayment(totalPaid, now, orderId)

                    // ── 8. Update contact financials ──────────────────────────
                    // NOTE: This is called for ALL cases (new and existing contact)
                    // For new supplier: update the row we just inserted
                    // For existing supplier: update outstanding + total_purchased
                    if (resolvedContactId.isNotBlank()) {
                        when (request.source.type) {
                            PurchaseSourceType.SUPPLIER -> {
                                if (!request.source.isNew) {
                                    // Only call updateSupplierFinancials if NOT new
                                    // (new supplier already has outstanding_balance = dueAmount from insertSupplier)
                                    db.supplierQueries.updateSupplierFinancials(
                                        balanceDelta = dueAmount,
                                        purchaseAmount = totalAmount,
                                        now = now,
                                        supplierId = resolvedContactId
                                    )
                                } else {
                                    // For new supplier, total_purchased wasn't set during insert, fix it
                                    db.supplierQueries.updateSupplierFinancials(
                                        balanceDelta = 0.0,          // already set in insert
                                        purchaseAmount = totalAmount,
                                        now = now,
                                        supplierId = resolvedContactId
                                    )
                                }
                            }

                            PurchaseSourceType.CUSTOMER -> {
                                // Buying FROM a customer: the due goes as "payable to customer"
                                // We track this via the purchase_order table (supplier_id = customer.id)
                                // Customer's outstanding_balance is NOT affected (that tracks what they owe US)
                            }
                        }
                    }
                }

                // Build receipt
                val settings = getPurchaseSettings()
                val items = request.items.map {
                    PurchaseOrderItem(
                        "",
                        orderId,
                        it.productId,
                        it.productName,
                        it.imei,
                        it.quantity,
                        it.unitCost,
                        it.totalCost
                    )
                }
                val payments = buildList {
                    if (request.cashAmount > 0) add(
                        PurchasePayment(
                            "",
                            orderId,
                            request.cashAmount,
                            AccountType.CASH,
                            request.cashAccountId,
                            poNumber,
                            null,
                            now
                        )
                    )
                    if (request.bankAmount > 0 && !request.bankAccountId.isNullOrBlank()) add(
                        PurchasePayment(
                            "",
                            orderId,
                            request.bankAmount,
                            AccountType.BANK,
                            request.bankAccountId,
                            poNumber,
                            null,
                            now
                        )
                    )
                }

                AppResult.Success(
                    PurchaseReceiptData(
                        purchaseOrderId = orderId, poNumber = poNumber, purchasedAt = now,
                        supplierId = resolvedContactId.takeIf { it.isNotBlank() },
                        supplierName = request.source.name, supplierPhone = request.source.phone,
                        items = items, payments = payments,
                        subtotal = subtotal, discountAmount = request.discountAmount,
                        totalAmount = totalAmount, paidAmount = totalPaid, dueAmount = dueAmount,
                        paymentStatus = payStatus, goodsStatus = GoodsStatus.RECEIVED,
                        notes = request.notes.takeIf { it.isNotBlank() },
                        shopName = settings.shopName, shopAddress = settings.shopAddress,
                        shopPhone = settings.shopPhone, currencySymbol = settings.currencySymbol,
                        cashAccountName = cashAccName, bankAccountNames = bankAccMap
                    )
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to create purchase order") }
        }

    // ── Pay purchase due ──────────────────────────────────────────────────────
    override suspend fun payPurchaseDue(
        orderId: String,
        amount: Double,
        accountType: AccountType,
        accountId: String
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val now = Clock.System.now().toEpochMilliseconds()
            val po = db.purchaseQueries.getPurchaseOrderById(orderId).executeAsOneOrNull()
                ?: return@withContext AppResult.Error("Purchase order not found")

            if (amount > po.due_amount + 0.01)
                return@withContext AppResult.Error("Amount exceeds due (${po.due_amount.toLong()})")

            db.database.transaction {
                when (accountType) {
                    AccountType.CASH -> {
                        val acc = db.ledgerQueries.getCashAccountById(accountId).executeAsOne()
                        if (acc.balance < amount) rollback()
                        val nb = acc.balance - amount
                        db.ledgerQueries.updateCashBalance(nb, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(),
                            account_type = "CASH",
                            account_id = accountId,
                            entry_type = "DEBIT",
                            reference_type = "PURCHASE_PAYMENT",
                            reference_id = orderId,
                            amount = amount,
                            balance_after = nb,
                            description = "Due payment — ${po.supplier_name} (${po.po_number})",
                            created_at = now
                        )
                    }

                    AccountType.BANK -> {
                        val acc = db.ledgerQueries.getBankAccountById(accountId).executeAsOne()
                        if (acc.balance < amount) rollback()
                        val nb = acc.balance - amount
                        db.ledgerQueries.updateBankBalance(nb, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(),
                            account_type = "BANK",
                            account_id = accountId,
                            entry_type = "DEBIT",
                            reference_type = "PURCHASE_PAYMENT",
                            reference_id = orderId,
                            amount = amount,
                            balance_after = nb,
                            description = "Due payment — ${po.supplier_name} (${po.po_number})",
                            created_at = now
                        )
                    }

                    else -> rollback()
                }
                db.purchaseQueries.insertPurchasePayment(
                    id = IdGenerator.generate(), purchase_order_id = orderId,
                    amount = amount, account_type = accountType.name, account_id = accountId,
                    reference_number = null, notes = "Due payment", paid_at = now
                )
                db.purchaseQueries.updatePurchaseOrderOnPayment(amount, now, orderId)
                // Decrement supplier outstanding (works for both supplier and customer source)
                if (po.supplier_id.isNotBlank()) {
                    db.supplierQueries.decrementSupplierOutstanding(amount, now, po.supplier_id)
                }
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Payment failed") }
    }

    // ── Settings ──────────────────────────────────────────────────────────────
    override suspend fun getPurchaseSettings(): PurchaseSettings =
        withContext(Dispatchers.Default) {
            fun s(k: String, d: String = "") =
                db.settingsQueries.getSetting(k).executeAsOneOrNull() ?: d

            val cashAccounts = runCatching {
                db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCash)
            }.getOrDefault(emptyList())
            val bankAccounts = runCatching {
                db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBank)
            }.getOrDefault(emptyList())
            PurchaseSettings(
                s("shop_name", "My Shop"),
                s("shop_address"),
                s("shop_phone"),
                s("currency_symbol", "Rs."),
                cashAccounts,
                bankAccounts
            )
        }
}