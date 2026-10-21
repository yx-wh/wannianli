package com.yx.wannianli.calendar

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

/**
 * 一天的完整黄历信息。
 * 节日按历法归属拆分：阳历节日与角色生日跟随「西元」行，农历节日与节气跟随「黄帝纪元」行。
 */
data class CalendarDay(
    val date: LocalDate,
    val lunar: LunarDate,
    val lunarDayName: String, // 已按显示风格渲染，如 "九日" / "初九"
    val solarFestival: String?, // 阳历节日（元旦、劳动节、国庆节…）
    val lunarFestival: String?, // 农历节日（春节、中秋、重阳、除夕、清明节…）
    val genshinBirthdays: List<String>, // 当日《原神》角色生日（可能多位）
    val ganzhiYear: String,
    val zodiac: String,
    val ganzhiMonth: String,
    val ganzhiDay: String,
    val solarTerm: String?,
    val huangdiYear: Int,
    val isoWeek: Int,
    val weekdayName: String
) {
    /** 第一行：西元2026年10月18日 第42周 星期日（阳历节日与角色生日附于行尾） */
    val line1: String
        get() = buildString {
            append("西元${date.year}年${date.monthValue}月${date.dayOfMonth}日")
            append("第${isoWeek}周$weekdayName")
            solarFestival?.let { append(" ").append(it) }
            birthdayTag?.let { append(" ").append(it) }
        }

    /** 公历日期部分：西元2026年10月18日（2×2 微件用） */
    val line1Date: String
        get() = "西元${date.year}年${date.monthValue}月${date.dayOfMonth}日"

    /** 周次部分：第42周 星期日（阳历节日与角色生日附于行尾，2×2 微件用） */
    val line1Week: String
        get() = buildString {
            append("第${isoWeek}周 $weekdayName")
            solarFestival?.let { append(" ").append(it) }
            birthdayTag?.let { append(" ").append(it) }
        }

    /** 角色生日标签，如 "可莉生日" / "可莉、久岐忍生日" */
    val birthdayTag: String?
        get() = if (genshinBirthdays.isEmpty()) null
                else genshinBirthdays.joinToString("、") + "生日"

    /** 第二行：黄帝纪元四七二三年九月九日丙午年（马）戊戌月乙丑日重阳节（农历节日/节气附于行尾） */
    val line2: String
        get() = buildString {
            append("黄帝纪元").append(ChineseNumerals.digitWise(huangdiYear)).append("年")
            append(lunar.monthName).append(lunarDayName)
            append(ganzhiYear).append("年（").append(zodiac).append("）")
            append(ganzhiMonth).append("月").append(ganzhiDay).append("日")
            (lunarFestival ?: solarTerm)?.let { append("　").append(it) }
        }

    /** 微件用：黄帝纪元四七二三年九月九日 */
    val widgetLine2: String
        get() = "黄帝纪元${ChineseNumerals.digitWise(huangdiYear)}年" +
            "${lunar.monthName}$lunarDayName"

    /** 微件用：丙午年（马）戊戌月乙丑日 · 重阳节（农历节日/节气） */
    val widgetLine3: String
        get() = buildString {
            append(ganzhiYear).append("年（").append(zodiac).append("）")
            append(ganzhiMonth).append("月").append(ganzhiDay).append("日")
            (lunarFestival ?: solarTerm)?.let { append(" · ").append(it) }
        }

    /** 微件用：单行紧凑版（4×1 微件） */
    val compactLine: String
        get() = "$widgetLine2 $widgetLine3"
}

/** 万年历门面 */
object WanNianLi {

    /**
     * 黄帝纪元 = 农历年 + 2697。
     * 以春节为界，与干支年同岁（如 2026 年春节起为黄帝四七二三年、丙午年）；
     * 春节前仍属上一个黄帝年（如 2026-02-16 为四七二二年、乙巳年）。
     */
    const val HUANGDI_OFFSET = 2697

    private val WEEKDAYS =
        arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

    fun of(
        date: LocalDate,
        style: LunarDayStyle = LunarDayStyle(),
        showGenshinBirthdays: Boolean = true
    ): CalendarDay {
        val lunar = LunarCalendar.fromSolar(date)
        val term = SolarTerms.termOn(date)
        val solarFestival = Festivals.solar(date)
        // 清明兼具节气与节日属性，作为农历节日展示
        val lunarFestival = Festivals.lunar(lunar) ?: if (term == "清明") "清明节" else null
        val birthdays =
            if (showGenshinBirthdays) GenshinBirthdays.of(date.monthValue, date.dayOfMonth)
            else emptyList()
        return CalendarDay(
            date = date,
            lunar = lunar,
            lunarDayName = lunar.dayName(style),
            solarFestival = solarFestival,
            lunarFestival = lunarFestival,
            genshinBirthdays = birthdays,
            ganzhiYear = Ganzhi.ofYear(lunar.year),
            zodiac = Ganzhi.zodiacOf(lunar.year),
            ganzhiMonth = Ganzhi.ofMonth(date),
            ganzhiDay = Ganzhi.ofDay(date),
            solarTerm = term,
            huangdiYear = lunar.year + HUANGDI_OFFSET,
            isoWeek = date.get(WeekFields.ISO.weekOfWeekBasedYear()),
            weekdayName = WEEKDAYS[date.dayOfWeek.value - 1]
        )
    }

    /** 月历网格：6 行 7 列，周日开头，含上下月补位 */
    fun monthGrid(
        month: YearMonth,
        style: LunarDayStyle = LunarDayStyle(),
        showGenshinBirthdays: Boolean = true
    ): List<CalendarDay> {
        val first = month.atDay(1)
        val lead = first.dayOfWeek.value % 7 // 周一=1 … 周日=0
        val start = first.minusDays(lead.toLong())
        return (0 until 42).map { of(start.plusDays(it.toLong()), style, showGenshinBirthdays) }
    }

    /** 某公历月内的节气（按日期升序） */
    fun termsOfMonth(month: YearMonth): List<Pair<LocalDate, String>> =
        SolarTerms.ofMonth(month)

    /** 某公历月内的节日（阳历 + 农历合并，按日期升序） */
    fun festivalsOfMonth(month: YearMonth): List<Pair<LocalDate, String>> {
        val result = ArrayList<Pair<LocalDate, String>>()
        var d = month.atDay(1)
        while (YearMonth.from(d) == month) {
            val day = of(d)
            val names = listOfNotNull(day.solarFestival, day.lunarFestival)
            if (names.isNotEmpty()) result.add(d to names.joinToString("、"))
            d = d.plusDays(1)
        }
        return result
    }

    /** 某公历月内的角色生日（按日期升序） */
    fun genshinBirthdaysOfMonth(
        month: YearMonth,
        showGenshinBirthdays: Boolean = true
    ): List<Pair<LocalDate, List<String>>> {
        if (!showGenshinBirthdays) return emptyList()
        val result = ArrayList<Pair<LocalDate, List<String>>>()
        var d = month.atDay(1)
        while (YearMonth.from(d) == month) {
            val names = GenshinBirthdays.of(d.monthValue, d.dayOfMonth)
            if (names.isNotEmpty()) result.add(d to names)
            d = d.plusDays(1)
        }
        return result
    }
}
