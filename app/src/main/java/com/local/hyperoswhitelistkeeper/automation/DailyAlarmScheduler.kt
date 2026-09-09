package com.local.hyperoswhitelistkeeper.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZonedDateTime

object DailyAlarmScheduler {
    val scheduleHours: List<Int> = listOf(8, 12)

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    fun scheduleAll(context: Context, now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(AlarmManager::class.java)
        if (!canScheduleExactAlarms(appContext)) return false

        return runCatching {
            scheduleHours.forEach { hour ->
                val triggerAtMillis = nextTrigger(now, hour).toInstant().toEpochMilli()
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    alarmPendingIntent(appContext, hour),
                )
            }
        }.onFailure {
            cancelAll(appContext)
        }.isSuccess
    }

    fun cancelAll(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(AlarmManager::class.java)
        scheduleHours.forEach { hour ->
            alarmManager.cancel(alarmPendingIntent(appContext, hour))
        }
    }

    internal fun nextTrigger(now: ZonedDateTime, hour: Int): ZonedDateTime {
        val todayAtHour = now.toLocalDate().atTime(hour, 0).atZone(now.zone)
        return if (todayAtHour.isAfter(now)) todayAtHour else todayAtHour.plusDays(1)
    }

    private fun alarmPendingIntent(context: Context, hour: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION_DAILY_REPAIR)
            .putExtra(AlarmReceiver.EXTRA_SCHEDULE_HOUR, hour)
        return PendingIntent.getBroadcast(
            context,
            hour,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
