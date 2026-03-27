package dev.androidbroadcast.claudex.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TimestampFormatterTest {

    @Test
    fun `non-zero timestamp returns a non-empty string`() {
        val result = System.currentTimeMillis().toRelativeTimestamp()
        assertNotNull(result)
        assert(result.isNotEmpty())
    }

    @Test
    fun `zero timestamp returns a date string`() {
        val result = 0L.toRelativeTimestamp()
        assertNotNull(result)
        assert(result.isNotEmpty())
    }

    @Test
    fun `current time returns Today`() {
        val result = System.currentTimeMillis().toRelativeTimestamp()
        assertEquals("Today", result)
    }
}
