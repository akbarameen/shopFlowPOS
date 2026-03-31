package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Installment_payment(
  public val id: String,
  public val plan_id: String,
  public val amount: Double,
  public val account_type: String,
  public val account_id: String,
  public val notes: String?,
  public val paid_at: Long,
)
