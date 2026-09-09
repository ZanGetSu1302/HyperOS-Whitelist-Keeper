package com.local.hyperoswhitelistkeeper.whitelist

import com.local.hyperoswhitelistkeeper.data.AppCatalog
import com.local.hyperoswhitelistkeeper.model.EntryType

object WhitelistLists {
    val packageKeys = listOf(
        "MILLET_NO_RESTRICT_APP",
        "power_pkg_white_list",
        "cloud_network_priority_whitelist",
        "rt_pkg_white_list",
        "turbo_sched_core_app_list",
    )
    const val processKey = "power_proc_white_list"

    fun parseList(value: String?): LinkedHashSet<String> {
        if (value.isNullOrBlank()) return linkedSetOf()

        return value.split(',')
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .toCollection(LinkedHashSet())
    }

    fun merge(currentValue: String?, requested: Set<String>): MergeValue {
        val original = parseList(currentValue)
        val merged = LinkedHashSet(original)
        requested.forEach(merged::add)
        return MergeValue(
            entries = merged,
            changed = merged != original,
            addedCount = requested.count { it !in original },
        )
    }

    fun requestedForKey(selectedEntries: Set<String>, key: String): Set<String> =
        when (key) {
            processKey -> selectedEntries.filterTo(linkedSetOf()) {
                AppCatalog.typeOf(it) == EntryType.PROCESS
            }

            in packageKeys -> selectedEntries.filterTo(linkedSetOf()) {
                AppCatalog.typeOf(it) == EntryType.PACKAGE
            }

            else -> emptySet()
        }

    fun activationStatuses(
        entryIds: Collection<String>,
        valuesByKey: Map<String, String?>,
    ): Map<String, Boolean> {
        val parsedByKey = valuesByKey.mapValues { (_, value) -> parseList(value) }
        return entryIds.associateWith { id ->
            when (AppCatalog.typeOf(id)) {
                EntryType.PACKAGE -> packageKeys.all { key -> id in parsedByKey[key].orEmpty() }
                EntryType.PROCESS -> id in parsedByKey[processKey].orEmpty()
            }
        }
    }
}

data class MergeValue(
    val entries: LinkedHashSet<String>,
    val changed: Boolean,
    val addedCount: Int,
) {
    val serialized: String = entries.joinToString(",")
}
