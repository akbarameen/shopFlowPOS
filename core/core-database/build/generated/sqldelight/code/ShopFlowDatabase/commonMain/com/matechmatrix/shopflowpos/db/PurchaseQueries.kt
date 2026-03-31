package com.matechmatrix.shopflowpos.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class PurchaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getPurchaseOrderById(id: String, mapper: (
    id: String,
    po_number: String,
    supplier_id: String,
    supplier_name: String,
    supplier_phone: String,
    subtotal: Double,
    discount_amount: Double,
    total_amount: Double,
    paid_amount: Double,
    due_amount: Double,
    payment_status: String,
    supplier_invoice_ref: String?,
    goods_status: String,
    notes: String?,
    purchased_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = GetPurchaseOrderByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11),
      cursor.getString(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!
    )
  }

  public fun getPurchaseOrderById(id: String): Query<Purchase_order> = getPurchaseOrderById(id) {
      id_, po_number, supplier_id, supplier_name, supplier_phone, subtotal, discount_amount,
      total_amount, paid_amount, due_amount, payment_status, supplier_invoice_ref, goods_status,
      notes, purchased_at, updated_at ->
    Purchase_order(
      id_,
      po_number,
      supplier_id,
      supplier_name,
      supplier_phone,
      subtotal,
      discount_amount,
      total_amount,
      paid_amount,
      due_amount,
      payment_status,
      supplier_invoice_ref,
      goods_status,
      notes,
      purchased_at,
      updated_at
    )
  }

  public fun <T : Any> getPurchaseOrderItems(purchase_order_id: String, mapper: (
    id: String,
    purchase_order_id: String,
    product_id: String,
    product_name: String,
    imei: String?,
    quantity: Long,
    unit_cost: Double,
    total_cost: Double,
  ) -> T): Query<T> = GetPurchaseOrderItemsQuery(purchase_order_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getLong(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!
    )
  }

  public fun getPurchaseOrderItems(purchase_order_id: String): Query<Purchase_order_item> =
      getPurchaseOrderItems(purchase_order_id) { id, purchase_order_id_, product_id, product_name,
      imei, quantity, unit_cost, total_cost ->
    Purchase_order_item(
      id,
      purchase_order_id_,
      product_id,
      product_name,
      imei,
      quantity,
      unit_cost,
      total_cost
    )
  }

  public fun <T : Any> getPurchasePayments(purchase_order_id: String, mapper: (
    id: String,
    purchase_order_id: String,
    amount: Double,
    account_type: String,
    account_id: String,
    reference_number: String?,
    notes: String?,
    paid_at: Long,
  ) -> T): Query<T> = GetPurchasePaymentsQuery(purchase_order_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5),
      cursor.getString(6),
      cursor.getLong(7)!!
    )
  }

  public fun getPurchasePayments(purchase_order_id: String): Query<Purchase_payment> =
      getPurchasePayments(purchase_order_id) { id, purchase_order_id_, amount, account_type,
      account_id, reference_number, notes, paid_at ->
    Purchase_payment(
      id,
      purchase_order_id_,
      amount,
      account_type,
      account_id,
      reference_number,
      notes,
      paid_at
    )
  }

  public fun <T : Any> getAllPurchaseOrders(
    limit: Long,
    offset: Long,
    mapper: (
      id: String,
      po_number: String,
      supplier_id: String,
      supplier_name: String,
      supplier_phone: String,
      subtotal: Double,
      discount_amount: Double,
      total_amount: Double,
      paid_amount: Double,
      due_amount: Double,
      payment_status: String,
      supplier_invoice_ref: String?,
      goods_status: String,
      notes: String?,
      purchased_at: Long,
      updated_at: Long,
    ) -> T,
  ): Query<T> = GetAllPurchaseOrdersQuery(limit, offset) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11),
      cursor.getString(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!
    )
  }

  public fun getAllPurchaseOrders(limit: Long, offset: Long): Query<Purchase_order> =
      getAllPurchaseOrders(limit, offset) { id, po_number, supplier_id, supplier_name,
      supplier_phone, subtotal, discount_amount, total_amount, paid_amount, due_amount,
      payment_status, supplier_invoice_ref, goods_status, notes, purchased_at, updated_at ->
    Purchase_order(
      id,
      po_number,
      supplier_id,
      supplier_name,
      supplier_phone,
      subtotal,
      discount_amount,
      total_amount,
      paid_amount,
      due_amount,
      payment_status,
      supplier_invoice_ref,
      goods_status,
      notes,
      purchased_at,
      updated_at
    )
  }

  public fun <T : Any> getPurchaseOrdersBySupplier(
    supplier_id: String,
    limit: Long,
    offset: Long,
    mapper: (
      id: String,
      po_number: String,
      supplier_id: String,
      supplier_name: String,
      supplier_phone: String,
      subtotal: Double,
      discount_amount: Double,
      total_amount: Double,
      paid_amount: Double,
      due_amount: Double,
      payment_status: String,
      supplier_invoice_ref: String?,
      goods_status: String,
      notes: String?,
      purchased_at: Long,
      updated_at: Long,
    ) -> T,
  ): Query<T> = GetPurchaseOrdersBySupplierQuery(supplier_id, limit, offset) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11),
      cursor.getString(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!
    )
  }

  public fun getPurchaseOrdersBySupplier(
    supplier_id: String,
    limit: Long,
    offset: Long,
  ): Query<Purchase_order> = getPurchaseOrdersBySupplier(supplier_id, limit, offset) { id,
      po_number, supplier_id_, supplier_name, supplier_phone, subtotal, discount_amount,
      total_amount, paid_amount, due_amount, payment_status, supplier_invoice_ref, goods_status,
      notes, purchased_at, updated_at ->
    Purchase_order(
      id,
      po_number,
      supplier_id_,
      supplier_name,
      supplier_phone,
      subtotal,
      discount_amount,
      total_amount,
      paid_amount,
      due_amount,
      payment_status,
      supplier_invoice_ref,
      goods_status,
      notes,
      purchased_at,
      updated_at
    )
  }

  public fun <T : Any> getPurchaseOrdersByDateRange(
    purchased_at: Long,
    purchased_at_: Long,
    mapper: (
      id: String,
      po_number: String,
      supplier_id: String,
      supplier_name: String,
      supplier_phone: String,
      subtotal: Double,
      discount_amount: Double,
      total_amount: Double,
      paid_amount: Double,
      due_amount: Double,
      payment_status: String,
      supplier_invoice_ref: String?,
      goods_status: String,
      notes: String?,
      purchased_at: Long,
      updated_at: Long,
    ) -> T,
  ): Query<T> = GetPurchaseOrdersByDateRangeQuery(purchased_at, purchased_at_) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11),
      cursor.getString(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!
    )
  }

  public fun getPurchaseOrdersByDateRange(purchased_at: Long, purchased_at_: Long):
      Query<Purchase_order> = getPurchaseOrdersByDateRange(purchased_at, purchased_at_) { id,
      po_number, supplier_id, supplier_name, supplier_phone, subtotal, discount_amount,
      total_amount, paid_amount, due_amount, payment_status, supplier_invoice_ref, goods_status,
      notes, purchased_at__, updated_at ->
    Purchase_order(
      id,
      po_number,
      supplier_id,
      supplier_name,
      supplier_phone,
      subtotal,
      discount_amount,
      total_amount,
      paid_amount,
      due_amount,
      payment_status,
      supplier_invoice_ref,
      goods_status,
      notes,
      purchased_at__,
      updated_at
    )
  }

  public fun <T : Any> getUnpaidPurchaseOrders(mapper: (
    id: String,
    po_number: String,
    supplier_id: String,
    supplier_name: String,
    supplier_phone: String,
    subtotal: Double,
    discount_amount: Double,
    total_amount: Double,
    paid_amount: Double,
    due_amount: Double,
    payment_status: String,
    supplier_invoice_ref: String?,
    goods_status: String,
    notes: String?,
    purchased_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = Query(-387_889_206, arrayOf("purchase_order"), driver, "Purchase.sq",
      "getUnpaidPurchaseOrders", """
  |SELECT purchase_order.id, purchase_order.po_number, purchase_order.supplier_id, purchase_order.supplier_name, purchase_order.supplier_phone, purchase_order.subtotal, purchase_order.discount_amount, purchase_order.total_amount, purchase_order.paid_amount, purchase_order.due_amount, purchase_order.payment_status, purchase_order.supplier_invoice_ref, purchase_order.goods_status, purchase_order.notes, purchase_order.purchased_at, purchase_order.updated_at FROM purchase_order
  |WHERE payment_status IN ('UNPAID', 'PARTIAL')
  |ORDER BY purchased_at ASC
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11),
      cursor.getString(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!
    )
  }

  public fun getUnpaidPurchaseOrders(): Query<Purchase_order> = getUnpaidPurchaseOrders { id,
      po_number, supplier_id, supplier_name, supplier_phone, subtotal, discount_amount,
      total_amount, paid_amount, due_amount, payment_status, supplier_invoice_ref, goods_status,
      notes, purchased_at, updated_at ->
    Purchase_order(
      id,
      po_number,
      supplier_id,
      supplier_name,
      supplier_phone,
      subtotal,
      discount_amount,
      total_amount,
      paid_amount,
      due_amount,
      payment_status,
      supplier_invoice_ref,
      goods_status,
      notes,
      purchased_at,
      updated_at
    )
  }

  public fun getTotalPurchasedByDateRange(purchased_at: Long, purchased_at_: Long): Query<Double> =
      GetTotalPurchasedByDateRangeQuery(purchased_at, purchased_at_) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getTotalPaidToPurchasesByDateRange(paid_at: Long, paid_at_: Long): Query<Double> =
      GetTotalPaidToPurchasesByDateRangeQuery(paid_at, paid_at_) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getTotalSupplierDues(): Query<Double> = Query(-430_201_232, arrayOf("purchase_order"),
      driver, "Purchase.sq", "getTotalSupplierDues", """
  |SELECT COALESCE(SUM(due_amount), 0.0) FROM purchase_order
  |WHERE payment_status IN ('UNPAID', 'PARTIAL')
  """.trimMargin()) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun insertPurchaseOrder(
    id: String,
    po_number: String,
    supplier_id: String,
    supplier_name: String,
    supplier_phone: String,
    subtotal: Double,
    discount_amount: Double,
    total_amount: Double,
    paid_amount: Double,
    due_amount: Double,
    payment_status: String,
    supplier_invoice_ref: String?,
    goods_status: String,
    notes: String?,
    purchased_at: Long,
    updated_at: Long,
  ) {
    driver.execute(-1_445_486_371, """
        |INSERT INTO purchase_order (id, po_number, supplier_id, supplier_name, supplier_phone, subtotal, discount_amount, total_amount, paid_amount, due_amount, payment_status, supplier_invoice_ref, goods_status, notes, purchased_at, updated_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 16) {
          bindString(0, id)
          bindString(1, po_number)
          bindString(2, supplier_id)
          bindString(3, supplier_name)
          bindString(4, supplier_phone)
          bindDouble(5, subtotal)
          bindDouble(6, discount_amount)
          bindDouble(7, total_amount)
          bindDouble(8, paid_amount)
          bindDouble(9, due_amount)
          bindString(10, payment_status)
          bindString(11, supplier_invoice_ref)
          bindString(12, goods_status)
          bindString(13, notes)
          bindLong(14, purchased_at)
          bindLong(15, updated_at)
        }
    notifyQueries(-1_445_486_371) { emit ->
      emit("purchase_order")
    }
  }

  public fun insertPurchaseOrderItem(
    id: String,
    purchase_order_id: String,
    product_id: String,
    product_name: String,
    imei: String?,
    quantity: Long,
    unit_cost: Double,
    total_cost: Double,
  ) {
    driver.execute(-1_051_403_888, """
        |INSERT INTO purchase_order_item (id, purchase_order_id, product_id, product_name, imei, quantity, unit_cost, total_cost)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 8) {
          bindString(0, id)
          bindString(1, purchase_order_id)
          bindString(2, product_id)
          bindString(3, product_name)
          bindString(4, imei)
          bindLong(5, quantity)
          bindDouble(6, unit_cost)
          bindDouble(7, total_cost)
        }
    notifyQueries(-1_051_403_888) { emit ->
      emit("purchase_order_item")
    }
  }

  public fun insertPurchasePayment(
    id: String,
    purchase_order_id: String,
    amount: Double,
    account_type: String,
    account_id: String,
    reference_number: String?,
    notes: String?,
    paid_at: Long,
  ) {
    driver.execute(-1_417_534_507, """
        |INSERT INTO purchase_payment (id, purchase_order_id, amount, account_type, account_id, reference_number, notes, paid_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 8) {
          bindString(0, id)
          bindString(1, purchase_order_id)
          bindDouble(2, amount)
          bindString(3, account_type)
          bindString(4, account_id)
          bindString(5, reference_number)
          bindString(6, notes)
          bindLong(7, paid_at)
        }
    notifyQueries(-1_417_534_507) { emit ->
      emit("purchase_payment")
    }
  }

  public fun updatePurchaseOrderOnPayment(
    amount: Double,
    now: Long,
    purchaseOrderId: String,
  ) {
    driver.execute(157_541_594, """
        |UPDATE purchase_order
        |SET paid_amount    = paid_amount + ?,
        |    due_amount     = ROUND(total_amount - paid_amount - ?, 2),
        |    payment_status = CASE
        |        WHEN (paid_amount + ?) >= total_amount THEN 'PAID'
        |        WHEN (paid_amount + ?) > 0             THEN 'PARTIAL'
        |        ELSE 'UNPAID'
        |    END,
        |    updated_at = ?
        |WHERE id = ?
        """.trimMargin(), 6) {
          bindDouble(0, amount)
          bindDouble(1, amount)
          bindDouble(2, amount)
          bindDouble(3, amount)
          bindLong(4, now)
          bindString(5, purchaseOrderId)
        }
    notifyQueries(157_541_594) { emit ->
      emit("purchase_order")
    }
  }

  public fun updatePurchaseGoodsStatus(
    goods_status: String,
    updated_at: Long,
    id: String,
  ) {
    driver.execute(-1_232_757_465,
        """UPDATE purchase_order SET goods_status = ?, updated_at = ? WHERE id = ?""", 3) {
          bindString(0, goods_status)
          bindLong(1, updated_at)
          bindString(2, id)
        }
    notifyQueries(-1_232_757_465) { emit ->
      emit("purchase_order")
    }
  }

  private inner class GetPurchaseOrderByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_order", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_order", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_339_197_536,
        """SELECT purchase_order.id, purchase_order.po_number, purchase_order.supplier_id, purchase_order.supplier_name, purchase_order.supplier_phone, purchase_order.subtotal, purchase_order.discount_amount, purchase_order.total_amount, purchase_order.paid_amount, purchase_order.due_amount, purchase_order.payment_status, purchase_order.supplier_invoice_ref, purchase_order.goods_status, purchase_order.notes, purchase_order.purchased_at, purchase_order.updated_at FROM purchase_order WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "Purchase.sq:getPurchaseOrderById"
  }

  private inner class GetPurchaseOrderItemsQuery<out T : Any>(
    public val purchase_order_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_order_item", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_order_item", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_428_206_350,
        """SELECT purchase_order_item.id, purchase_order_item.purchase_order_id, purchase_order_item.product_id, purchase_order_item.product_name, purchase_order_item.imei, purchase_order_item.quantity, purchase_order_item.unit_cost, purchase_order_item.total_cost FROM purchase_order_item WHERE purchase_order_id = ?""",
        mapper, 1) {
      bindString(0, purchase_order_id)
    }

    override fun toString(): String = "Purchase.sq:getPurchaseOrderItems"
  }

  private inner class GetPurchasePaymentsQuery<out T : Any>(
    public val purchase_order_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_payment", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_payment", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_193_868_531,
        """SELECT purchase_payment.id, purchase_payment.purchase_order_id, purchase_payment.amount, purchase_payment.account_type, purchase_payment.account_id, purchase_payment.reference_number, purchase_payment.notes, purchase_payment.paid_at FROM purchase_payment WHERE purchase_order_id = ? ORDER BY paid_at ASC""",
        mapper, 1) {
      bindString(0, purchase_order_id)
    }

    override fun toString(): String = "Purchase.sq:getPurchasePayments"
  }

  private inner class GetAllPurchaseOrdersQuery<out T : Any>(
    public val limit: Long,
    public val offset: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_order", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_order", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-616_332_440,
        """SELECT purchase_order.id, purchase_order.po_number, purchase_order.supplier_id, purchase_order.supplier_name, purchase_order.supplier_phone, purchase_order.subtotal, purchase_order.discount_amount, purchase_order.total_amount, purchase_order.paid_amount, purchase_order.due_amount, purchase_order.payment_status, purchase_order.supplier_invoice_ref, purchase_order.goods_status, purchase_order.notes, purchase_order.purchased_at, purchase_order.updated_at FROM purchase_order ORDER BY purchased_at DESC LIMIT ? OFFSET ?""",
        mapper, 2) {
      bindLong(0, limit)
      bindLong(1, offset)
    }

    override fun toString(): String = "Purchase.sq:getAllPurchaseOrders"
  }

  private inner class GetPurchaseOrdersBySupplierQuery<out T : Any>(
    public val supplier_id: String,
    public val limit: Long,
    public val offset: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_order", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_order", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(298_441_704, """
    |SELECT purchase_order.id, purchase_order.po_number, purchase_order.supplier_id, purchase_order.supplier_name, purchase_order.supplier_phone, purchase_order.subtotal, purchase_order.discount_amount, purchase_order.total_amount, purchase_order.paid_amount, purchase_order.due_amount, purchase_order.payment_status, purchase_order.supplier_invoice_ref, purchase_order.goods_status, purchase_order.notes, purchase_order.purchased_at, purchase_order.updated_at FROM purchase_order
    |WHERE supplier_id = ?
    |ORDER BY purchased_at DESC
    |LIMIT ? OFFSET ?
    """.trimMargin(), mapper, 3) {
      bindString(0, supplier_id)
      bindLong(1, limit)
      bindLong(2, offset)
    }

    override fun toString(): String = "Purchase.sq:getPurchaseOrdersBySupplier"
  }

  private inner class GetPurchaseOrdersByDateRangeQuery<out T : Any>(
    public val purchased_at: Long,
    public val purchased_at_: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_order", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_order", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(423_188_371, """
    |SELECT purchase_order.id, purchase_order.po_number, purchase_order.supplier_id, purchase_order.supplier_name, purchase_order.supplier_phone, purchase_order.subtotal, purchase_order.discount_amount, purchase_order.total_amount, purchase_order.paid_amount, purchase_order.due_amount, purchase_order.payment_status, purchase_order.supplier_invoice_ref, purchase_order.goods_status, purchase_order.notes, purchase_order.purchased_at, purchase_order.updated_at FROM purchase_order
    |WHERE purchased_at BETWEEN ? AND ?
    |ORDER BY purchased_at DESC
    """.trimMargin(), mapper, 2) {
      bindLong(0, purchased_at)
      bindLong(1, purchased_at_)
    }

    override fun toString(): String = "Purchase.sq:getPurchaseOrdersByDateRange"
  }

  private inner class GetTotalPurchasedByDateRangeQuery<out T : Any>(
    public val purchased_at: Long,
    public val purchased_at_: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_order", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_order", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_790_720_154, """
    |SELECT COALESCE(SUM(total_amount), 0.0) FROM purchase_order
    |WHERE purchased_at BETWEEN ? AND ?
    """.trimMargin(), mapper, 2) {
      bindLong(0, purchased_at)
      bindLong(1, purchased_at_)
    }

    override fun toString(): String = "Purchase.sq:getTotalPurchasedByDateRange"
  }

  private inner class GetTotalPaidToPurchasesByDateRangeQuery<out T : Any>(
    public val paid_at: Long,
    public val paid_at_: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("purchase_payment", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("purchase_payment", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_649_120_882, """
    |SELECT COALESCE(SUM(amount), 0.0) FROM purchase_payment
    |WHERE paid_at BETWEEN ? AND ?
    """.trimMargin(), mapper, 2) {
      bindLong(0, paid_at)
      bindLong(1, paid_at_)
    }

    override fun toString(): String = "Purchase.sq:getTotalPaidToPurchasesByDateRange"
  }
}
