package com.yx.wannianli.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

/** 微件跨零刷新：在次日 00:00 前后的时间窗内触发一次 */
object WidgetAlarm {

    private const val WINDOW_MILLIS = 15 * 60 * 1000L

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextMidnight = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val pi = pendingIntent(context)
        alarmManager.cancel(pi)
        // setWindow 无需精确闹钟权限，15 分钟窗口内唤醒即可
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, nextMidnight, WINDOW_MILLIS, pi)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CalendarWidgetProvider::class.java)
            .setAction(CalendarWidgetProvider.ACTION_MIDNIGHT)
        return PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
