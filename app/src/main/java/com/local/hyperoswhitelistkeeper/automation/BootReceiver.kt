package com.local.hyperoswhitelistkeeper.automation

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.local.hyperoswhitelistkeeper.data.PreferencesRepository
import com.local.hyperoswhitelistkeeper.whitelist.WhitelistRepairRunner
import com.local.hyperoswhitelistkeeper.whitelist.WhitelistWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_TIME_CHANGED &&
            intent.action != Intent.ACTION_TIMEZONE_CHANGED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                WhitelistWorker.cancelLegacySchedule(appContext)
                val preferences = PreferencesRepository(appContext)
                if (
                    intent.action == Intent.ACTION_BOOT_COMPLETED &&
                    preferences.loadRunOnBootEnabled()
                ) {
                    WhitelistRepairRunner.run(appContext)
                }

                if (preferences.loadScheduledRunEnabled()) {
                    DailyAlarmScheduler.scheduleAll(appContext)
                } else {
                    DailyAlarmScheduler.cancelAll(appContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
