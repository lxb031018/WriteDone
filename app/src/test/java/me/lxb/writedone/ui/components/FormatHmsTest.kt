package me.lxb.writedone.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatHmsTest {

    @Test
    fun `zero seconds`() {
        assertEquals("00:00:00", formatHms(0))
    }

    @Test
    fun `one second`() {
        assertEquals("00:00:01", formatHms(1))
    }

    @Test
    fun `fifty nine seconds`() {
        assertEquals("00:00:59", formatHms(59))
    }

    @Test
    fun `one minute`() {
        assertEquals("00:01:00", formatHms(60))
    }

    @Test
    fun `one minute one second`() {
        assertEquals("00:01:01", formatHms(61))
    }

    @Test
    fun `one hour`() {
        assertEquals("01:00:00", formatHms(3600))
    }

    @Test
    fun `one hour one minute one second`() {
        assertEquals("01:01:01", formatHms(3661))
    }

    @Test
    fun `max displayable value`() {
        assertEquals("99:59:59", formatHms(359999))
    }

    @Test
    fun `pomodoro test threshold 25 minutes`() {
        assertEquals("00:25:00", formatHms(1500))
    }
}
