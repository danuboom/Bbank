package com.danunant.bbank.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val thLocale = Locale("th", "TH")
private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", thLocale)

fun formatTHB(satang: Long): String {
    val absSatang = abs(satang)
    val baht = absSatang / 100
    val sat = absSatang % 100
    val sign = if (satang < 0) "-" else ""
    return String.format("%s฿%,d.%02d", sign, baht, sat)
}

fun formatThaiDateTime(instant: Instant): String =
    dateFmt.withZone(ZoneId.systemDefault()).format(instant)

/** Formats a plain account number string with hyphens (e.g., 1234567890 -> 123-4-56789-0) */
fun formatAccountNumber(number: String): String {
    return number.replace("-", "") // Remove existing hyphens just in case
        .chunked(1) // Split into individual digits
        .let { digits ->
            when (digits.size) {
                10 -> "${digits.subList(0, 3).joinToString("")}-${digits[3]}-${digits.subList(4, 9).joinToString("")}-${digits[9]}"
                // Add other formats if needed
                else -> number // Return original if format is unexpected
            }
        }
}

/** Removes hyphens from an account number string (e.g., 123-4-56789-0 -> 1234567890) */
fun unformatAccountNumber(number: String): String {
    return number.replace("-", "")
}