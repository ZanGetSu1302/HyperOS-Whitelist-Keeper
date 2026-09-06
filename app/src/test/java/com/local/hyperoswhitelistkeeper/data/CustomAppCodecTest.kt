package com.local.hyperoswhitelistkeeper.data

import com.local.hyperoswhitelistkeeper.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomAppCodecTest {
    @Test
    fun `entry survives encode and decode`() {
        val entry = AppEntry(
            id = "com.example.messaging",
            label = "Ứng dụng: Tin nhắn",
        )

        assertEquals(entry, CustomAppCodec.decode(CustomAppCodec.encode(entry)))
    }

    @Test
    fun `invalid stored values are ignored`() {
        assertNull(CustomAppCodec.decode("missing length"))
        assertNull(CustomAppCodec.decode("99:short"))
        assertNull(CustomAppCodec.decode("3:com"))
    }
}
