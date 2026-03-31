package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Stock_movement(
  public val id: String,
  public val product_id: String,
  public val product_name: String,
  public val movement_type: String,
  public val reference_type: String?,
  public val reference_id: String?,
  public val quantity_before: Long,
  public val quantity_change: Long,
  public val quantity_after: Long,
  public val unit_cost: Double?,
  public val notes: String?,
  public val created_at: Long,
)
