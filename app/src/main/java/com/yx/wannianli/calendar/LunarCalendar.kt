package com.yx.wannianli.calendar

import java.time.LocalDate

/**
 * 农历日期的显示风格。
 * 书面体（九月九日）见于古诗文（如《九月九日忆山东兄弟》），是正式行文的正体；
 * 传统体（九月初九）为口语习惯，用于与格里历区分。
 */
data class LunarDayStyle(
    /** true：书面体「九月九日」；false：传统体「九月初九」 */
    val written: Boolean = true,
    /** true：21-29 日作「廿三」；false：作「二十三」 */
    val useNian: Boolean = false,
    /** true：30 日作「卅」；false：作「三十」 */
    val useSa: Boolean = false
)

/** 农历日期 */
class LunarDate(
    val year: Int,   // 农历年（1900-2100）
    val month: Int,  // 1..12
    val day: Int,    // 1..30
    val isLeap: Boolean
) {
    /** 如 "九月" / "闰六月" */
    val monthName: String
        get() = (if (isLeap) "闰" else "") + LunarCalendar.MONTH_NAMES[month - 1] + "月"

    /**
     * 农历日，按风格渲染：
     * 书面体 "九日" / "十日" / "十一日" / "二十一日" / "三十日"；
     * 传统体 "初九" / "初十" / "十一" / "廿三" / "三十"。
     */
    fun dayName(style: LunarDayStyle): String {
        val suffix = if (style.written) "日" else ""
        return when (day) {
            in 1..10 -> {
                val num = if (day == 10) "十" else ChineseNumerals.DIGITS[day]
                (if (style.written) "" else "初") + num + suffix
            }
            in 11..19 -> "十" + ChineseNumerals.DIGITS[day - 10] + suffix
            in 20..29 -> {
                val tens = when {
                    day == 20 -> "二十"
                    style.useNian -> "廿"
                    else -> "二十"
                }
                tens + (if (day == 20) "" else ChineseNumerals.DIGITS[day - 20]) + suffix
            }
            else -> (if (style.useSa) "卅" else "三十") + suffix
        }
    }
}

/**
 * 农历换算（1900-2100）。
 * 数据采用经典农历信息表：每年一个 16 位数，
 * 低 4 位为闰月月份，第 4-15 位依次表示 12 个月是大月(30天)还是小月(29天)，
 * 第 16 位表示闰月是否为大月。
 */
object LunarCalendar {
    const val MIN_YEAR = 1900
    const val MAX_YEAR = 2100

    val MONTH_NAMES = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")

    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
        0x0d520
    )

    private val BASE_DATE = LocalDate.of(1900, 1, 31)

    private fun info(year: Int) = LUNAR_INFO[year - MIN_YEAR]

    /** 闰月月份，0 表示无闰月 */
    fun leapMonth(year: Int): Int = info(year) and 0xF

    /** 闰月天数 */
    fun leapDays(year: Int): Int = when {
        leapMonth(year) == 0 -> 0
        info(year) and 0x10000 != 0 -> 30
        else -> 29
    }

    /** 第 month 个月的天数（不含闰月） */
    fun monthDays(year: Int, month: Int): Int =
        if (info(year) and (0x10000 ushr month) != 0) 30 else 29

    /** 农历年总天数 */
    fun yearDays(year: Int): Int {
        var sum = 348
        var bit = 0x8000
        while (bit > 0x8) {
            if (info(year) and bit != 0) sum++
            bit = bit ushr 1
        }
        return sum + leapDays(year)
    }

    /** 公历 -> 农历 */
    fun fromSolar(date: LocalDate): LunarDate {
        require(date.year in MIN_YEAR..MAX_YEAR) { "仅支持 ${MIN_YEAR}-${MAX_YEAR} 年" }
        var offset = date.toEpochDay() - BASE_DATE.toEpochDay()
        var year = MIN_YEAR
        while (offset >= yearDays(year)) {
            offset -= yearDays(year)
            year++
        }
        val leap = leapMonth(year)
        var month = 1
        var isLeap = false
        var remaining = offset
        while (true) {
            val days = if (isLeap) leapDays(year) else monthDays(year, month)
            if (remaining < days) break
            remaining -= days
            when {
                isLeap -> {
                    isLeap = false
                    month++
                }
                leap > 0 && month == leap -> isLeap = true
                else -> month++
            }
        }
        return LunarDate(year, month, remaining.toInt() + 1, isLeap)
    }

    /** 农历 -> 公历 */
    fun toSolar(year: Int, month: Int, day: Int, isLeap: Boolean): LocalDate {
        var offset = 0L
        for (y in MIN_YEAR until year) offset += yearDays(y)
        val leap = leapMonth(year)
        for (m in 1 until month) {
            offset += monthDays(year, m)
            if (leap > 0 && m == leap) offset += leapDays(year)
        }
        if (isLeap) offset += monthDays(year, month)
        offset += day - 1
        return BASE_DATE.plusDays(offset)
    }

    /** 是否为农历除夕（农历一年的最后一天） */
    fun isNewYearEve(lunar: LunarDate): Boolean {
        if (lunar.month != 12) return false
        val leap = leapMonth(lunar.year)
        return if (leap == 12) {
            lunar.isLeap && lunar.day == leapDays(lunar.year)
        } else {
            !lunar.isLeap && lunar.day == monthDays(lunar.year, 12)
        }
    }
}
