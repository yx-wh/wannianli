package com.yx.wannianli.calendar

/** 中文数字工具 */
object ChineseNumerals {
    val DIGITS = arrayOf("〇", "一", "二", "三", "四", "五", "六", "七", "八", "九")

    /** 逐位读法：4723 -> 四七二三（用于黄帝纪元年份） */
    fun digitWise(value: Int): String =
        value.toString().map { DIGITS[it - '0'] }.joinToString("")
}
