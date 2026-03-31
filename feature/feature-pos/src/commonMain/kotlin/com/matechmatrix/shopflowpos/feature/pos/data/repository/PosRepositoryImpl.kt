// ════════════════════════════════════════════════════════════════════════════
// feature/pos/data/repository/PosRepositoryImpl.kt
// ════════════════════════════════════════════════════════════════════════════
package com.matechmatrix.shopflowpos.feature.pos.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.pos.data.paging.PosProductPagingSource
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.CompleteSaleRequest
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosRepository
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val PAGE_SIZE = 40

class PosRepositoryImpl(private val db: DatabaseProvider) : PosRepository {

    // ── Row Mappers ───────────────────────────────────────────────────────────

    private fun mapProduct(r: com.matechmatrix.shopflowpos.db.Product) = Product(
        id                = r.id,
        name              = r.name,
        brand             = r.brand,
        model             = r.model,
        imei              = r.imei,
        barcode           = r.barcode,
        category          = runCatching { ProductCategory.valueOf(r.category) }.getOrDefault(ProductCategory.OTHER),
        condition         = runCatching { ProductCondition.valueOf(r.condition_type) }.getOrDefault(ProductCondition.NEW),
        ptaStatus         = r.pta_status,
        costPrice         = r.cost_price,
        sellingPrice      = r.selling_price,
        stock             = r.stock.toInt(),
        lowStockThreshold = r.low_stock_threshold.toInt(),
        description       = r.notes,
        isActive          = r.is_active == 1L,
        createdAt         = r.created_at,
        updatedAt         = r.updated_at,
        imageUri          = r.image_uri,
        color             = r.color,
        storageGb         = r.storage_gb?.toInt(),
        ramGb             = r.ram_gb?.toInt(),
        romGb             = r.rom_gb?.toInt(),
        batteryMah        = r.battery_mah?.toInt(),
        screenSizeInch    = r.screen_size_inch?.toFloat(),
        processor         = r.processor
    )

    private fun mapCustomer(r: com.matechmatrix.shopflowpos.db.Customer) = Customer(
        id                 = r.id,
        name               = r.name,
        phone              = r.phone,
        whatsapp           = r.whatsapp,
        cnic               = r.cnic,
        email              = r.email,
        address            = r.address,
        city               = r.city,
        creditLimit        = r.credit_limit,
        openingBalance     = r.opening_balance,
        outstandingBalance = r.outstanding_balance,
        totalPurchases     = r.total_purchases,
        totalTransactions  = r.total_transactions.toInt(),
        notes              = r.notes,
        isActive           = r.is_active == 1L,
        createdAt          = r.created_at,
        updatedAt          = r.updated_at
    )

    private fun mapCashAccount(r: com.matechmatrix.shopflowpos.db.Cash_account) = CashAccount(
        id = r.id, name = r.name, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapBankAccount(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id = r.id, bankName = r.bank_name, accountTitle = r.account_title,
        accountNumber = r.account_number, iban = r.iban, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    // ── Paged Products ────────────────────────────────────────────────────────

    override fun getProductsPaged(
        query    : String,
        category : ProductCategory?
    ): Flow<PagingData<Product>> = Pager(
        config = PagingConfig(
            pageSize           = PAGE_SIZE,
            prefetchDistance   = PAGE_SIZE / 2,
            enablePlaceholders = false
        )
    ) {
        PosProductPagingSource(
            queries  = db.productQueries,
            query    = query,
            category = category,
            mapper   = ::mapProduct
        )
    }.flow.flowOn(Dispatchers.Default)

    // ── Reference Data ────────────────────────────────────────────────────────

    override suspend fun getAllCustomers(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.customerQueries.getAllActiveCustomers().executeAsList().map(::mapCustomer)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load customers") }
        }

    override suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCashAccount)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load cash accounts") }
        }

