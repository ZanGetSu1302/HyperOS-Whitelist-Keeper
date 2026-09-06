package com.local.hyperoswhitelistkeeper.whitelist

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.local.hyperoswhitelistkeeper.data.PreferencesRepository
import java.util.concurrent.TimeUnit

class WhitelistWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val whitelistRepository = WhitelistRepository(applicationContext)
        if (!whitelistRepository.canWrite()) return Result.success()

        val selected = PreferencesRepository(applicationContext).loadSelectedPackages()
        return when (whitelistRepository.apply(selected)) {
            is WhitelistResult.Failure -> Result.retry()
            else -> Result.success()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "periodic_whitelist_repair"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<WhitelistWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
