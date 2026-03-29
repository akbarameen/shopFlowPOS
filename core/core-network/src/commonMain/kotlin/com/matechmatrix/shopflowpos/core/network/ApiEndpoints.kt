package com.matechmatrix.shopflowpos.core.network

object ApiEndpoints {
    private const val BASE_URL = "https://api.shopflowpos.app/v1"

    const val ACTIVATE_LICENSE  = "$BASE_URL/auth/activate"
    const val VALIDATE_LICENSE  = "$BASE_URL/auth/validate"
    const val CHECK_UPDATE      = "$BASE_URL/updates/check"
}
