package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Customer(
  public val id: String,
  public val name: String,
  public val phone: String,
  public val whatsapp: String?,
  public val cnic: String?,
  public val email: String?,
  public val address: String,
  public val city: String,
  public val credit_limit: Double,
  public val opening_balance: Double,
  public val outstanding_balance: Double,
  public val total_purchases: Double,
  public val total_transactions: Long,
  public val notes: String?,
  public val is_active: Long,
  public val created_at: Long,
  public val updated_at: Long,
)
