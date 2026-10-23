package io.github.pabi277.dayledger.data

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * One usage-stats signal, reduced to the four things a screen-time measurement needs.
 *
 * Deliberately free of any `android.app.usage` type so the whole measurement can be
 * unit-tested on the JVM with fictional packages and fictional timestamps.
 */
enum class ScreenEventType {
    /** `ACTIVITY_RESUMED` / `MOVE_TO_FOREGROUND`. */
    APP_FOREGROUND,

    /** `ACTIVITY_PAUSED` / `MOVE_TO_BACKGROUND`. */
    APP_BACKGROUND,

    /** `SCREEN_INTERACTIVE`. */
    SCREEN_ON,

    /** `SCREEN_NON_INTERACTIVE` or `DEVICE_SHUTDOWN`. */
    SCREEN_OFF
}

data class ScreenEvent(
    val packageName: String?,
    val type: ScreenEventType,
    val timestamp: Long
)

data class AppScreenTime(
    val packageName: String,
    val foregroundMinutes: Int,
    val pickups: Int,
    val longestSessionMin: Int
)

data class ScreenTimeResult(
    val apps: List<AppScreenTime>,
    val hourlyMinutes: List<Int>,
    val totalMinutes: Int
)

/**
 * Measures **screen-on foreground time**, which is what Android's Digital Wellbeing
 * shows and what kill test P0 compares against.
 *
 * Why this exists (owner-reported failure, 2026-10-23): the first implementation summed
 * `UsageStats.totalTimeInForeground`. That number is a daily-diary accumulator, not
 * screen time — it keeps growing while the screen is off (foreground services, and on
 * several OEM ROMs a package that is simply left resumed), and a `queryUsageStats`
 * request returns whole interval files rather than a range clipped to the request. On
 * the owner's device it read 4 h 46 m against Digital Wellbeing's 2 h 25 m.
 *
 * The rules here, in order of how much they mattered:
 *
 * 1. Time only accrues while **the screen is on**. A `SCREEN_OFF` stops the clock even
 *    if the package is still resumed; a later `SCREEN_ON` resumes it. This alone removes
 *    the "left open for four hours overnight" inflation.
 * 2. Time only accrues for **the package that is in front**, and never for a package the
 *    [isExcluded] predicate rejects (home screen, system UI, dialer).
 * 3. Nothing outside `[windowStart, windowEnd]` is ever counted, so a diary entry that
 *    spans midnight cannot leak a previous day's minutes into today.
 * 4. If the screen state at `windowStart` is unknown (no `SCREEN_INTERACTIVE` before or
 *    inside the window), the default is **off**, i.e. count nothing. Under-reporting a
 *    boundary is safer than the four-hour over-report it replaces.
 * 5. Minutes are allocated from **one running total** across apps, and the 24 hourly
 *    buckets the same way, so "the rows add up to the total" and "the bars add up to the
 *    total" are true by construction rather than approximately true.
 */
class ScreenTimeAggregator(
    private val isExcluded: (String) -> Boolean = { false },
    private val zone: ZoneId = ZoneId.systemDefault()
) {

    fun aggregate(
        events: List<ScreenEvent>,
        windowStart: Long,
        windowEnd: Long
    ): ScreenTimeResult {
        val sorted = events.sortedBy { it.timestamp }
        val hourlyMillis = LongArray(24)
        val millisPerApp = LinkedHashMap<String, Long>()
        val longestPerApp = HashMap<String, Long>()
        val pickupsPerApp = HashMap<String, Int>()

        var screenOn = false
        var current: String? = null
        var cursor = windowStart
        // -1 means "no accrual segment open".
        var segmentStart = -1L

        /** Credit `[cursor, to)` to whatever is in front, if the screen is on. */
        fun advance(to: Long) {
            if (to <= cursor) return
            val pkg = current
            if (screenOn && pkg != null && !isExcluded(pkg)) {
                millisPerApp[pkg] = (millisPerApp[pkg] ?: 0L) + (to - cursor)
                addToHours(hourlyMillis, cursor, to)
                if (segmentStart < 0L) segmentStart = cursor
                val length = to - segmentStart
                if (length > (longestPerApp[pkg] ?: 0L)) longestPerApp[pkg] = length
            } else {
                segmentStart = -1L
            }
            cursor = to
        }

        fun applyEvent(event: ScreenEvent, countPickup: Boolean) {
            when (event.type) {
                ScreenEventType.APP_FOREGROUND -> {
                    val pkg = event.packageName ?: return
                    if (current != pkg) {
                        if (countPickup) pickupsPerApp[pkg] = (pickupsPerApp[pkg] ?: 0) + 1
                        current = pkg
                    }
                }
                ScreenEventType.APP_BACKGROUND -> {
                    if (event.packageName == null || event.packageName == current) current = null
                }
                ScreenEventType.SCREEN_ON -> screenOn = true
                ScreenEventType.SCREEN_OFF -> screenOn = false
            }
        }

        for (event in sorted) {
            if (event.timestamp <= windowStart) {
                // Before the window these events only establish the starting state.
                applyEvent(event, countPickup = false)
                continue
            }
            if (event.timestamp >= windowEnd) break
            advance(event.timestamp)
            applyEvent(event, countPickup = true)
        }
        advance(windowEnd)

        // Minutes per app from a single running total, largest first, so the sum of the
        // rows is exactly the floored device total — no minute lost to per-app rounding.
        val minutesPerApp = HashMap<String, Int>()
        var runningMillis = 0L
        var flooredBefore = 0L
        for ((pkg, ms) in millisPerApp.entries.sortedByDescending { it.value }) {
            runningMillis += ms
            val floored = runningMillis / 60_000L
            minutesPerApp[pkg] = (floored - flooredBefore).toInt()
            flooredBefore = floored
        }
        val totalMinutes = flooredBefore.toInt()

        val apps = millisPerApp.keys.map { pkg ->
            AppScreenTime(
                packageName = pkg,
                foregroundMinutes = minutesPerApp[pkg] ?: 0,
                pickups = pickupsPerApp[pkg] ?: 0,
                longestSessionMin = ((longestPerApp[pkg] ?: 0L) / 60_000L).toInt()
            )
        }
            .filter { it.foregroundMinutes > 0 || it.pickups > 0 }
            .sortedWith(compareByDescending<AppScreenTime> { it.foregroundMinutes }
                .thenBy { it.packageName })

        return ScreenTimeResult(
            apps = apps,
            hourlyMinutes = minutesByCumulativeFloor(hourlyMillis),
            totalMinutes = totalMinutes
        )
    }

    /**
     * Whole minutes per hour from running totals, so the 24 bars add up to the same
     * floored total as the app rows instead of dropping a minute at every boundary.
     */
    private fun minutesByCumulativeFloor(hourlyMillis: LongArray): List<Int> {
        var running = 0L
        var flooredBefore = 0L
        return hourlyMillis.map { ms ->
            running += ms
            val floored = running / 60_000L
            val minutes = (floored - flooredBefore).toInt()
            flooredBefore = floored
            minutes
        }
    }

    private fun addToHours(totals: LongArray, start: Long, end: Long) {
        var t = start
        while (t < end) {
            val zoned = Instant.ofEpochMilli(t).atZone(zone)
            val nextHour = zoned.truncatedTo(ChronoUnit.HOURS)
                .plusHours(1)
                .toInstant()
                .toEpochMilli()
            val chunkEnd = minOf(end, nextHour)
            if (zoned.hour in 0..23) totals[zoned.hour] += chunkEnd - t
            t = chunkEnd
        }
    }
}
