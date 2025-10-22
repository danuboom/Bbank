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
