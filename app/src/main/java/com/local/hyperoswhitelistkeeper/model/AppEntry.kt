package com.local.hyperoswhitelistkeeper.model

enum class EntryType {
    PACKAGE,
    PROCESS,
}

data class AppEntry(
    val id: String,
    val label: String,
    val type: EntryType = EntryType.PACKAGE,
)
