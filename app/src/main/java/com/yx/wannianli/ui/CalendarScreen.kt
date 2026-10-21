package com.yx.wannianli.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yx.wannianli.AppSettings
import com.yx.wannianli.ThemeMode
import com.yx.wannianli.WidgetBackground
import com.yx.wannianli.calendar.CalendarDay
import com.yx.wannianli.calendar.ChineseNumerals
import com.yx.wannianli.calendar.Ganzhi
import com.yx.wannianli.calendar.LunarDate
import com.yx.wannianli.calendar.WanNianLi
import com.yx.wannianli.ui.theme.LocalPalette
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val PAGER_PAGES = 1201
private const val PAGER_MIDDLE = 600

@Composable
fun CalendarScreen(settings: AppSettings, onSettingsChange: (AppSettings) -> Unit) {
    val p = LocalPalette.current
    val dayStyle = settings.dayStyle
    val showGenshin = settings.showGenshinBirthdays
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(today) }
    var showSettings by remember { mutableStateOf(false) }
    val baseMonth = remember { YearMonth.from(today) }
    val pagerState = rememberPagerState(initialPage = PAGER_MIDDLE, pageCount = { PAGER_PAGES })
    val scope = rememberCoroutineScope()

    val day = remember(selected, dayStyle, showGenshin) {
        WanNianLi.of(selected, dayStyle, showGenshin)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(p.background)
            // edge-to-edge 下消费系统栏与刘海 Insets，避免被状态栏/导航栏遮挡
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp)
        ) {
            HeroCard(day = day, today = today)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = p.card),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, p.gold.copy(alpha = 0.25f))
            ) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    // 月份标题：◀◀ 上一年 / ◀ 上月 / 年月 / ▶ 下月 / ▶▶ 下一年
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    (pagerState.currentPage - 12).coerceAtLeast(0)
                                )
                            }
                        }) {
                            Text("◀◀", color = p.gold, fontSize = 13.sp)
                        }
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    (pagerState.currentPage - 1).coerceAtLeast(0)
                                )
                            }
                        }) {
                            Text("◀", color = p.gold, fontSize = 16.sp)
                        }
                        val pageMonth = baseMonth.plusMonths(
                            (pagerState.currentPage - PAGER_MIDDLE).toLong()
                        )
                        val firstOfPage = remember(pageMonth) { WanNianLi.of(pageMonth.atDay(1)) }
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${pageMonth.year}年${pageMonth.monthValue}月",
                                color = p.rice,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${firstOfPage.ganzhiYear}年${firstOfPage.lunar.monthName}",
                                color = p.gold.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    (pagerState.currentPage + 1).coerceAtMost(PAGER_PAGES - 1)
                                )
                            }
                        }) {
                            Text("▶", color = p.gold, fontSize = 16.sp)
                        }
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    (pagerState.currentPage + 12).coerceAtMost(PAGER_PAGES - 1)
                                )
                            }
                        }) {
                            Text("▶▶", color = p.gold, fontSize = 13.sp)
                        }
                    }

                    // 星期表头（周日开头）
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        listOf("日", "一", "二", "三", "四", "五", "六").forEach { w ->
                            Text(
                                w,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = if (w == "日" || w == "六") p.cinnabarBright else p.gold.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val pageMonth = baseMonth.plusMonths((page - PAGER_MIDDLE).toLong())
                        val grid = remember(pageMonth, dayStyle, showGenshin) {
                            WanNianLi.monthGrid(pageMonth, dayStyle, showGenshin)
                        }
                        val terms = remember(pageMonth) { WanNianLi.termsOfMonth(pageMonth) }
                        val festivals = remember(pageMonth) { WanNianLi.festivalsOfMonth(pageMonth) }
                        val birthdays = remember(pageMonth, showGenshin) {
                            WanNianLi.genshinBirthdaysOfMonth(pageMonth, showGenshin)
                        }
                        MonthGrid(
                            grid = grid,
                            month = pageMonth,
                            selected = selected,
                            today = today,
                            onSelect = { picked ->
                                selected = picked.date
                                val target = PAGER_MIDDLE + ChronoUnit.MONTHS
                                    .between(baseMonth, YearMonth.from(picked.date)).toInt()
                                if (target != pagerState.currentPage) {
                                    scope.launch { pagerState.animateScrollToPage(target) }
                                }
                            }
                        )
                        MonthFooter(
                            terms = terms,
                            festivals = festivals,
                            genshinBirthdays = birthdays
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "黄帝纪元 = 公元年份 + 2697 · 周数按 ISO-8601（周一为首日）· 支持 1900-2100 年",
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = p.riceDim,
                fontSize = 11.sp
            )
        }

        // 右上角：显示设置 + 回到今天
        Row(
            Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "显示设置",
                    tint = p.gold
                )
            }
            TextButton(
                onClick = {
                    selected = today
                    scope.launch { pagerState.animateScrollToPage(PAGER_MIDDLE) }
                }
            ) {
                Text("今天", color = p.gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            settings = settings,
            onChange = onSettingsChange,
            onDismiss = { showSettings = false }
        )
    }
}

