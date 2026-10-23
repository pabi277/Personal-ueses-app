package io.github.pabi277.dayledger.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Unit tests for the screen-on foreground measurement that kill test P0 compares against
 * Android's Digital Wellbeing.
 *
 * Written against the owner-reported failure of 2026-10-23 (Today 4 h 46 m vs Digital
 * Wellbeing 2 h 25 m at the same minute, WhatsApp 1 h 00 m vs 17 m). Every package name
 * and timestamp here is fictional; nothing is seeded from the device.
 */
class ScreenTimeAggregatorTest {

    private val utc = ZoneOffset.UTC
    private val midnight = ts(0, 0)

    private val aggregator = ScreenTimeAggregator(zone = utc)

    private fun ts(hour: Int, minute: Int = 0, second: Int = 0): Long =
        LocalDateTime.of(2026, 10, 23, hour, minute, second)
            .toInstant(utc)
            .toEpochMilli()

    /** 23:00 the night before, for the midnight-boundary case. */
    private fun tsPrevDay(hour: Int, minute: Int = 0): Long =
        LocalDateTime.of(2026, 10, 22, hour, minute)
            .toInstant(utc)
            .toEpochMilli()

    private fun on(at: Long) = ScreenEvent(null, ScreenEventType.SCREEN_ON, at)
    private fun off(at: Long) = ScreenEvent(null, ScreenEventType.SCREEN_OFF, at)
    private fun fg(pkg: String, at: Long) = ScreenEvent(pkg, ScreenEventType.APP_FOREGROUND, at)
    private fun bg(pkg: String, at: Long) = ScreenEvent(pkg, ScreenEventType.APP_BACKGROUND, at)

    private fun minutesOf(result: ScreenTimeResult, pkg: String): Int =
        result.apps.firstOrNull { it.packageName == pkg }?.foregroundMinutes ?: 0

    @Test
    fun `a package left in the foreground does not accrue while the screen is off`() {
        // The regression that caused the P0 failure: the old code closed every open
        // session at the query moment, so a package left resumed at 08:05 was credited
        // until refresh — four hours later in the owner's case.
        // 5 minutes before the screen went off, then 30 more once it came back on at
        // 11:00 — the three dark hours in between are the part the old code counted.
        val result = aggregator.aggregate(
            listOf(on(ts(7, 0)), fg("com.example.chat", ts(8, 0)), off(ts(8, 5)), on(ts(11, 0))),
            midnight,
            ts(11, 30)
        )
        assertEquals(35, minutesOf(result, "com.example.chat"))
        assertEquals(35, result.totalMinutes)
    }

