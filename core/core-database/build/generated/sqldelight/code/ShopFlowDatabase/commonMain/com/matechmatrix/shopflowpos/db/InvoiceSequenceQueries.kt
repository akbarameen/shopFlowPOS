package com.matechmatrix.shopflowpos.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class InvoiceSequenceQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun getSequence(prefix: String): Query<Long> = GetSequenceQuery(prefix) { cursor ->
    cursor.getLong(0)!!
  }

  public fun incrementSequence(updated_at: Long, prefix: String) {
    driver.execute(-1_153_412_804, """
        |UPDATE invoice_sequence
        |SET last_number = last_number + 1, updated_at = ?
        |WHERE prefix = ?
        """.trimMargin(), 2) {
          bindLong(0, updated_at)
          bindString(1, prefix)
        }
    notifyQueries(-1_153_412_804) { emit ->
      emit("invoice_sequence")
    }
  }

  public fun initSequence(prefix: String, updated_at: Long) {
    driver.execute(-1_302_756_219, """
        |INSERT OR IGNORE INTO invoice_sequence (prefix, last_number, updated_at)
        |VALUES (?, 0, ?)
        """.trimMargin(), 2) {
          bindString(0, prefix)
          bindLong(1, updated_at)
        }
    notifyQueries(-1_302_756_219) { emit ->
      emit("invoice_sequence")
    }
  }

  private inner class GetSequenceQuery<out T : Any>(
    public val prefix: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("invoice_sequence", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("invoice_sequence", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_606_197_027,
        """SELECT last_number FROM invoice_sequence WHERE prefix = ?""", mapper, 1) {
      bindString(0, prefix)
    }

    override fun toString(): String = "InvoiceSequence.sq:getSequence"
  }
}
