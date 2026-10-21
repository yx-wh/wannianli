package com.yx.wannianli.calendar

import java.time.LocalDate

/**
 * 儒略日（Julian Day Number）换算。
 * 采用 Fliegel-Van Flandern 经典算法（整数除法向零取整）。
 */
internal object JulianDays {

    /** 公历日期 -> 儒略日（正午计日的整数 JDN） */
    fun toJdn(date: LocalDate): Long {
        val a = (14 - date.monthValue) / 12
        val y = date.year + 4800L - a
        val m = date.monthValue + 12 * a - 3
        return date.dayOfMonth +
            (153 * m + 2) / 5 +
            365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    /** 儒略日 -> 公历日期 */
    fun fromJdn(jdn: Long): LocalDate {
        val a = jdn + 32044
        val b = (4 * a + 3) / 146097
        val c = a - 146097 * b / 4
        val d = (4 * c + 3) / 1461
        val e = c - 1461 * d / 4
        val m = (5 * e + 2) / 153
        val day = (e - (153 * m + 2) / 5 + 1).toInt()
        val month = (m + 3 - 12 * (m / 10)).toInt()
        val year = (100 * b + d - 4800 + m / 10).toInt()
        return LocalDate.of(year, month, day)
    }
}
