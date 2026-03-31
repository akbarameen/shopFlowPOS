package com.matechmatrix.shopflowpos.db

import kotlin.Double

public data class GetCustomerCreditInfo(
  public val outstanding_balance: Double,
  public val credit_limit: Double,
)
