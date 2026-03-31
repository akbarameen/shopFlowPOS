package com.matechmatrix.shopflowpos.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Installment_plan(
  public val id: String,
  public val plan_number: String,
  public val sale_id: String?,
  public val customer_id: String?,
  public val customer_name: String,
  public val customer_phone: String,
  public val customer_cnic: String?,
  public val customer_address: String,
  public val product_id: String,
  public val product_name: String,
  public val imei: String?,
  public val total_amount: Double,
  public val down_payment: Double,
  public val financed_amount: Double,
  public val installment_amount: Double,
  public val total_installments: Long,
  public val paid_installments: Long,
  public val paid_amount: Double,
  public val remaining_amount: Double,
  public val frequency: String,
  public val start_date: Long,
  public val next_due_date: Long,
  public val is_completed: Long,
  public val is_defaulted: Long,
  public val notes: String?,
  public val created_at: Long,
  public val updated_at: Long,
)
