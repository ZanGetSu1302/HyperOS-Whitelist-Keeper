package com.local.hyperoswhitelistkeeper.whitelist

import android.content.ContentValues
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.local.hyperoswhitelistkeeper.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(
    private val context: Context,
) {
    private val resolver = context.contentResolver

    fun canWrite(): Boolean = Settings.System.canWrite(context)

    suspend fun apply(selectedEntries: Set<String>): WhitelistResult = withContext(Dispatchers.IO) {
        if (!canWrite()) return@withContext WhitelistResult.PermissionMissing

        val requested = selectedEntries.toSet()
        val keys = WhitelistLists.packageKeys + WhitelistLists.processKey
        val backups = linkedMapOf<String, String?>()
        val writtenKeys = mutableListOf<String>()
        val changedEntries = linkedSetOf<String>()

        try {
            for (key in keys) {
                val requestedForKey = WhitelistLists.requestedForKey(requested, key)
                if (requestedForKey.isEmpty()) continue

                debug("Reading $key")
                val current = Settings.System.getString(resolver, key)
                backups[key] = current
                val original = WhitelistLists.parseList(current)
                val merged = WhitelistLists.merge(current, requestedForKey)

                debug("Existing entries: ${original.size}")
                debug("Requested entries: ${requestedForKey.size}")
                debug("Adding: ${merged.addedCount}")

                if (!merged.changed) continue

                requestedForKey.filterTo(changedEntries) { it !in original }
                val writeResult = writeSystemSetting(key, merged.serialized)
                debug("Write result: $writeResult")
                if (!writeResult) {
                    restoreBestEffort(backups, writtenKeys)
                    return@withContext WhitelistResult.Failure(null)
                }
                writtenKeys += key
            }

            if (writtenKeys.isEmpty()) return@withContext WhitelistResult.AlreadyApplied

            val verified = verifyOnCurrentDispatcher(requested)
            debug("Verify: ${if (verified) "success" else "failed"}")
            if (!verified) {
                restoreBestEffort(backups, writtenKeys)
                WhitelistResult.Failure(null)
            } else {
                WhitelistResult.Success(
                    addedCount = changedEntries.size,
                    unchangedCount = requested.size - changedEntries.size,
                )
            }
        } catch (exception: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Whitelist update failed (${exception.javaClass.simpleName})", exception)
            }
            restoreBestEffort(backups, writtenKeys)
            WhitelistResult.Failure(exception)
        }
    }

    suspend fun verify(selectedEntries: Set<String>): Boolean = withContext(Dispatchers.IO) {
        if (!canWrite()) return@withContext false
        try {
            verifyOnCurrentDispatcher(selectedEntries)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun activationStatuses(entryIds: Collection<String>): Map<String, Boolean> =
        withContext(Dispatchers.IO) {
            val keys = WhitelistLists.packageKeys + WhitelistLists.processKey
            val valuesByKey = keys.associateWith { key ->
                Settings.System.getString(resolver, key)
            }
            WhitelistLists.activationStatuses(entryIds, valuesByKey)
        }

    private fun verifyOnCurrentDispatcher(selectedEntries: Set<String>): Boolean {
        val keys = WhitelistLists.packageKeys + WhitelistLists.processKey
        return keys.all { key ->
            val requestedForKey = WhitelistLists.requestedForKey(selectedEntries, key)
            requestedForKey.isEmpty() || WhitelistLists.parseList(
                Settings.System.getString(resolver, key),
            ).containsAll(requestedForKey)
        }
    }

    private fun restoreBestEffort(backups: Map<String, String?>, writtenKeys: List<String>) {
        writtenKeys.asReversed().forEach { key ->
            runCatching {
                val previousValue = backups[key]
                if (previousValue == null) {
                    resolver.delete(
                        Settings.System.CONTENT_URI,
                        "${Settings.NameValueTable.NAME} = ?",
                        arrayOf(key),
                    )
                } else {
                    writeSystemSetting(key, previousValue)
                }
            }
        }
    }

    /**
     * Uses the same System-table write path as SetEdit: insert a name/value pair
     * through the Settings content provider. The subsequent read-back remains the
     * source of truth, so a provider response alone is never treated as success.
     */
    private fun writeSystemSetting(key: String, value: String): Boolean {
        val values = ContentValues(2).apply {
            put(Settings.NameValueTable.NAME, key)
            put(Settings.NameValueTable.VALUE, value)
        }
        return resolver.insert(Settings.System.CONTENT_URI, values) != null
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "WhitelistKeeper"
    }
}
