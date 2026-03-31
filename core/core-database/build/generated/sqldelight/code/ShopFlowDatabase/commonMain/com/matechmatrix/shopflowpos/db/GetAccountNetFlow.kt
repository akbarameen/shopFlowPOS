package com.matechmatrix.shopflowpos.db

import kotlin.Double

public data class GetAccountNetFlow(
  public val total_credit: Double,
  public val total_debit: Double,
)
