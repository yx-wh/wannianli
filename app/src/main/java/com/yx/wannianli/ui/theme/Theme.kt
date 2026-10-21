package com.yx.wannianli.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 应用配色（水墨鎏金）。
 * 暗色用于常规 OLED 屏幕；亮色（宣纸）用于墨水屏与日间阅读。
 */
data class WanNianLiPalette(
    val background: Color,
    val card: Color,
    val gold: Color,
    val goldBright: Color,
    val rice: Color,
    val riceDim: Color,
    val cinnabar: Color,
    val cinnabarBright: Color,
    val jade: Color,
    val violet: Color,
    val todayGold: Color,  // 「今日」圆底
    val onToday: Color,    // 「今日」圆上的文字
    val onCinnabar: Color  // 朱砂选中圆上的文字
)

val DarkPalette = WanNianLiPalette(
    background = Ink,
    card = InkCard,
    gold = Gold,
    goldBright = GoldBright,
    rice = RicePaper,
    riceDim = RiceDim,
    cinnabar = Cinnabar,
    cinnabarBright = CinnabarBright,
    jade = Jade,
    violet = Violet,
    todayGold = Gold,
    onToday = Ink,
    onCinnabar = RicePaper
)

val LightPalette = WanNianLiPalette(
    background = PaperBg,
    card = PaperCard,
    gold = Bronze,
    goldBright = BronzeBright,
    rice = InkText,
    riceDim = InkDim,
    cinnabar = Cinnabar,
    cinnabarBright = Cinnabar,
    jade = JadeDark,
    violet = VioletDark,
    todayGold = Gold,
    onToday = InkText,
    onCinnabar = RicePaper
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = CinnabarBright,
    onSecondary = RicePaper,
    background = Ink,
    onBackground = RicePaper,
    surface = InkSurface,
    onSurface = RicePaper,
    surfaceVariant = InkCard,
    onSurfaceVariant = RiceDim,
    outline = Gold.copy(alpha = 0.35f)
)

private val LightColors = lightColorScheme(
    primary = Bronze,
    onPrimary = PaperCard,
    secondary = Cinnabar,
    onSecondary = RicePaper,
    background = PaperBg,
    onBackground = InkText,
    surface = PaperCard,
    onSurface = InkText,
    surfaceVariant = PaperVariant,
    onSurfaceVariant = InkDim,
    outline = Color(0xFFC9B98A)
)

@Composable
fun WanNianLiTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPalette provides if (dark) DarkPalette else LightPalette) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = Typography(),
            content = content
        )
    }
}
