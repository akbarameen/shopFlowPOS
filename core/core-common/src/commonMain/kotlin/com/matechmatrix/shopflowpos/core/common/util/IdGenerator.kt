package com.matechmatrix.shopflowpos.core.common.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generates unique IDs for all entities.
 * Uses a simple timestamp + random suffix approach for offline-first apps.
 * No UUID library needed — works on all platforms.
 */
object IdGenerator {

    fun generate(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(100_000, 999_999)
        return "${timestamp}_${random}"
    }

    fun generateInvoiceNumber(todaySalesCount: Int): String {
        // Format: INV-20240315-0001
        val now = Clock.System.now()
        val date = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dateStr = "${date.year}${date.monthNumber.toString().padStart(2, '0')}${date.dayOfMonth.toString().padStart(2, '0')}"
        val seq = (todaySalesCount + 1).toString().padStart(4, '0')
        return "INV-$dateStr-$seq"
    }

    /** Full UUID string e.g. "550e8400-e29b-41d4-a716-446655440000" */
    @OptIn(ExperimentalUuidApi::class)
    fun newId(): String = Uuid.random().toString()

    /** Short 8-char hex prefix — for invoice suffixes, ticket numbers */
    fun shortId(): String = newId().replace("-", "").take(8).uppercase()

    /** Invoice-style: prefix + counter, e.g. "S-1043" */
    fun invoiceId(prefix: String, counter: Int): String = "$prefix-$counter"
}
