package com.matechmatrix.shopflowpos.core.common.util

import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

object DateTimeUtils {

    fun nowMillis(): Long =
        Clock.System.now().toEpochMilliseconds()

    fun todayRange(): Pair<Long, Long> {
        val start = todayStartMillis()
        val end = todayEndMillis()
        return Pair(start, end)
    }

    fun todayString(): String =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() // "2024-03-15"

    @OptIn(ExperimentalTime::class)
    fun todayStartMillis(): Long {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    fun todayEndMillis(): Long =
        todayStartMillis() + 86_400_000L - 1L

    fun yesterdayRange(): Pair<Long, Long> {
        val start = todayStartMillis() - 86_400_000L
        return Pair(start, start + 86_400_000L - 1L)
    }

    fun thisWeekRange(): Pair<Long, Long> {
        val todayStart = todayStartMillis()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dayOfWeek = today.dayOfWeek.ordinal // 0 = Monday
        val weekStart = todayStart - (dayOfWeek * 86_400_000L)
        return Pair(weekStart, todayEndMillis())
    }

    @OptIn(ExperimentalTime::class)
    fun thisMonthRange(): Pair<Long, Long> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val monthStart = LocalDate(today.year, today.month, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        return Pair(monthStart, todayEndMillis())
    }
    @OptIn(ExperimentalTime::class)
    fun thisYearRange(): Pair<Long, Long> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        // Create a date for January 1st of the current year
        val yearStart = LocalDate(today.year, 1, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        return Pair(yearStart, todayEndMillis())
    }
    @OptIn(ExperimentalTime::class)
    fun dateStringToRange(dateString: String): Pair<Long, Long> {
        // dateString format: "yyyy-MM-dd"
        val date = LocalDate.parse(dateString)
        val start = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return Pair(start, start + 86_400_000L - 1L)
    }

    fun formatDate(epochMs: Long): String {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return "${dt.dayOfMonth} ${months[dt.monthNumber - 1]} ${dt.year}"
    }

    fun formatDateTime(epochMs: Long): String {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val hour = dt.hour.toString().padStart(2, '0')
        val min = dt.minute.toString().padStart(2, '0')
        return "${dt.dayOfMonth} ${months[dt.monthNumber - 1]} ${dt.year}, $hour:$min"
    }

    fun formatTime(epochMs: Long): String {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = dt.hour.toString().padStart(2, '0')
        val min = dt.minute.toString().padStart(2, '0')
        return "$hour:$min"
    }


    private val tz = TimeZone.of("Asia/Karachi") // PKT UTC+5

//    /** Current UTC epoch millis */
//    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    /** Today as ISO string: "2025-03-26" */
    fun today(): String = Clock.System.now()
        .toLocalDateTime(tz).date.toString()

    /** Current time as "14:30" */
    fun currentTime(): String {
        val dt = Clock.System.now().toLocalDateTime(tz)
        return "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    }

    /** Current month-year string: "2025-03" */
    fun currentMonthYear(): String {
        val dt = Clock.System.now().toLocalDateTime(tz)
        return "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}"
    }

    /** Parse ISO date string to LocalDate */
    fun parseDate(iso: String): LocalDate = LocalDate.parse(iso)

    /** Human-readable: "26 Mar 2025" */
    fun formatDisplayDate(iso: String): String {
        val d = parseDate(iso)
        val month = when (d.monthNumber) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
            5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
            9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dec"
        }
        return "${d.dayOfMonth} $month ${d.year}"
    }

    /** Returns true if iso date string is today */
    fun isToday(iso: String): Boolean = iso == today()

    /** Days difference: positive = iso is in the future */
    fun daysDiff(isoFrom: String, isoTo: String): Int {
        val from = parseDate(isoFrom)
        val to   = parseDate(isoTo)
        return (to.toEpochDays() - from.toEpochDays()).toInt()
    }
}
