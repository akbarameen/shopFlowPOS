package com.matechmatrix.shopflowpos.core.model.enums

enum class ProductCategory(
    val displayName: String,
    val emoji: String,
    val hasImei: Boolean = false,
    val hasSpecs: Boolean = false,   // RAM/ROM/Storage/Color
    val hasStock: Boolean = true,    // false = per-unit (mobile with IMEI)
    val showModel: Boolean = true,
    val showBrand: Boolean = true
) {
    PHONE(
        displayName = "Mobile Phone",
        emoji       = "📱",
        hasImei     = true,
        hasSpecs    = false,
        hasStock    = false,   // IMEI-based → each unit is unique
        showModel   = true,
        showBrand   = true
    ),
    LAPTOP(
        displayName = "Laptop",
        emoji       = "💻",
        hasImei     = false,
        hasSpecs    = true,
        hasStock    = true,
        showModel   = true,
        showBrand   = true
    ),
    TABLET(
        displayName = "Tablet",
        emoji       = "📲",
        hasImei     = true,
        hasSpecs    = true,
        hasStock    = false,
        showModel   = true,
        showBrand   = true
    ),
    ACCESSORY(
        displayName = "Accessory",
        emoji       = "🎧",
        hasImei     = false,
        hasSpecs    = false,
        hasStock    = true,
        showModel   = true,
        showBrand   = true
    ),
    SMARTWATCH(
        displayName = "Smartwatch",
        emoji       = "⌚",
        hasImei     = false,
        hasSpecs    = false,
        hasStock    = true,
        showModel   = true,
        showBrand   = true
    ),
    GAMING(
        displayName = "Gaming",
        emoji       = "🎮",
        hasImei     = false,
        hasSpecs    = false,
        hasStock    = true,
        showModel   = true,
        showBrand   = true
    ),
    OTHER(
        displayName = "Other",
        emoji       = "📦",
        hasImei     = false,
        hasSpecs    = false,
        hasStock    = true,
        showModel   = false,
        showBrand   = false
    )
}