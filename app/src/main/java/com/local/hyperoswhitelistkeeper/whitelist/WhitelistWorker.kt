package com.local.hyperoswhitelistkeeper.whitelist

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class WhitelistWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result =
        when (WhitelistRepairRunner.run(applicationContext)) {
            is WhitelistResult.Failure -> Result.retry()
            else -> Result.success()
        }

    companion object {
        private const val UNIQUE_WORK_NAME = "periodic_whitelist_repair"

        fun cancelLegacySchedule(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
