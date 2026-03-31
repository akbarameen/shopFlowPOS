package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Purchase_order(
  public val id: String,
  public val po_number: String,
  public val supplier_id: String,
  public val supplier_name: String,
  public val supplier_phone: String,
  public val subtotal: Double,
  public val discount_amount: Double,
  public val total_amount: Double,
  public val paid_amount: Double,
  public val due_amount: Double,
  public val payment_status: String,
  public val supplier_invoice_ref: String?,
  public val goods_status: String,
  public val notes: String?,
  public val purchased_at: Long,
  public val updated_at: Long,
)
