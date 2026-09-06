package com.local.hyperoswhitelistkeeper.data

import com.local.hyperoswhitelistkeeper.model.AppEntry

internal object CustomAppCodec {
    fun encode(entry: AppEntry): String = "${entry.id.length}:${entry.id}${entry.label}"

    fun decode(value: String): AppEntry? {
        val separatorIndex = value.indexOf(':')
        if (separatorIndex <= 0) return null

        val idLength = value.substring(0, separatorIndex).toIntOrNull() ?: return null
        val idStart = separatorIndex + 1
        val idEnd = idStart + idLength
        if (idLength <= 0 || idEnd > value.length) return null

        val id = value.substring(idStart, idEnd).trim()
        val label = value.substring(idEnd).trim()
        if (id.isBlank() || label.isBlank()) return null

        return AppEntry(id = id, label = label)
    }
}
