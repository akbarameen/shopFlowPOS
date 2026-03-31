package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Purchase_payment(
  public val id: String,
  public val purchase_order_id: String,
  public val amount: Double,
  public val account_type: String,
  public val account_id: String,
  public val reference_number: String?,
  public val notes: String?,
  public val paid_at: Long,
)
