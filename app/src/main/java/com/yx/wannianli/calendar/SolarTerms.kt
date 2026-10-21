package com.yx.wannianli.calendar

import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

/**
 * 二十四节气。
 * 以 Meeus 低精度太阳视黄经公式计算交节时刻（误差约 0.01°，足以确定交节日期），
 * 再转换为北京时间（UTC+8）的公历日期。
 */
object SolarTerms {
    val NAMES = arrayOf(
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    )

    private val cache = HashMap<Int, LocalDate>()

    /** 第 index 个节气（0=小寒 … 23=冬至）的交节日期（北京时间） */
    fun date(year: Int, index: Int): LocalDate {
        val key = year * 100 + index
        cache[key]?.let { return it }
        val target = normalize(285.0 + 15.0 * index)
        var jd = JulianDays.toJdn(LocalDate.of(year, 1, 1)) - 0.5 + 15.2 * index + 4.5
        repeat(12) {
            val f = angleDiff(sunLongitude(jd), target)
            if (abs(f) < 1e-10) return@repeat
            val slope = angleDiff(sunLongitude(jd + 0.05), sunLongitude(jd - 0.05)) / 0.1
            jd -= f / slope
        }
        val date = JulianDays.fromJdn(floor(jd + 8.0 / 24.0 + 0.5).toLong())
        cache[key] = date
        return date
    }

    /** 当天正好是某个节气时返回节气名，否则 null */
    fun termOn(date: LocalDate): String? {
        for (i in NAMES.indices) {
            if (date(date.year, i) == date) return NAMES[i]
        }
        return null
    }

    /** 某公历月内的节气列表（按日期升序） */
    fun ofMonth(month: java.time.YearMonth): List<Pair<LocalDate, String>> =
        (0 until 24).mapNotNull { i ->
            val d = date(month.year, i)
            if (java.time.YearMonth.from(d) == month) d to NAMES[i] else null
        }.sortedBy { it.first }

    /**
     * 当前所处“节”（月首节气）的序号 0..11：
     * 0=小寒(丑月) 1=立春(寅月) … 11=大雪(子月)。用于确定干支月柱。
     */
    fun lastJieIndex(date: LocalDate): Int {
        for (y in date.year downTo date.year - 1) {
            for (k in 11 downTo 0) {
                if (date(y, k * 2) <= date) return k
            }
        }
        return 11
    }

    private fun normalize(deg: Double) = ((deg % 360.0) + 360.0) % 360.0

    private fun angleDiff(a: Double, b: Double) = normalize(a - b + 180.0) - 180.0

    /** 太阳视黄经（度） */
    private fun sunLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val m = Math.toRadians(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(m) +
            (0.019993 - 0.000101 * t) * sin(2 * m) +
            0.000289 * sin(3 * m)
        val trueLong = l0 + c
        val omega = Math.toRadians(125.04 - 1934.136 * t)
        val lambda = trueLong - 0.00569 - 0.00478 * sin(omega)
        return normalize(lambda)
    }
}
