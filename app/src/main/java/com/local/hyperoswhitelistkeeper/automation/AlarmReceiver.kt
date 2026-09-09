package com.local.hyperoswhitelistkeeper.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.local.hyperoswhitelistkeeper.data.PreferencesRepository
import com.local.hyperoswhitelistkeeper.whitelist.WhitelistRepairRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY_REPAIR) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val scheduledRunEnabled = PreferencesRepository(appContext)
                    .loadScheduledRunEnabled()
                if (scheduledRunEnabled) {
                    WhitelistRepairRunner.run(appContext)
                } else {
                    DailyAlarmScheduler.cancelAll(appContext)
                }
            } finally {
                val scheduledRunEnabled = runCatching {
                    PreferencesRepository(appContext).loadScheduledRunEnabled()
                }.getOrDefault(false)
                if (scheduledRunEnabled) {
                    DailyAlarmScheduler.scheduleAll(appContext)
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DAILY_REPAIR =
            "com.local.hyperoswhitelistkeeper.action.DAILY_REPAIR"
        const val EXTRA_SCHEDULE_HOUR = "schedule_hour"
    }
}
