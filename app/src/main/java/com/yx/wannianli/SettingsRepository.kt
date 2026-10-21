package com.yx.wannianli

import android.content.Context
import com.yx.wannianli.calendar.LunarDayStyle

/** 微件背景样式 */
enum class WidgetBackground {
    /** 暗色卡片（默认） */
    DARK,

    /** 半透明磨砂 */
    FROSTED,

    /** 全透明，仅保留细金边（浅色文字，适合深色壁纸） */
    CLEAR
}

/** 主题模式 */
enum class ThemeMode {
    /** 跟随系统 */
    SYSTEM,

    /** 暗色（水墨） */
    DARK,

    /** 亮色（宣纸，适合墨水屏） */
    LIGHT
}

/** 应用全部显示设置 */
data class AppSettings(
    val dayStyle: LunarDayStyle = LunarDayStyle(),
    val widgetBackground: WidgetBackground = WidgetBackground.DARK,
    val showGenshinBirthdays: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/** 显示设置的持久化（SharedPreferences） */
object SettingsRepository {

    private const val PREFS_NAME = "wannianli_settings"
    private const val KEY_WRITTEN = "lunar_day_written"
    private const val KEY_NIAN = "lunar_num_nian"
    private const val KEY_SA = "lunar_num_sa"
    private const val KEY_WIDGET_BG = "widget_background"
    private const val KEY_GENSHIN = "show_genshin_birthdays"
    private const val KEY_THEME = "theme_mode"

    fun load(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val bgName = prefs.getString(KEY_WIDGET_BG, WidgetBackground.DARK.name)
        val bg = WidgetBackground.entries.firstOrNull { it.name == bgName }
            ?: WidgetBackground.DARK
        val themeName = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)
        val theme = ThemeMode.entries.firstOrNull { it.name == themeName } ?: ThemeMode.SYSTEM
        return AppSettings(
            dayStyle = LunarDayStyle(
                written = prefs.getBoolean(KEY_WRITTEN, true),
                useNian = prefs.getBoolean(KEY_NIAN, false),
                useSa = prefs.getBoolean(KEY_SA, false)
            ),
            widgetBackground = bg,
            showGenshinBirthdays = prefs.getBoolean(KEY_GENSHIN, true),
            themeMode = theme
        )
    }

    fun save(context: Context, settings: AppSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WRITTEN, settings.dayStyle.written)
            .putBoolean(KEY_NIAN, settings.dayStyle.useNian)
            .putBoolean(KEY_SA, settings.dayStyle.useSa)
            .putString(KEY_WIDGET_BG, settings.widgetBackground.name)
            .putBoolean(KEY_GENSHIN, settings.showGenshinBirthdays)
            .putString(KEY_THEME, settings.themeMode.name)
            .apply()
    }
}
