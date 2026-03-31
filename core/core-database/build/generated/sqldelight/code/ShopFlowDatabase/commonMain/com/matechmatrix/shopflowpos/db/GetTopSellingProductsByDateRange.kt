package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class GetTopSellingProductsByDateRange(
  public val product_id: String,
  public val product_name: String,
  public val total_qty: Long?,
  public val total_revenue: Double?,
  public val total_cost: Double?,
)
