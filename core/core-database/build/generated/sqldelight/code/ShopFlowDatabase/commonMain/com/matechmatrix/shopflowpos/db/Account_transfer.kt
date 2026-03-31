package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Account_transfer(
  public val id: String,
  public val from_account_type: String,
  public val from_account_id: String,
  public val to_account_type: String,
  public val to_account_id: String,
  public val amount: Double,
  public val notes: String?,
  public val transferred_at: Long,
)
