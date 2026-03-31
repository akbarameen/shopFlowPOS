package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Purchase_order_item(
  public val id: String,
  public val purchase_order_id: String,
  public val product_id: String,
  public val product_name: String,
  public val imei: String?,
  public val quantity: Long,
  public val unit_cost: Double,
  public val total_cost: Double,
)
