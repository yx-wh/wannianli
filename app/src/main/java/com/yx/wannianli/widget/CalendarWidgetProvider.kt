package com.yx.wannianli.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.yx.wannianli.MainActivity
import com.yx.wannianli.R
import com.yx.wannianli.SettingsRepository
import com.yx.wannianli.WidgetBackground
import com.yx.wannianli.calendar.WanNianLi
import java.time.LocalDate

/**
 * 桌面微件基类（4×2）。
 * 另有 4×1、2×2 两个子类提供不同尺寸；三者共享刷新逻辑，
 * 跨零 / 日期变更 / 开机广播由本类统一刷新全部实例。
 */
open class CalendarWidgetProvider : AppWidgetProvider() {

    /** 本尺寸对应的布局 */
    open val layoutRes: Int = R.layout.calendar_widget_4x2

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateWidget(context, appWidgetManager, it, layoutRes) }
        WidgetAlarm.schedule(context)
    }

    override fun onEnabled(context: Context) {
        WidgetAlarm.schedule(context)
    }

    override fun onDisabled(context: Context) {
        // 仅当三种尺寸的微件都被移除后，才停止跨零定时刷新
        val manager = AppWidgetManager.getInstance(context)
        val anyLeft = providerClasses.any {
            manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty()
        }
        if (!anyLeft) WidgetAlarm.cancel(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> refreshAll(context)

            Intent.ACTION_BOOT_COMPLETED -> {
                refreshAll(context)
                WidgetAlarm.schedule(context)
            }

            ACTION_MIDNIGHT -> refreshAll(context)
        }
    }

    companion object {
        const val ACTION_MIDNIGHT = "com.yx.wannianli.action.WIDGET_MIDNIGHT"

        /** 三种尺寸的提供者 */
        private val providerClasses = listOf(
            CalendarWidgetProvider::class.java,
            CalendarWidgetProvider4x1::class.java,
            CalendarWidgetProvider2x2::class.java
        )

        private fun layoutResOf(cls: Class<out AppWidgetProvider>): Int = when (cls) {
            CalendarWidgetProvider4x1::class.java -> R.layout.calendar_widget_4x1
            CalendarWidgetProvider2x2::class.java -> R.layout.calendar_widget_2x2
            else -> R.layout.calendar_widget_4x2
        }

        /** 刷新全部微件实例（三种尺寸） */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            providerClasses.forEach { cls ->
                val layoutRes = layoutResOf(cls)
                manager.getAppWidgetIds(ComponentName(context, cls))
                    .forEach { updateWidget(context, manager, it, layoutRes) }
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            layoutRes: Int
        ) {
            val settings = SettingsRepository.load(context)
            val day = WanNianLi.of(
                LocalDate.now(),
                settings.dayStyle,
                settings.showGenshinBirthdays
            )
            val views = RemoteViews(context.packageName, layoutRes)

            when (layoutRes) {
                R.layout.calendar_widget_4x1 -> {
                    // 紧凑单行
                    views.setTextViewText(R.id.widget_line1, day.line1)
                    views.setTextViewText(R.id.widget_line2, day.compactLine)
                }
                R.layout.calendar_widget_2x2 -> {
                    // 窄布局：日期与周次分两行
                    views.setTextViewText(R.id.widget_line1, day.line1Date)
                    views.setTextViewText(R.id.widget_line1b, day.line1Week)
                    views.setTextViewText(R.id.widget_line2, day.widgetLine2)
                    views.setTextViewText(R.id.widget_line3, day.widgetLine3)
                }
                else -> {
                    views.setTextViewText(R.id.widget_line1, day.line1)
                    views.setTextViewText(R.id.widget_line2, day.widgetLine2)
                    views.setTextViewText(R.id.widget_line3, day.widgetLine3)
                }
            }

            // 背景（可在设置中改为半透明 / 透明）
            val bgRes = when (settings.widgetBackground) {
                WidgetBackground.DARK -> R.drawable.widget_bg
                WidgetBackground.FROSTED -> R.drawable.widget_bg_frosted
                WidgetBackground.CLEAR -> R.drawable.widget_bg_clear
            }
            views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}

/** 4×1 紧凑微件 */
class CalendarWidgetProvider4x1 : CalendarWidgetProvider() {
    override val layoutRes: Int = R.layout.calendar_widget_4x1
}

/** 2×2 微件 */
class CalendarWidgetProvider2x2 : CalendarWidgetProvider() {
    override val layoutRes: Int = R.layout.calendar_widget_2x2
}
