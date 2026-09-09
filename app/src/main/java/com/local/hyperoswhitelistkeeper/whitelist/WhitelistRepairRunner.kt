package com.local.hyperoswhitelistkeeper.whitelist

import android.content.Context
import com.local.hyperoswhitelistkeeper.data.PreferencesRepository

object WhitelistRepairRunner {
    suspend fun run(context: Context): WhitelistResult {
        val appContext = context.applicationContext
        val whitelistRepository = WhitelistRepository(appContext)
        if (!whitelistRepository.canWrite()) return WhitelistResult.PermissionMissing

        val selected = PreferencesRepository(appContext).loadSelectedPackages()
        return whitelistRepository.apply(selected)
    }
}
