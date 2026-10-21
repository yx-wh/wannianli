package com.yx.wannianli.calendar

import java.time.LocalDate

/** 干支纪年法 */
object Ganzhi {
    val STEMS = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    val BRANCHES = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    val ZODIACS = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    /** 六十甲子第 index 位（0=甲子） */
    fun ganzhi(index: Int): String {
        val i = ((index % 60) + 60) % 60
        return STEMS[i % 10] + BRANCHES[i % 12]
    }

    /** 农历年 -> 干支年（以春节为岁首） */
    fun ofYear(lunarYear: Int): String = ganzhi(lunarYear - 4)

    /** 农历年 -> 生肖 */
    fun zodiacOf(lunarYear: Int): String = ZODIACS[(((lunarYear - 4) % 12) + 12) % 12]

    /** 日柱：由儒略日推算，(JDN + 49) % 60 == 0 即甲子日 */
    fun ofDay(date: LocalDate): String = ganzhi((JulianDays.toJdn(date) + 49).toInt())

    /**
     * 月柱：以节气定月（遇“节”换月），年干取农历年干，按“五虎遁”推月干。
     * 丙辛之年寻庚起 —— 如丙午年寅月起庚寅。
     */
    fun ofMonth(date: LocalDate): String {
        val lunarYear = LunarCalendar.fromSolar(date).year
        val monthIndex = SolarTerms.lastJieIndex(date) // 0=丑月 … 11=子月
        val yinStem = ((lunarYear - 4) % 5) * 2 + 2     // 寅月天干
        val stem = (((yinStem + monthIndex - 1) % 10) + 10) % 10
        val branch = (monthIndex + 1) % 12
        return STEMS[stem] + BRANCHES[branch]
    }

    /** 时辰：hour 为 0-23，子时跨 23:00-01:00 */
    fun ofHour(hour: Int): String = BRANCHES[((hour + 1) / 2) % 12] + "时"
}
