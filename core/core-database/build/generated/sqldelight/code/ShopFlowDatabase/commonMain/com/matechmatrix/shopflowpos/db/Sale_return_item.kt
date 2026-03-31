package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Sale_return_item(
  public val id: String,
  public val return_id: String,
  public val original_sale_item_id: String,
  public val product_id: String,
  public val product_name: String,
  public val imei: String?,
  public val returned_quantity: Long,
  public val unit_price: Double,
  public val restock_item: Long,
  public val line_refund: Double,
)