    override suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBankAccount)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load bank accounts") }
        }

    // ── Settings ──────────────────────────────────────────────────────────────

    override suspend fun getPosSettings(): PosSettings = withContext(Dispatchers.Default) {
        fun setting(key: String, default: String = "") =
            db.settingsQueries.getSetting(key).executeAsOneOrNull() ?: default

        val cashAccounts = runCatching {
            db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCashAccount)
        }.getOrDefault(emptyList())

        val bankAccounts = runCatching {
            db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBankAccount)
        }.getOrDefault(emptyList())

        PosSettings(
            shopName       = setting("shop_name", "My Shop"),
            shopAddress    = setting("shop_address"),
            shopPhone      = setting("shop_phone"),
            currencySymbol = setting("currency_symbol", "Rs."),
            taxRate        = setting("tax_rate", "0.0").toDoubleOrNull() ?: 0.0,
            cashAccounts   = cashAccounts,
            bankAccounts   = bankAccounts
        )
    }

    // ── Complete Sale (fully atomic) ──────────────────────────────────────────

    override suspend fun completeSale(
        request: CompleteSaleRequest
    ): AppResult<ReceiptData> = withContext(Dispatchers.Default) {
        runCatching {
            val now    = Clock.System.now().toEpochMilliseconds()
            val saleId = IdGenerator.generate()

            // Pre-compute financials
            val subtotal       = request.cart.sumOf { it.lineTotal }
            val discountAmount = request.discountAmount
            val afterDiscount  = (subtotal - discountAmount).coerceAtLeast(0.0)
            val taxAmount      = afterDiscount * (request.taxRate / 100.0)
            val totalAmount    = afterDiscount + taxAmount
            val costTotal      = request.cart.sumOf { it.lineCost }
            val grossProfit    = totalAmount - costTotal
            val totalPaid      = request.cashAmount + request.bankAmount
            val dueAmount      = (totalAmount - totalPaid).coerceAtLeast(0.0)

            val paymentStatus = when {
                dueAmount <= 0           -> PaymentStatus.PAID
                totalPaid > 0            -> PaymentStatus.PARTIAL
                else                     -> PaymentStatus.UNPAID
            }

            // Customer snapshot
            val customerName    = request.customer?.name    ?: request.walkinName
            val customerPhone   = request.customer?.phone   ?: request.walkinPhone
            val customerCnic    = request.customer?.cnic
            val customerAddress = request.customer?.address ?: ""

            // Collect objects we need to build ReceiptData after the transaction
            var invoiceNumber = ""
            var cashAccName   = "Cash"
            var bankAccMap    = mutableMapOf<String, String>()  // accountId -> bankName

            db.database.transaction {
                // 1. Generate invoice number atomically
                db.invoiceSequenceQueries.incrementSequence(now, "INV")
                val seqNum = db.invoiceSequenceQueries.getSequence("INV").executeAsOne()
                invoiceNumber = "INV-${seqNum.toString().padStart(5, '0')}"

                // 2. Insert sale header
                db.saleQueries.insertSale(
                    id              = saleId,
                    invoice_number  = invoiceNumber,
                    customer_id     = request.customer?.id,
                    customer_name   = customerName,
                    customer_phone  = customerPhone,
                    customer_cnic   = customerCnic,
                    customer_address = customerAddress,
                    subtotal        = subtotal,
                    discount_amount = discountAmount,
                    tax_amount      = taxAmount,
                    total_amount    = totalAmount,
                    cost_total      = costTotal,
                    gross_profit    = grossProfit,
                    paid_amount     = totalPaid,
                    due_amount      = dueAmount,
                    payment_status  = paymentStatus.name,
                    due_date        = request.dueDate,
                    status          = SaleStatus.COMPLETED.name,
                    notes           = request.notes.takeIf { it.isNotBlank() },
                    sold_at         = now,
                    updated_at      = now
                )

                // 3. Insert sale items + update stock + insert stock movements
                request.cart.forEach { item ->
                    val itemId        = IdGenerator.generate()
                    val movementId    = IdGenerator.generate()
                    val stockBefore   = item.product.stock

                    db.saleQueries.insertSaleItem(
                        id           = itemId,
                        sale_id      = saleId,
                        product_id   = item.product.id,
                        product_name = item.product.name,
                        imei         = item.product.imei,
                        category     = item.product.category.name,
                        quantity     = item.quantity.toLong(),
                        unit_cost    = item.product.costPrice,
                        unit_price   = item.product.sellingPrice,
                        discount     = item.discount,
                        line_total   = item.lineTotal
                    )

                    if (item.product.isImeiTracked) {
                        db.productQueries.markAsSold(now, item.product.id)
                        db.stockMovementQueries.insertStockMovement(
                            id             = movementId,
                            product_id     = item.product.id,
                            product_name   = item.product.name,
                            movement_type  = MovementType.SALE.name,
                            reference_type = "SALE",
                            reference_id   = saleId,
                            quantity_before = 1L,
                            quantity_change = -1L,
                            quantity_after  = 0L,
                            unit_cost       = item.product.costPrice,
                            notes           = invoiceNumber,
                            created_at      = now
                        )
                    } else {
                        db.productQueries.decrementStock(item.quantity.toLong(), now, item.product.id)
                        db.stockMovementQueries.insertStockMovement(
                            id             = movementId,
                            product_id     = item.product.id,
                            product_name   = item.product.name,
                            movement_type  = MovementType.SALE.name,
                            reference_type = "SALE",
                            reference_id   = saleId,
                            quantity_before = stockBefore.toLong(),
                            quantity_change = -item.quantity.toLong(),
                            quantity_after  = (stockBefore - item.quantity).toLong(),
                            unit_cost       = item.product.costPrice,
                            notes           = invoiceNumber,
                            created_at      = now
                        )
                    }
                }

                // 4. Cash payment
                if (request.cashAmount > 0) {
                    val cashAcc    = db.ledgerQueries.getCashAccountById(request.cashAccountId).executeAsOne()
                    val newBalance = cashAcc.balance + request.cashAmount
                    cashAccName    = cashAcc.name

                    db.ledgerQueries.updateCashBalance(newBalance, now, request.cashAccountId)

                    db.ledgerQueries.insertLedgerEntry(
                        id             = IdGenerator.generate(),
                        account_type   = "CASH",
                        account_id     = request.cashAccountId,
                        entry_type     = "CREDIT",
                        reference_type = "SALE_PAYMENT",
                        reference_id   = saleId,
                        amount         = request.cashAmount,
                        balance_after  = newBalance,
                        description    = "Sale $invoiceNumber — $customerName (Cash)",
                        created_at     = now
                    )

                    db.saleQueries.insertSalePayment(
                        id               = IdGenerator.generate(),
                        sale_id          = saleId,
                        amount           = request.cashAmount,
                        account_type     = "CASH",
                        account_id       = request.cashAccountId,
                        reference_number = invoiceNumber,
                        notes            = null,
                        paid_at          = now
                    )
                }

                // 5. Bank payment
                if (request.bankAmount > 0 && request.bankAccountId != null) {
                    val bankAcc    = db.ledgerQueries.getBankAccountById(request.bankAccountId).executeAsOne()
                    val newBalance = bankAcc.balance + request.bankAmount
                    bankAccMap[request.bankAccountId] = "${bankAcc.bank_name} - ${bankAcc.account_number}"

                    db.ledgerQueries.updateBankBalance(newBalance, now, request.bankAccountId)

                    db.ledgerQueries.insertLedgerEntry(
                        id             = IdGenerator.generate(),
                        account_type   = "BANK",
                        account_id     = request.bankAccountId,
                        entry_type     = "CREDIT",
                        reference_type = "SALE_PAYMENT",
                        reference_id   = saleId,
                        amount         = request.bankAmount,
                        balance_after  = newBalance,
                        description    = "Sale $invoiceNumber — $customerName (Bank)",
                        created_at     = now
                    )

                    db.saleQueries.insertSalePayment(
                        id               = IdGenerator.generate(),
                        sale_id          = saleId,
                        amount           = request.bankAmount,
                        account_type     = "BANK",
                        account_id       = request.bankAccountId,
                        reference_number = invoiceNumber,
                        notes            = null,
                        paid_at          = now
                    )
                }

                // 6. Update customer stats if linked
                if (request.customer != null) {
                    db.customerQueries.updateCustomerAfterSale(
                        dueAmount  = dueAmount,
                        saleTotal  = totalAmount,
                        now        = now,
                        customerId = request.customer.id
                    )
                }
            }

            // 7. Fetch settings for receipt (outside transaction)
            val settings = getPosSettings()

            val saleItems = request.cart.map { item ->
                SaleItem(
                    id          = "",    // not needed for receipt display
                    saleId      = saleId,
                    productId   = item.product.id,
                    productName = item.product.name,
                    imei        = item.product.imei,
                    category    = item.product.category.name,
                    quantity    = item.quantity,
                    unitCost    = item.product.costPrice,
                    unitPrice   = item.product.sellingPrice,
                    discount    = item.discount,
                    lineTotal   = item.lineTotal
                )
            }

            val payments = buildList {
                if (request.cashAmount > 0) add(
                    SalePayment("", saleId, request.cashAmount, AccountType.CASH, request.cashAccountId, invoiceNumber, null, now)
                )
                if (request.bankAmount > 0 && request.bankAccountId != null) add(
                    SalePayment("", saleId, request.bankAmount, AccountType.BANK, request.bankAccountId, invoiceNumber, null, now)
                )
            }

            AppResult.Success(
                ReceiptData(
                    saleId         = saleId,
                    invoiceNumber  = invoiceNumber,
                    soldAt         = now,
                    customerId     = request.customer?.id,
                    customerName   = customerName,
                    customerPhone  = customerPhone,
                    customerCnic   = customerCnic,
                    customerAddress = customerAddress,
                    items          = saleItems,
                    payments       = payments,
                    subtotal       = subtotal,
                    discountAmount = discountAmount,
                    taxAmount      = taxAmount,
                    totalAmount    = totalAmount,
                    costTotal      = costTotal,
                    grossProfit    = grossProfit,
                    paidAmount     = totalPaid,
                    dueAmount      = dueAmount,
                    paymentStatus  = paymentStatus,
                    notes          = request.notes.takeIf { it.isNotBlank() },
                    shopName       = settings.shopName,
                    shopAddress    = settings.shopAddress,
                    shopPhone      = settings.shopPhone,
                    currencySymbol = settings.currencySymbol,
                    cashAccountName = cashAccName,
                    bankAccountNames = bankAccMap
                )
            )
        }.getOrElse { e ->
            AppResult.Error(e.message ?: "Failed to complete sale")
        }
    }
}