package com.matechmatrix.shopflowpos.feature.pos.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock

class PosRepositoryImpl(private val db: DatabaseProvider) : PosRepository {

    override suspend fun getAllProducts(): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.productQueries.getAllActive(limit = 100, offset = 0).executeAsList()
            AppResult.Success(rows.map { r ->
                Product(
                    id = r.id,
                    name = r.name,
                    brand = r.brand,
                    model = r.model,
                    imei = r.imei,
                    barcode = r.barcode,
                    category = runCatching { ProductCategory.valueOf(r.category) }.getOrDefault(ProductCategory.PHONE),
                    condition = runCatching { ProductCondition.valueOf(r.condition_type) }.getOrDefault(ProductCondition.NEW),
                    costPrice = r.cost_price,
                    sellingPrice = r.selling_price,
                    stock = r.stock.toInt(),
                    lowStockThreshold = r.low_stock_threshold.toInt(),
                    imageUri = r.image_uri,
                    description = r.notes,
                    isActive = r.is_active == 1L,
                    createdAt = r.created_at,
                    updatedAt = r.updated_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load products")
        }
    }

    override suspend fun searchProducts(query: String): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.productQueries.searchProducts(
                query = query,
                limit = 50,
                offset = 0
            ).executeAsList()
            AppResult.Success(rows.map { r ->
                Product(
                    id = r.id,
                    name = r.name,
                    brand = r.brand,
                    model = r.model,
                    imei = r.imei,
                    barcode = r.barcode,
                    category = runCatching { ProductCategory.valueOf(r.category) }.getOrDefault(ProductCategory.PHONE),
                    condition = runCatching { ProductCondition.valueOf(r.condition_type) }.getOrDefault(ProductCondition.NEW),
                    costPrice = r.cost_price,
                    sellingPrice = r.selling_price,
                    stock = r.stock.toInt(),
                    lowStockThreshold = r.low_stock_threshold.toInt(),
                    imageUri = r.image_uri,
                    description = r.notes,
                    isActive = r.is_active == 1L,
                    createdAt = r.created_at,
                    updatedAt = r.updated_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun getAllCustomers(): AppResult<List<Customer>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.customerQueries.getAllCustomers().executeAsList()
            AppResult.Success(rows.map { r ->
                Customer(
                    id = r.id,
                    name = r.name,
                    phone = r.phone,
                    address = r.address,
                    notes = r.notes,
                    totalPurchases = r.total_purchases,
                    totalTransactions = r.total_transactions.toInt(),
                    createdAt = r.created_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load customers")
        }
    }

    override suspend fun getBankAccounts(): AppResult<List<BankAccount>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.ledgerQueries.getAllBankAccounts().executeAsList()
            AppResult.Success(rows.map { r ->
                BankAccount(
                    id = r.id,
                    bankName = r.bank_name,
                    accountTitle = r.account_title,
                    accountNumber = r.account_number,
                    balance = r.balance,
                    updatedAt = r.updated_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load bank accounts")
        }
    }

    override suspend fun completeSale(
        cartItems: List<CartItem>,
        discount: Long,
        paymentMethod: PaymentMethod,
        cashAmount: Long,
        bankAmount: Long,
        bankAccountId: String?,
        customerId: String?,
        customerName: String?,
        notes: String,
        dueDate: Long?
    ): AppResult<Sale> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now()
            val nowMs = now.toEpochMilliseconds()
            val tz = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(tz).date
            val startOfDay = today.atStartOfDayIn(tz).toEpochMilliseconds()
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000

            val todaySalesCount = db.saleQueries.countTodaySales(startOfDay, endOfDay).executeAsOne().toInt()
            val saleId = IdGenerator.generate()
            val invoiceNo = IdGenerator.generateInvoiceNumber(todaySalesCount)

            val subtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity.toDouble() }
            val costTotal = cartItems.sumOf { it.product.costPrice * it.quantity.toDouble() }
            val netAmount = subtotal - discount.toDouble()
            val totalReceived = cashAmount.toDouble() + bankAmount.toDouble()
            val dueAmount = (netAmount - totalReceived).coerceAtLeast(0.0)
            val grossProfit = netAmount - costTotal

            db.saleQueries.insertSale(
                id = saleId,
                invoice_number = invoiceNo,
                customer_id = customerId,
                customer_name = customerName,
                subtotal = subtotal,
                discount = discount.toDouble(),
                total_amount = netAmount,
                cost_total = costTotal,
                gross_profit = grossProfit,
                payment_method = paymentMethod.name,
                cash_amount = cashAmount.toDouble(),
                bank_amount = bankAmount.toDouble(),
                bank_account_id = bankAccountId,
                due_amount = dueAmount,
                due_date = dueDate,
                status = SaleStatus.COMPLETED.name,
                notes = notes.takeIf { it.isNotBlank() },
                sold_at = nowMs
            )

            val saleItems = cartItems.map { item ->
                val saleItem = SaleItem(
                    id = IdGenerator.generate(),
                    saleId = saleId,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    costPrice = item.product.costPrice,
                    sellingPrice = item.product.sellingPrice,
                    discount = 0.0,
                    lineTotal = item.product.sellingPrice * item.quantity.toDouble()
                )
                db.saleQueries.insertSaleItem(
                    id = saleItem.id,
                    sale_id = saleItem.saleId,
                    product_id = saleItem.productId,
                    product_name = saleItem.productName,
                    quantity = saleItem.quantity.toLong(),
                    cost_price = saleItem.costPrice,
                    selling_price = saleItem.sellingPrice,
                    discount = saleItem.discount,
                    line_total = saleItem.lineTotal
                )
                db.productQueries.decrementStock(item.quantity.toLong(), nowMs, item.product.id)
                saleItem
            }

            // Update balances and stats
            if (cashAmount > 0) {
                val current = db.ledgerQueries.getCashBalance().executeAsOne()
                db.ledgerQueries.updateCashBalance(current + cashAmount.toDouble(), nowMs)
            }
            if (bankAmount > 0 && bankAccountId != null) {
                val acct = db.ledgerQueries.getBankAccountById(bankAccountId).executeAsOneOrNull()
                val curBal = acct?.balance ?: 0.0
                db.ledgerQueries.updateBankBalance(curBal + bankAmount.toDouble(), nowMs, bankAccountId)
            }
            
            if (customerId != null) {
                db.customerQueries.updateCustomerStats(totalReceived, customerId)
            }

            // Ledger Entries
            if (cashAmount > 0) {
                db.ledgerQueries.insertLedgerEntry(
                    id = IdGenerator.generate(),
                    type = TransactionType.SALE_CASH.name,
                    amount = cashAmount.toDouble(),
                    account_type = AccountType.CASH.name,
                    bank_account_id = null,
                    reference_id = saleId,
                    description = "Sale - $invoiceNo (Cash)",
                    balance_after = 0.0,
                    created_at = nowMs
                )
            }
            if (bankAmount > 0) {
                db.ledgerQueries.insertLedgerEntry(
                    id = IdGenerator.generate(),
                    type = TransactionType.SALE_BANK.name,
                    amount = bankAmount.toDouble(),
                    account_type = AccountType.BANK.name,
                    bank_account_id = bankAccountId,
                    reference_id = saleId,
                    description = "Sale - $invoiceNo (Bank)",
                    balance_after = 0.0,
                    created_at = nowMs
                )
            }

            AppResult.Success(
                Sale(
                    id = saleId,
                    invoiceNumber = invoiceNo,
                    customerId = customerId,
                    customerName = customerName,
                    items = saleItems,
                    subtotal = subtotal,
                    discount = discount.toDouble(),
                    totalAmount = netAmount,
                    costTotal = costTotal,
                    grossProfit = grossProfit,
                    paymentMethod = paymentMethod,
                    cashAmount = cashAmount.toDouble(),
                    bankAmount = bankAmount.toDouble(),
                    bankAccountId = bankAccountId,
                    dueAmount = dueAmount,
                    dueDate = dueDate,
                    status = SaleStatus.COMPLETED,
                    notes = notes.takeIf { it.isNotBlank() },
                    soldAt = nowMs
                )
            )
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to complete sale")
        }
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    override suspend fun getShopName(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("shop_name").executeAsOneOrNull() ?: "My Shop"
    }
}