/** 显示设置：农历日期格式、廿卅写法、微件背景 */
@Composable
private fun SettingsDialog(
    settings: AppSettings,
    onChange: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val p = LocalPalette.current
    val style = settings.dayStyle
    val sample = buildString {
        append("九月").append(LunarDate(2026, 9, 9, false).dayName(style))
        append(" · 九月").append(LunarDate(2026, 9, 23, false).dayName(style))
        append(" · 九月").append(LunarDate(2026, 9, 30, false).dayName(style))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = p.card,
        titleContentColor = p.rice,
        textContentColor = p.rice,
        title = { Text("显示设置") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "主题",
                    color = p.gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                OptionRow("跟随系统", settings.themeMode == ThemeMode.SYSTEM) {
                    onChange(settings.copy(themeMode = ThemeMode.SYSTEM))
                }
                OptionRow("暗色（水墨）", settings.themeMode == ThemeMode.DARK) {
                    onChange(settings.copy(themeMode = ThemeMode.DARK))
                }
                OptionRow("亮色（宣纸，适合墨水屏）", settings.themeMode == ThemeMode.LIGHT) {
                    onChange(settings.copy(themeMode = ThemeMode.LIGHT))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChange(
                                settings.copy(
                                    showGenshinBirthdays = !settings.showGenshinBirthdays
                                )
                            )
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = settings.showGenshinBirthdays,
                        onCheckedChange = {
                            onChange(settings.copy(showGenshinBirthdays = it))
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "显示《原神》角色生日（120 位）",
                        color = p.rice,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "农历日期格式",
                    color = p.gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                OptionRow("九月九日（书面）", style.written) {
                    onChange(settings.copy(dayStyle = style.copy(written = true)))
                }
                OptionRow("九月初九（传统）", !style.written) {
                    onChange(settings.copy(dayStyle = style.copy(written = false)))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "「廿」字（廿一至廿九）",
                    color = p.gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                OptionRow("用「廿」", style.useNian) {
                    onChange(settings.copy(dayStyle = style.copy(useNian = true)))
                }
                OptionRow("用「二十」", !style.useNian) {
                    onChange(settings.copy(dayStyle = style.copy(useNian = false)))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "「卅」字（三十日）",
                    color = p.gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                OptionRow("用「卅」", style.useSa) {
                    onChange(settings.copy(dayStyle = style.copy(useSa = true)))
                }
                OptionRow("用「三十」", !style.useSa) {
                    onChange(settings.copy(dayStyle = style.copy(useSa = false)))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "微件背景",
                    color = p.gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                OptionRow(
                    "暗色卡片（默认）",
                    settings.widgetBackground == WidgetBackground.DARK
                ) {
                    onChange(settings.copy(widgetBackground = WidgetBackground.DARK))
                }
                OptionRow(
                    "半透明磨砂",
                    settings.widgetBackground == WidgetBackground.FROSTED
                ) {
                    onChange(settings.copy(widgetBackground = WidgetBackground.FROSTED))
                }
                OptionRow(
                    "透明（浅色文字，适合深色壁纸）",
                    settings.widgetBackground == WidgetBackground.CLEAR
                ) {
                    onChange(settings.copy(widgetBackground = WidgetBackground.CLEAR))
                }
                Spacer(Modifier.height(12.dp))
                Text("示例：$sample", color = p.goldBright, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成", color = p.gold) }
        }
    )
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    val p = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(4.dp))
        Text(label, color = p.rice, fontSize = 14.sp)
    }
}

/** 今日详情大卡：完整呈现两行标准格式 */
@Composable
private fun HeroCard(day: CalendarDay, today: LocalDate) {
    val p = LocalPalette.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = p.card),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, p.gold.copy(alpha = 0.45f))
    ) {
        Column(Modifier.padding(20.dp)) {
            // 第一行：西元2026年10月18日 第42周 星期日
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = p.gold, fontSize = 13.sp)) { append("西元") }
                    withStyle(
                        SpanStyle(
                            color = p.rice,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("${day.date.year}年${day.date.monthValue}月${day.date.dayOfMonth}日")
                    }
                    withStyle(SpanStyle(color = p.gold, fontSize = 13.sp)) { append("  第${day.isoWeek}周") }
                    withStyle(
                        SpanStyle(
                            color = p.rice,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    ) { append(" ${day.weekdayName}") }
                    // 阳历节日与角色生日附于「西元」行尾
                    day.solarFestival?.let {
                        withStyle(
                            SpanStyle(
                                color = p.cinnabarBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        ) { append(" $it") }
                    }
                    day.birthdayTag?.let {
                        withStyle(
                            SpanStyle(color = p.violet, fontSize = 14.sp)
                        ) { append(" $it") }
                    }
                },
                lineHeight = 34.sp
            )

            Spacer(Modifier.height(10.dp))

            // 第二行：黄帝纪元四七二三年九月九日丙午年（马）戊戌月乙丑日重阳节
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = p.gold, fontSize = 13.sp)) { append("黄帝纪元") }
                    withStyle(
                        SpanStyle(
                            color = p.rice,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) { append("${ChineseNumerals.digitWise(day.huangdiYear)}年") }
                    withStyle(
                        SpanStyle(
                            color = p.rice,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) { append("${day.lunar.monthName}${day.lunarDayName}") }
                    withStyle(SpanStyle(color = p.goldBright, fontSize = 14.sp)) {
                        append("${day.ganzhiYear}年（${day.zodiac}）")
                    }
                    withStyle(SpanStyle(color = p.goldBright, fontSize = 14.sp)) {
                        append("${day.ganzhiMonth}月")
                    }
                    withStyle(SpanStyle(color = p.goldBright, fontSize = 14.sp)) {
                        append("${day.ganzhiDay}日")
                    }
                    (day.lunarFestival ?: day.solarTerm)?.let {
                        withStyle(
                            SpanStyle(
                                color = p.cinnabarBright,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) { append("　$it") }
                    }
                },
                lineHeight = 30.sp
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = p.gold.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            val daysApart = ChronoUnit.DAYS.between(today, day.date)
            val details = listOf(
                "干　　支" to "${day.ganzhiYear}年 ${day.ganzhiMonth}月 ${day.ganzhiDay}日",
                "生　　肖" to day.zodiac,
                "农　　历" to "${day.ganzhiYear}年${day.lunar.monthName}${day.lunarDayName}",
                "节　　气" to (day.solarTerm ?: "—"),
                "节　　日" to listOfNotNull(day.solarFestival, day.lunarFestival)
                    .joinToString("、").ifEmpty { "—" },
                "角色生日" to (
                    if (day.genshinBirthdays.isEmpty()) "—"
                    else day.genshinBirthdays.joinToString("、")
                    ),
                "时　　辰" to Ganzhi.ofHour(LocalTime.now().hour),
                "黄帝纪元" to "${day.huangdiYear} 年",
                "距今天数" to if (daysApart == 0L) "今天" else "${daysApart} 天"
            )
            details.chunked(2).forEach { row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (label, value) ->
                        Column(Modifier.weight(1f)) {
                            Text(label, color = p.gold.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(
                                value,
                                color = p.rice,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    grid: List<CalendarDay>,
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (CalendarDay) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
        for (week in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (dow in 0 until 7) {
                    val cell = grid[week * 7 + dow]
                    DayCell(
                        day = cell,
                        isSelected = cell.date == selected,
                        isToday = cell.date == today,
                        inMonth = YearMonth.from(cell.date) == month,
                        modifier = Modifier.weight(1f),
                        onSelect = { onSelect(cell) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    inMonth: Boolean,
    modifier: Modifier,
    onSelect: () -> Unit
) {
    val p = LocalPalette.current
    val background = when {
        isToday -> p.todayGold
        isSelected -> p.cinnabar
        else -> Color.Transparent
    }
    val solarColor = when {
        isToday -> p.onToday
        isSelected -> p.onCinnabar
        !inMonth -> p.rice.copy(alpha = 0.28f)
        day.date.dayOfWeek == java.time.DayOfWeek.SUNDAY ||
            day.date.dayOfWeek == java.time.DayOfWeek.SATURDAY -> p.cinnabarBright
        else -> p.rice
    }
    val festival = day.solarFestival ?: day.lunarFestival
    val tag = festival ?: day.birthdayTag ?: day.solarTerm
    val tagColor = when {
        isToday -> p.onToday.copy(alpha = 0.75f)
        isSelected -> p.onCinnabar.copy(alpha = 0.9f)
        tag == null -> p.riceDim.copy(alpha = if (inMonth) 0.6f else 0.25f)
        festival != null -> p.cinnabarBright.copy(alpha = if (inMonth) 1f else 0.4f)
        day.genshinBirthdays.isNotEmpty() -> p.violet.copy(alpha = if (inMonth) 1f else 0.4f)
        else -> p.jade.copy(alpha = if (inMonth) 1f else 0.4f)
    }
    Box(
        modifier
            .aspectRatio(0.85f)
            .padding(2.dp)
            .background(background, CircleShape)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${day.date.dayOfMonth}",
                color = solarColor,
                fontSize = 15.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                tag ?: day.lunarDayName,
                color = tagColor,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MonthFooter(
    terms: List<Pair<LocalDate, String>>,
    festivals: List<Pair<LocalDate, String>>,
    genshinBirthdays: List<Pair<LocalDate, List<String>>>
) {
    val p = LocalPalette.current
    Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 12.dp)) {
        if (terms.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("节气 ", color = p.gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    terms.joinToString("　") { (d, name) -> "$name ${d.dayOfMonth}日" },
                    color = p.jade,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (festivals.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("节日 ", color = p.gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    festivals.joinToString("　") { (d, name) -> "$name ${d.dayOfMonth}日" },
                    color = p.cinnabarBright,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (genshinBirthdays.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("生日 ", color = p.gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                val shown = genshinBirthdays.take(3).joinToString("、") { (d, names) ->
                    "${names.joinToString("、")} ${d.dayOfMonth}日"
                }
                val more = if (genshinBirthdays.size > 3) " 等${genshinBirthdays.size}位" else ""
                Text(
                    shown + more,
                    color = p.violet,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
