package com.local.hyperoswhitelistkeeper.ui

import com.local.hyperoswhitelistkeeper.data.AppLanguage
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStringsTest {
    @Test
    fun `Vietnamese guide starts with Carrier Services and Auto Start`() {
        val guide = uiStrings(AppLanguage.VI).guideBody

        assertTrue(guide.startsWith("1. Cài Carrier Services"))
        assertTrue(guide.contains("2. Bật Tự khởi động (Auto Start)"))
    }

    @Test
    fun `English guide starts with Carrier Services and Auto Start`() {
        val guide = uiStrings(AppLanguage.EN).guideBody

        assertTrue(guide.startsWith("1. Install Carrier Services"))
        assertTrue(guide.contains("2. Enable Auto Start"))
    }
}
