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

    @Test
    fun `Donate button has localized accessibility labels`() {
        assertTrue(uiStrings(AppLanguage.VI).donateDescription.contains("QR Donate"))
        assertTrue(uiStrings(AppLanguage.EN).donateDescription.contains("Donate QR"))
    }

    @Test
    fun `Automation options mention boot and exact schedule in both languages`() {
        val vi = uiStrings(AppLanguage.VI)
        val en = uiStrings(AppLanguage.EN)

        assertTrue(vi.runOnBoot.contains("khởi động"))
        assertTrue(vi.scheduledRunDescription.contains("08:00") && vi.scheduledRunDescription.contains("12:00"))
        assertTrue(en.runOnBoot.contains("boot"))
        assertTrue(en.scheduledRunDescription.contains("08:00") && en.scheduledRunDescription.contains("12:00"))
    }
}
