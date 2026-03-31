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

public class CustomerQueriesQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getAllActiveCustomers(mapper: (
    id: String,
    name: String,
    phone: String,
    whatsapp: String?,
    cnic: String?,
    email: String?,
    address: String,
    city: String,
    credit_limit: Double,
    opening_balance: Double,
    outstanding_balance: Double,
    total_purchases: Double,
    total_transactions: Long,
    notes: String?,
    is_active: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = Query(1_718_594_198, arrayOf("customer"), driver, "CustomerQueries.sq",
      "getAllActiveCustomers",
      "SELECT customer.id, customer.name, customer.phone, customer.whatsapp, customer.cnic, customer.email, customer.address, customer.city, customer.credit_limit, customer.opening_balance, customer.outstanding_balance, customer.total_purchases, customer.total_transactions, customer.notes, customer.is_active, customer.created_at, customer.updated_at FROM customer WHERE is_active = 1 ORDER BY name ASC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4),
      cursor.getString(5),
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getDouble(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun getAllActiveCustomers(): Query<Customer> = getAllActiveCustomers { id, name, phone,
      whatsapp, cnic, email, address, city, credit_limit, opening_balance, outstanding_balance,
      total_purchases, total_transactions, notes, is_active, created_at, updated_at ->
    Customer(
      id,
      name,
      phone,
      whatsapp,
      cnic,
      email,
      address,
      city,
      credit_limit,
      opening_balance,
      outstanding_balance,
      total_purchases,
      total_transactions,
      notes,
      is_active,
      created_at,
      updated_at
    )
  }

  public fun <T : Any> searchCustomers(q: String, mapper: (
    id: String,
    name: String,
    phone: String,
    whatsapp: String?,
    cnic: String?,
    email: String?,
    address: String,
    city: String,
    credit_limit: Double,
    opening_balance: Double,
    outstanding_balance: Double,
    total_purchases: Double,
    total_transactions: Long,
    notes: String?,
    is_active: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = SearchCustomersQuery(q) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4),
      cursor.getString(5),
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getDouble(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun searchCustomers(q: String): Query<Customer> = searchCustomers(q) { id, name, phone,
      whatsapp, cnic, email, address, city, credit_limit, opening_balance, outstanding_balance,
      total_purchases, total_transactions, notes, is_active, created_at, updated_at ->
    Customer(
      id,
      name,
      phone,
      whatsapp,
      cnic,
      email,
      address,
      city,
      credit_limit,
      opening_balance,
      outstanding_balance,
      total_purchases,
      total_transactions,
      notes,
      is_active,
      created_at,
      updated_at
    )
  }

  public fun <T : Any> getCustomerById(id: String, mapper: (
    id: String,
    name: String,
    phone: String,
    whatsapp: String?,
    cnic: String?,
    email: String?,
    address: String,
    city: String,
    credit_limit: Double,
    opening_balance: Double,
    outstanding_balance: Double,
    total_purchases: Double,
    total_transactions: Long,
    notes: String?,
    is_active: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = GetCustomerByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4),
      cursor.getString(5),
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getDouble(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun getCustomerById(id: String): Query<Customer> = getCustomerById(id) { id_, name, phone,
      whatsapp, cnic, email, address, city, credit_limit, opening_balance, outstanding_balance,
      total_purchases, total_transactions, notes, is_active, created_at, updated_at ->
    Customer(
      id_,
      name,
      phone,
      whatsapp,
      cnic,
      email,
      address,
      city,
      credit_limit,
      opening_balance,
      outstanding_balance,
      total_purchases,
      total_transactions,
      notes,
      is_active,
      created_at,
      updated_at
    )
  }

  public fun <T : Any> getCustomersWithDues(mapper: (
    id: String,
    name: String,
    phone: String,
    whatsapp: String?,
    cnic: String?,
    email: String?,
    address: String,
    city: String,
    credit_limit: Double,
    opening_balance: Double,
    outstanding_balance: Double,
    total_purchases: Double,
    total_transactions: Long,
    notes: String?,
    is_active: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = Query(644_347_602, arrayOf("customer"), driver, "CustomerQueries.sq",
      "getCustomersWithDues", """
  |SELECT customer.id, customer.name, customer.phone, customer.whatsapp, customer.cnic, customer.email, customer.address, customer.city, customer.credit_limit, customer.opening_balance, customer.outstanding_balance, customer.total_purchases, customer.total_transactions, customer.notes, customer.is_active, customer.created_at, customer.updated_at FROM customer
  |WHERE outstanding_balance > 0 AND is_active = 1
  |ORDER BY outstanding_balance DESC
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4),
      cursor.getString(5),
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getDouble(11)!!,
      cursor.getLong(12)!!,
      cursor.getString(13),
      cursor.getLong(14)!!,
      cursor.getLong(15)!!,
      cursor.getLong(16)!!
    )
  }

  public fun getCustomersWithDues(): Query<Customer> = getCustomersWithDues { id, name, phone,
      whatsapp, cnic, email, address, city, credit_limit, opening_balance, outstanding_balance,
      total_purchases, total_transactions, notes, is_active, created_at, updated_at ->
    Customer(
      id,
      name,
      phone,
      whatsapp,
      cnic,
      email,
      address,
      city,
      credit_limit,
      opening_balance,
      outstanding_balance,
      total_purchases,
      total_transactions,
      notes,
      is_active,
      created_at,
      updated_at
    )
  }

  public fun <T : Any> getCustomerCreditInfo(id: String, mapper: (outstanding_balance: Double,
      credit_limit: Double) -> T): Query<T> = GetCustomerCreditInfoQuery(id) { cursor ->
    mapper(
      cursor.getDouble(0)!!,
      cursor.getDouble(1)!!
    )
  }

  public fun getCustomerCreditInfo(id: String): Query<GetCustomerCreditInfo> =
      getCustomerCreditInfo(id) { outstanding_balance, credit_limit ->
    GetCustomerCreditInfo(
      outstanding_balance,
      credit_limit
    )
  }

  public fun insertCustomer(
    id: String,
    name: String,
    phone: String,
    whatsapp: String?,
    cnic: String?,
    email: String?,
    address: String,
    city: String,
    credit_limit: Double,
    opening_balance: Double,
    outstanding_balance: Double,
    notes: String?,
    created_at: Long,
    updated_at: Long,
  ) {
    driver.execute(-1_978_662_555, """
        |INSERT INTO customer (id, name, phone, whatsapp, cnic, email, address, city, credit_limit, opening_balance, outstanding_balance, total_purchases, total_transactions, notes, is_active, created_at, updated_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0.0, 0, ?, 1, ?, ?)
        """.trimMargin(), 14) {
          bindString(0, id)
          bindString(1, name)
          bindString(2, phone)
          bindString(3, whatsapp)
          bindString(4, cnic)
          bindString(5, email)
          bindString(6, address)
          bindString(7, city)
          bindDouble(8, credit_limit)
          bindDouble(9, opening_balance)
          bindDouble(10, outstanding_balance)
          bindString(11, notes)
          bindLong(12, created_at)
          bindLong(13, updated_at)
        }
    notifyQueries(-1_978_662_555) { emit ->
      emit("customer")
    }
  }

  public fun updateCustomer(
    name: String,
    phone: String,
    whatsapp: String?,
    cnic: String?,
    email: String?,
    address: String,
    city: String,
    credit_limit: Double,
    notes: String?,
    updated_at: Long,
    id: String,
  ) {
    driver.execute(1_863_346_037, """
        |UPDATE customer
        |SET name = ?, phone = ?, whatsapp = ?, cnic = ?, email = ?, address = ?, city = ?, credit_limit = ?, notes = ?, updated_at = ?
        |WHERE id = ?
        """.trimMargin(), 11) {
          bindString(0, name)
          bindString(1, phone)
          bindString(2, whatsapp)
          bindString(3, cnic)
          bindString(4, email)
          bindString(5, address)
          bindString(6, city)
          bindDouble(7, credit_limit)
          bindString(8, notes)
          bindLong(9, updated_at)
          bindString(10, id)
        }
    notifyQueries(1_863_346_037) { emit ->
      emit("customer")
    }
  }

  public fun updateCustomerAfterSale(
    dueAmount: Double,
    saleTotal: Double,
    now: Long,
    customerId: String,
  ) {
    driver.execute(1_765_161_038, """
        |UPDATE customer
        |SET outstanding_balance = outstanding_balance + ?,
        |    total_purchases     = total_purchases     + ?,
        |    total_transactions  = total_transactions  + 1,
        |    updated_at          = ?
        |WHERE id = ?
        """.trimMargin(), 4) {
          bindDouble(0, dueAmount)
          bindDouble(1, saleTotal)
          bindLong(2, now)
          bindString(3, customerId)
        }
    notifyQueries(1_765_161_038) { emit ->
      emit("customer")
    }
  }

  public fun decrementCustomerOutstanding(
    amount: Double,
    now: Long,
    customerId: String,
  ) {
    driver.execute(-1_960_534_409, """
        |UPDATE customer
        |SET outstanding_balance = MAX(0.0, outstanding_balance - ?),
        |    updated_at = ?
        |WHERE id = ?
        """.trimMargin(), 3) {
          bindDouble(0, amount)
          bindLong(1, now)
          bindString(2, customerId)
        }
    notifyQueries(-1_960_534_409) { emit ->
      emit("customer")
    }
  }

  public fun softDeleteCustomer(updated_at: Long, id: String) {
    driver.execute(1_780_505_121,
        """UPDATE customer SET is_active = 0, updated_at = ? WHERE id = ?""", 2) {
          bindLong(0, updated_at)
          bindString(1, id)
        }
    notifyQueries(1_780_505_121) { emit ->
      emit("customer")
    }
  }

  private inner class SearchCustomersQuery<out T : Any>(
    public val q: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("customer", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("customer", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_146_264_161, """
    |SELECT customer.id, customer.name, customer.phone, customer.whatsapp, customer.cnic, customer.email, customer.address, customer.city, customer.credit_limit, customer.opening_balance, customer.outstanding_balance, customer.total_purchases, customer.total_transactions, customer.notes, customer.is_active, customer.created_at, customer.updated_at FROM customer
    |WHERE is_active = 1
    |  AND (
    |    name  LIKE ('%' || ? || '%') OR
    |    phone LIKE ('%' || ? || '%') OR
    |    cnic  LIKE ('%' || ? || '%')
    |  )
    |ORDER BY name ASC
    """.trimMargin(), mapper, 3) {
      bindString(0, q)
      bindString(1, q)
      bindString(2, q)
    }

    override fun toString(): String = "CustomerQueries.sq:searchCustomers"
  }

  private inner class GetCustomerByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("customer", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("customer", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_638_405_464,
        """SELECT customer.id, customer.name, customer.phone, customer.whatsapp, customer.cnic, customer.email, customer.address, customer.city, customer.credit_limit, customer.opening_balance, customer.outstanding_balance, customer.total_purchases, customer.total_transactions, customer.notes, customer.is_active, customer.created_at, customer.updated_at FROM customer WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "CustomerQueries.sq:getCustomerById"
  }

  private inner class GetCustomerCreditInfoQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("customer", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("customer", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_838_785_459,
        """SELECT outstanding_balance, credit_limit FROM customer WHERE id = ?""", mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "CustomerQueries.sq:getCustomerCreditInfo"
  }
}
