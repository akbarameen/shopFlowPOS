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

public class StockMovementQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getMovementsByProduct(
    product_id: String,
    limit: Long,
    offset: Long,
    mapper: (
      id: String,
      product_id: String,
      product_name: String,
      movement_type: String,
      reference_type: String?,
      reference_id: String?,
      quantity_before: Long,
      quantity_change: Long,
      quantity_after: Long,
      unit_cost: Double?,
      notes: String?,
      created_at: Long,
    ) -> T,
  ): Query<T> = GetMovementsByProductQuery(product_id, limit, offset) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getDouble(9),
      cursor.getString(10),
      cursor.getLong(11)!!
    )
  }

  public fun getMovementsByProduct(
    product_id: String,
    limit: Long,
    offset: Long,
  ): Query<Stock_movement> = getMovementsByProduct(product_id, limit, offset) { id, product_id_,
      product_name, movement_type, reference_type, reference_id, quantity_before, quantity_change,
      quantity_after, unit_cost, notes, created_at ->
    Stock_movement(
      id,
      product_id_,
      product_name,
      movement_type,
      reference_type,
      reference_id,
      quantity_before,
      quantity_change,
      quantity_after,
      unit_cost,
      notes,
      created_at
    )
  }

  public fun <T : Any> getMovementsByDateRange(
    created_at: Long,
    created_at_: Long,
    mapper: (
      id: String,
      product_id: String,
      product_name: String,
      movement_type: String,
      reference_type: String?,
      reference_id: String?,
      quantity_before: Long,
      quantity_change: Long,
      quantity_after: Long,
      unit_cost: Double?,
      notes: String?,
      created_at: Long,
    ) -> T,
  ): Query<T> = GetMovementsByDateRangeQuery(created_at, created_at_) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getDouble(9),
      cursor.getString(10),
      cursor.getLong(11)!!
    )
  }

  public fun getMovementsByDateRange(created_at: Long, created_at_: Long): Query<Stock_movement> =
      getMovementsByDateRange(created_at, created_at_) { id, product_id, product_name,
      movement_type, reference_type, reference_id, quantity_before, quantity_change, quantity_after,
      unit_cost, notes, created_at__ ->
    Stock_movement(
      id,
      product_id,
      product_name,
      movement_type,
      reference_type,
      reference_id,
      quantity_before,
      quantity_change,
      quantity_after,
      unit_cost,
      notes,
      created_at__
    )
  }

  public fun <T : Any> getMovementsByType(
    movement_type: String,
    limit: Long,
    offset: Long,
    mapper: (
      id: String,
      product_id: String,
      product_name: String,
      movement_type: String,
      reference_type: String?,
      reference_id: String?,
      quantity_before: Long,
      quantity_change: Long,
      quantity_after: Long,
      unit_cost: Double?,
      notes: String?,
      created_at: Long,
    ) -> T,
  ): Query<T> = GetMovementsByTypeQuery(movement_type, limit, offset) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getDouble(9),
      cursor.getString(10),
      cursor.getLong(11)!!
    )
  }

  public fun getMovementsByType(
    movement_type: String,
    limit: Long,
    offset: Long,
  ): Query<Stock_movement> = getMovementsByType(movement_type, limit, offset) { id, product_id,
      product_name, movement_type_, reference_type, reference_id, quantity_before, quantity_change,
      quantity_after, unit_cost, notes, created_at ->
    Stock_movement(
      id,
      product_id,
      product_name,
      movement_type_,
      reference_type,
      reference_id,
      quantity_before,
      quantity_change,
      quantity_after,
      unit_cost,
      notes,
      created_at
    )
  }

  public fun <T : Any> getProductMovementSummary(product_id: String, mapper: (
    product_id: String,
    total_in: Long,
    total_out: Long,
  ) -> T): Query<T> = GetProductMovementSummaryQuery(product_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!
    )
  }

  public fun getProductMovementSummary(product_id: String): Query<GetProductMovementSummary> =
      getProductMovementSummary(product_id) { product_id_, total_in, total_out ->
    GetProductMovementSummary(
      product_id_,
      total_in,
      total_out
    )
  }

  public fun insertStockMovement(
    id: String,
    product_id: String,
    product_name: String,
    movement_type: String,
    reference_type: String?,
    reference_id: String?,
    quantity_before: Long,
    quantity_change: Long,
    quantity_after: Long,
    unit_cost: Double?,
    notes: String?,
    created_at: Long,
  ) {
    driver.execute(1_184_303_585, """
        |INSERT INTO stock_movement (id, product_id, product_name, movement_type, reference_type, reference_id, quantity_before, quantity_change, quantity_after, unit_cost, notes, created_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 12) {
          bindString(0, id)
          bindString(1, product_id)
          bindString(2, product_name)
          bindString(3, movement_type)
          bindString(4, reference_type)
          bindString(5, reference_id)
          bindLong(6, quantity_before)
          bindLong(7, quantity_change)
          bindLong(8, quantity_after)
          bindDouble(9, unit_cost)
          bindString(10, notes)
          bindLong(11, created_at)
        }
    notifyQueries(1_184_303_585) { emit ->
      emit("stock_movement")
    }
  }

  private inner class GetMovementsByProductQuery<out T : Any>(
    public val product_id: String,
    public val limit: Long,
    public val offset: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stock_movement", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stock_movement", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_893_246_527, """
    |SELECT stock_movement.id, stock_movement.product_id, stock_movement.product_name, stock_movement.movement_type, stock_movement.reference_type, stock_movement.reference_id, stock_movement.quantity_before, stock_movement.quantity_change, stock_movement.quantity_after, stock_movement.unit_cost, stock_movement.notes, stock_movement.created_at FROM stock_movement
    |WHERE product_id = ?
    |ORDER BY created_at DESC
    |LIMIT ? OFFSET ?
    """.trimMargin(), mapper, 3) {
      bindString(0, product_id)
      bindLong(1, limit)
      bindLong(2, offset)
    }

    override fun toString(): String = "StockMovement.sq:getMovementsByProduct"
  }

  private inner class GetMovementsByDateRangeQuery<out T : Any>(
    public val created_at: Long,
    public val created_at_: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stock_movement", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stock_movement", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-865_813_633, """
    |SELECT stock_movement.id, stock_movement.product_id, stock_movement.product_name, stock_movement.movement_type, stock_movement.reference_type, stock_movement.reference_id, stock_movement.quantity_before, stock_movement.quantity_change, stock_movement.quantity_after, stock_movement.unit_cost, stock_movement.notes, stock_movement.created_at FROM stock_movement
    |WHERE created_at BETWEEN ? AND ?
    |ORDER BY created_at DESC
    """.trimMargin(), mapper, 2) {
      bindLong(0, created_at)
      bindLong(1, created_at_)
    }

    override fun toString(): String = "StockMovement.sq:getMovementsByDateRange"
  }

  private inner class GetMovementsByTypeQuery<out T : Any>(
    public val movement_type: String,
    public val limit: Long,
    public val offset: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stock_movement", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stock_movement", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(585_807_850, """
    |SELECT stock_movement.id, stock_movement.product_id, stock_movement.product_name, stock_movement.movement_type, stock_movement.reference_type, stock_movement.reference_id, stock_movement.quantity_before, stock_movement.quantity_change, stock_movement.quantity_after, stock_movement.unit_cost, stock_movement.notes, stock_movement.created_at FROM stock_movement
    |WHERE movement_type = ?
    |ORDER BY created_at DESC
    |LIMIT ? OFFSET ?
    """.trimMargin(), mapper, 3) {
      bindString(0, movement_type)
      bindLong(1, limit)
      bindLong(2, offset)
    }

    override fun toString(): String = "StockMovement.sq:getMovementsByType"
  }

  private inner class GetProductMovementSummaryQuery<out T : Any>(
    public val product_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stock_movement", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stock_movement", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-415_205_261, """
    |SELECT
    |    product_id,
    |    COALESCE(SUM(CASE WHEN quantity_change > 0 THEN  quantity_change ELSE 0 END), 0) AS total_in,
    |    COALESCE(SUM(CASE WHEN quantity_change < 0 THEN -quantity_change ELSE 0 END), 0) AS total_out
    |FROM stock_movement
    |WHERE product_id = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, product_id)
    }

    override fun toString(): String = "StockMovement.sq:getProductMovementSummary"
  }
}
