package com.matechmatrix.shopflowpos.db

import kotlin.Long
import kotlin.String

public data class GetProductMovementSummary(
  public val product_id: String,
  public val total_in: Long,
  public val total_out: Long,
)
