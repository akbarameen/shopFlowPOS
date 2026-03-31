package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long

public data class GetTotalRefundsByDateRange(
  public val gross_refund: Double,
  public val total_deductions: Double,
  public val net_refund: Double,
  public val return_count: Long,
)
