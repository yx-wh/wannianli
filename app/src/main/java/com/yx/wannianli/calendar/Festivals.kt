package com.yx.wannianli.calendar

import java.time.LocalDate

/** 公历节日与农历传统节日 */
object Festivals {

    private val SOLAR = mapOf(
        "01-01" to "元旦",
        "02-14" to "情人节",
        "03-08" to "妇女节",
        "03-12" to "植树节",
        "05-01" to "劳动节",
        "05-04" to "青年节",
        "06-01" to "儿童节",
        "07-01" to "建党节",
        "08-01" to "建军节",
        "09-10" to "教师节",
        "10-01" to "国庆节",
        "12-24" to "平安夜",
        "12-25" to "圣诞节"
    )

    private val LUNAR = mapOf(
        (1 to 1) to "春节",
        (1 to 15) to "元宵节",
        (2 to 2) to "龙抬头",
        (3 to 3) to "上巳节",
        (5 to 5) to "端午节",
        (7 to 7) to "七夕节",
        (7 to 15) to "中元节",
        (8 to 15) to "中秋节",
        (9 to 9) to "重阳节",
        (10 to 1) to "寒衣节",
        (10 to 15) to "下元节",
        (12 to 8) to "腊八节",
        (12 to 23) to "小年"
    )

    fun solar(date: LocalDate): String? =
        SOLAR["%02d-%02d".format(date.monthValue, date.dayOfMonth)]

    fun lunar(lunar: LunarDate): String? = LUNAR[lunar.month to lunar.day]

    /** 当日节日：公历节日优先，其次农历节日与除夕 */
    fun of(date: LocalDate, lunarDate: LunarDate): String? =
        solar(date)
            ?: lunar(lunarDate)
            ?: if (LunarCalendar.isNewYearEve(lunarDate)) "除夕" else null
}
