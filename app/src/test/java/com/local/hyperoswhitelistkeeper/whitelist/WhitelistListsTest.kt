package com.local.hyperoswhitelistkeeper.whitelist

import com.local.hyperoswhitelistkeeper.data.AppCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistListsTest {
    @Test
    fun `empty value plus GMS returns GMS`() {
        val result = WhitelistLists.merge(null, setOf(GMS))

        assertEquals(listOf(GMS), result.entries.toList())
        assertTrue(result.changed)
    }

    @Test
    fun `existing GMS plus GMS contains one entry and does not rewrite`() {
        val result = WhitelistLists.merge(GMS, setOf(GMS))

        assertEquals(listOf(GMS), result.entries.toList())
        assertFalse(result.changed)
        assertEquals(0, result.addedCount)
    }

    @Test
    fun `existing list is preserved and new item is appended`() {
        val result = WhitelistLists.merge("A,B", linkedSetOf("B", "C"))

        assertEquals("A,B,C", result.serialized)
    }

    @Test
    fun `spaces and blank values are removed`() {
        assertEquals(
            listOf("A", "B", "C"),
            WhitelistLists.parseList("A, B , ,C").toList(),
        )
    }

    @Test
    fun `duplicate source values are collapsed in insertion order`() {
        assertEquals(
            listOf(GMS, "com.zing.zalo"),
            WhitelistLists.parseList("$GMS,$GMS, com.zing.zalo").toList(),
        )
    }

    @Test
    fun `persistent process is routed only to process whitelist`() {
        val selected = setOf(AppCatalog.PERSISTENT_PROCESS, GMS)

        assertEquals(
            setOf(AppCatalog.PERSISTENT_PROCESS),
            WhitelistLists.requestedForKey(selected, WhitelistLists.processKey),
        )
        WhitelistLists.packageKeys.forEach { key ->
            val requested = WhitelistLists.requestedForKey(selected, key)
            assertEquals(setOf(GMS), requested)
            assertFalse(AppCatalog.PERSISTENT_PROCESS in requested)
        }
    }

    @Test
    fun `catalog is unique and excludes Antutu`() {
        assertEquals(AppCatalog.ids.size, AppCatalog.ids.toSet().size)
        assertFalse("com.antutu.ABenchMark" in AppCatalog.ids)
        assertTrue(AppCatalog.defaultSelection.all { it in AppCatalog.ids })
    }

    @Test
    fun `package is active only when present in all package whitelists`() {
        val values = mapOf(
            WhitelistLists.packageKeys[0] to "A,$GMS",
            WhitelistLists.packageKeys[1] to GMS,
            WhitelistLists.packageKeys[2] to "B",
            WhitelistLists.processKey to AppCatalog.PERSISTENT_PROCESS,
        )

        val statuses = WhitelistLists.activationStatuses(
            listOf(GMS, AppCatalog.PERSISTENT_PROCESS),
            values,
        )

        assertFalse(statuses.getValue(GMS))
        assertTrue(statuses.getValue(AppCatalog.PERSISTENT_PROCESS))
    }

    @Test
    fun `package is active when present in all package whitelists`() {
        val values = WhitelistLists.packageKeys.associateWith { "A,$GMS" }

        val status = WhitelistLists.activationStatuses(listOf(GMS), values)

        assertTrue(status.getValue(GMS))
    }

    private companion object {
        const val GMS = "com.google.android.gms"
    }
}
