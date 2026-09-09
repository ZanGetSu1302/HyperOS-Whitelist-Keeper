package com.local.hyperoswhitelistkeeper.automation

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyAlarmSchedulerTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun `08 alarm stays on today when it has not passed`() {
        val now = ZonedDateTime.of(2026, 9, 9, 7, 59, 30, 0, zone)

        val next = DailyAlarmScheduler.nextTrigger(now, 8)

        assertEquals(ZonedDateTime.of(2026, 9, 9, 8, 0, 0, 0, zone), next)
    }

    @Test
    fun `08 alarm moves to tomorrow when current time is exactly 08`() {
        val now = ZonedDateTime.of(2026, 9, 9, 8, 0, 0, 0, zone)

        val next = DailyAlarmScheduler.nextTrigger(now, 8)

        assertEquals(ZonedDateTime.of(2026, 9, 10, 8, 0, 0, 0, zone), next)
    }

    @Test
    fun `12 alarm stays on today after 08 has passed`() {
        val now = ZonedDateTime.of(2026, 9, 9, 10, 30, 0, 0, zone)

        val next = DailyAlarmScheduler.nextTrigger(now, 12)

        assertEquals(ZonedDateTime.of(2026, 9, 9, 12, 0, 0, 0, zone), next)
    }
}