    @Test
    fun `accrual resumes when the screen comes back on for the same package`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(7, 0)),
                fg("com.example.video", ts(8, 0)),
                off(ts(8, 5)),
                on(ts(8, 20))
            ),
            midnight,
            ts(8, 30)
        )
        assertEquals(15, minutesOf(result, "com.example.video"))
    }

    @Test
    fun `unknown screen state at the window start counts nothing`() {
        // No SCREEN_ON was ever seen, so the screen state is unknown. Counting nothing is
        // the deliberate choice: under-reporting a boundary beats the old four-hour
        // over-report.
        val result = aggregator.aggregate(
            listOf(fg("com.example.reader", ts(7, 0))),
            midnight,
            ts(8, 0)
        )
        assertEquals(0, result.totalMinutes)
        assertTrue(result.apps.isEmpty())
    }

    @Test
    fun `screen state established before the window is honoured`() {
        val result = aggregator.aggregate(
            listOf(on(ts(6, 0)), fg("com.example.reader", ts(7, 0))),
            ts(7, 0),
            ts(7, 30)
        )
        assertEquals(30, minutesOf(result, "com.example.reader"))
    }

    @Test
    fun `switching apps splits time between them`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(8, 55)),
                fg("com.example.reader", ts(9, 0)),
                fg("com.example.chat", ts(9, 10)),
                bg("com.example.chat", ts(9, 25))
            ),
            midnight,
            ts(10, 0)
        )
        assertEquals(10, minutesOf(result, "com.example.reader"))
        assertEquals(15, minutesOf(result, "com.example.chat"))
        assertEquals(25, result.totalMinutes)
    }

    @Test
    fun `a background event stops accrual immediately`() {
        val result = aggregator.aggregate(
            listOf(on(ts(8, 55)), fg("com.example.reader", ts(9, 0)), bg("com.example.reader", ts(9, 5))),
            midnight,
            ts(10, 0)
        )
        assertEquals(5, minutesOf(result, "com.example.reader"))
    }

    @Test
    fun `hourly buckets split at the hour boundary and agree with the total`() {
        val result = aggregator.aggregate(
            listOf(on(ts(9, 0)), fg("com.example.reader", ts(9, 50))),
            midnight,
            ts(10, 20)
        )
        assertEquals(10, result.hourlyMinutes[9])
        assertEquals(20, result.hourlyMinutes[10])
        assertEquals(30, result.hourlyMinutes.sum())
        assertEquals(result.totalMinutes, result.hourlyMinutes.sum())
    }

    @Test
    fun `excluded packages are measured as nothing and never listed`() {
        val excluding = ScreenTimeAggregator(
            isExcluded = { it == "com.example.launcher" },
            zone = utc
        )
        val result = excluding.aggregate(
            listOf(
                on(ts(9, 0)),
                fg("com.example.launcher", ts(9, 0)),
                fg("com.example.chat", ts(9, 10))
            ),
            midnight,
            ts(9, 30)
        )
        assertEquals(20, result.totalMinutes)
        assertTrue(result.apps.none { it.packageName == "com.example.launcher" })
        assertEquals(result.totalMinutes, result.hourlyMinutes.sum())
    }

    @Test
    fun `sub-minute sessions add up instead of vanishing`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(9, 0)),
                fg("com.example.notes", ts(9, 0)),
                bg("com.example.notes", ts(9, 0, 30)),
                fg("com.example.notes", ts(9, 1)),
                bg("com.example.notes", ts(9, 1, 30)),
                fg("com.example.notes", ts(9, 2))
            ),
            midnight,
            ts(9, 2, 30)
        )
        // Three 30-second sessions: 90 s floors to 1 minute, not 0.
        assertEquals(1, minutesOf(result, "com.example.notes"))
    }

    @Test
    fun `longest session is the longest continuous stretch`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(9, 0)),
                fg("com.example.video", ts(9, 0)),
                off(ts(9, 10)),
                on(ts(9, 40))
            ),
            midnight,
            ts(10, 0)
        )
        val app = result.apps.first { it.packageName == "com.example.video" }
        assertEquals(30, app.foregroundMinutes)
        assertEquals(20, app.longestSessionMin)
    }

    @Test
    fun `pickups count foreground transitions and ignore repeats`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(9, 0)),
                fg("com.example.chat", ts(9, 0)),
                fg("com.example.chat", ts(9, 1)),
                fg("com.example.reader", ts(9, 2)),
                fg("com.example.chat", ts(9, 3))
            ),
            midnight,
            ts(9, 10)
        )
        val chat = result.apps.first { it.packageName == "com.example.chat" }
        assertEquals(2, chat.pickups)
    }

    @Test
    fun `accrual is clipped at the window end`() {
        val result = aggregator.aggregate(
            listOf(on(ts(9, 0)), fg("com.example.reader", ts(10, 0))),
            midnight,
            ts(10, 30)
        )
        assertEquals(30, minutesOf(result, "com.example.reader"))
    }

    @Test
    fun `events after the window are ignored`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(9, 0)),
                fg("com.example.reader", ts(10, 0)),
                bg("com.example.reader", ts(10, 10)),
                fg("com.example.reader", ts(11, 30))
            ),
            midnight,
            ts(11, 0)
        )
        assertEquals(10, minutesOf(result, "com.example.reader"))
    }

    @Test
    fun `a diary entry running past midnight cannot leak into the next day`() {
        // The window starts at 00:00 while the package has been in front since 23:00 the
        // night before; the screen is on throughout.
        val result = aggregator.aggregate(
            listOf(on(tsPrevDay(23)), fg("com.example.reader", tsPrevDay(23))),
            midnight,
            ts(0, 45)
        )
        assertEquals(45, minutesOf(result, "com.example.reader"))
    }

    @Test
    fun `the hourly bars, the app rows and the total agree exactly even with odd seconds`() {
        val result = aggregator.aggregate(
            listOf(
                on(ts(9, 0)),
                fg("com.example.reader", ts(9, 0, 20)),
                bg("com.example.reader", ts(9, 20, 40)),
                fg("com.example.chat", ts(9, 21, 5))
            ),
            midnight,
            ts(9, 59, 59)
        )
        assertEquals(result.totalMinutes, result.hourlyMinutes.sum())
        assertEquals(result.totalMinutes, result.apps.sumOf { it.foregroundMinutes })
    }
}
