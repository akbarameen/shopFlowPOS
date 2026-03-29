package com.matechmatrix.shopflowpos.core.common.util

object CurrencyFormatter {

    // Symbol can be changed from Settings (stored in DB)
    var symbol: String = "Rs."

    fun format(amount: Double): String {
        val longAmount = amount.toLong()
        if (longAmount < 0) return "-${format(-amount)}"
        val str = longAmount.toString()
        val withCommas = buildString {
            str.reversed().forEachIndexed { i, c ->
                if (i != 0 && i % 3 == 0) append(',')
                append(c)
            }
        }.reversed()
        return "$symbol $withCommas"
    }

    fun format(amount: Long): String = format(amount.toDouble())

    fun formatRs(amount: Double): String = format(amount)
    fun formatRs(amount: Long): String = format(amount.toDouble())

    fun formatCompact(amount: Double): String = when {
        amount >= 1_000_000_000 -> {
            val value = (amount / 100_000_000).toLong() / 10.0
            "$symbol ${value}B"
        }
        amount >= 1_000_000 -> {
            val value = (amount / 100_000).toLong() / 10.0
            "$symbol ${value}M"
        }
        amount >= 1_000 -> {
            val value = (amount / 1_000).toLong()
            "$symbol ${value}K"
        }
        else -> format(amount)
    }

    fun formatWithDecimal(amount: Double): String {
        val whole = amount.toLong()
        val decimal = ((amount - whole) * 100).toInt()
        val absDecimal = if (decimal < 0) -decimal else decimal
        return "${format(whole.toDouble())}.${absDecimal.toString().padStart(2, '0')}"
    }

    private const val SYMBOL = "Rs"

    /** Format: "Rs 1,234,567" */
    fun formatPkr(amount: Double): String {
        if (amount < 0) return "- ${formatPkr(-amount)}"
        val long = amount.toLong()
        val decimal = ((amount - long) * 100).toInt()
        val absDecimal = if (decimal < 0) -decimal else decimal
        val formatted = insertCommas(long.toString())
        return if (absDecimal > 0) "$SYMBOL $formatted.${absDecimal.toString().padStart(2, '0')}"
        else "$SYMBOL $formatted"
    }

    fun formatPkr(amount: Long): String = formatPkr(amount.toDouble())
    fun formatPkr(amount: Int): String  = formatPkr(amount.toDouble())

    /** Compact: "Rs 1.2M", "Rs 45K" for dashboard stats */
    fun formatPkrCompact(amount: Double): String = when {
        amount >= 1_000_000 -> "$SYMBOL ${(amount / 100_000).toLong() / 10.0}M"
        amount >= 1_000     -> "$SYMBOL ${(amount / 100).toLong() / 10.0}K"
        else                -> formatPkr(amount)
    }

    private fun insertCommas(s: String): String {
        if (s.length <= 3) return s
        val sb = StringBuilder()
        val start = s.length % 3
        if (start > 0) sb.append(s.take(start))
        var i = start
        while (i < s.length) {
            if (sb.isNotEmpty()) sb.append(',')
            sb.append(s.substring(i, i + 3))
            i += 3
        }
        return sb.toString()
    }
}
