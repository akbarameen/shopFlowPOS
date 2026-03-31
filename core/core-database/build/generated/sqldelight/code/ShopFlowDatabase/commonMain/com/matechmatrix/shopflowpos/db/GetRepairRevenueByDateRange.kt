package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long

public data class GetRepairRevenueByDateRange(
  public val total_billed: Double,
  public val total_collected: Double,
  public val total_outstanding: Double,
  public val job_count: Long,
)
