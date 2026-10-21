package com.yx.wannianli

import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.yx.wannianli.ui.CalendarScreen
import com.yx.wannianli.ui.theme.DarkPalette
import com.yx.wannianli.ui.theme.LightPalette
import com.yx.wannianli.ui.theme.WanNianLiTheme
import com.yx.wannianli.widget.CalendarWidgetProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initial = SettingsRepository.load(this)
        val initialDark = isDarkTheme(initial.themeMode)
        // 窗口底色跟随主题，避免启动时暗色闪屏
        window.setBackgroundDrawable(
            ColorDrawable(
                (if (initialDark) DarkPalette.background else LightPalette.background).toArgb()
            )
        )
        enableEdgeToEdge()
        applySystemBarAppearance(initialDark)

        setContent {
            var settings by remember { mutableStateOf(initial) }
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            WanNianLiTheme(dark = dark) {
                CalendarScreen(
                    settings = settings,
                    onSettingsChange = { newSettings ->
                        settings = newSettings
                        SettingsRepository.save(this, newSettings)
                        applySystemBarAppearance(isDarkTheme(newSettings.themeMode))
                        // 设置变更后立即同步桌面微件（含三种尺寸）
                        CalendarWidgetProvider.refreshAll(this)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 打开应用时同步刷新桌面微件（例如跨天后首次打开）
        CalendarWidgetProvider.refreshAll(this)
    }

    /** 主题模式 -> 是否暗色（SYSTEM 时读系统夜间模式） */
    private fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
        ThemeMode.SYSTEM -> (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    /** 状态栏/导航栏图标明暗（edge-to-edge 下背景透明，仅切换图标） */
    private fun applySystemBarAppearance(dark: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = !dark
        controller.isAppearanceLightNavigationBars = !dark
    }
}
