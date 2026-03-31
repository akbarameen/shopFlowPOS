package com.matechmatrix.shopflowpos.db

import kotlin.Long
import kotlin.String

public data class Invoice_sequence(
  public val prefix: String,
  public val last_number: Long,
  public val updated_at: Long,
)
