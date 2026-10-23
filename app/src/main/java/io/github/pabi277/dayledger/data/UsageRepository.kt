package io.github.pabi277.dayledger.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.telecom.TelecomManager
import androidx.room.withTransaction
import java.time.LocalDate
import java.time.ZoneId

class UsageRepository(private val context: Context) {

    private val db = DayLedgerDatabase.getInstance(context)
    private val usageDao = db.usageDao()
    private val hourlyDao = db.hourlyDao()
    private val appInfoDao = db.appInfoDao()
    private val categoryDao = db.categoryDao()
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val pm = context.packageManager

    /**
     * Home screen, system UI and dialer are not "screen time" — Digital Wellbeing does not
     * count them, so neither does Today. See [SystemPackageRules] for why this is resolved
     * from the platform rather than guessed from package names.
     */
    private val isExcluded: (String) -> Boolean by lazy {
        val home = try {
            pm.resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                PackageManager.MATCH_DEFAULT_ONLY
            )?.activityInfo?.packageName
        } catch (e: Exception) {
            null
        }
        val dialer = try {
            (context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager)
                ?.defaultDialerPackage
        } catch (e: Exception) {
            null
        }
        SystemPackageRules.exclusion(home, dialer)
    }

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun seedCategoriesIfNeeded() {
        if (categoryDao.all().isEmpty()) {
            categoryDao.upsertAll(
                listOf(
                    CategoryEntity("earning", "Earning", "floor", 30, 0xFF4CAF50),
                    CategoryEntity("study", "Study", "floor", 30, 0xFF2196F3),
                    CategoryEntity("college", "College", "ceiling", 240, 0xFF9C27B0),
                    CategoryEntity("waste", "Social/Waste", "ceiling", 45, 0xFFF44336),
                    CategoryEntity("life", "Life", "ceiling", 120, 0xFFFF9800),
                    CategoryEntity("unclassified", "Unclassified", "ceiling", 60, 0xFF9E9E9E)
                )
            )
        }
    }

    suspend fun ingestRecentDays(days: Int = 7) {
        seedCategoriesIfNeeded()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val now = System.currentTimeMillis()
        val firstDayStart = today.minusDays(days.toLong())
            .atStartOfDay(zone).toInstant().toEpochMilli()

        // One read for the whole span. The extra [STATE_LOOKBACK_MS] before the first day
        // exists only so the aggregator knows whether the screen was on, and which package
        // was in front, at the moment each window starts.
        val events = readScreenEvents(
            from = maxOf(firstDayStart - STATE_LOOKBACK_MS, 0L),
            to = now
        )

        // Nothing readable at all: leave the stored days alone rather than replacing real
        // numbers with zeros. (A day with genuinely no events still gets rewritten to zero.)
        if (events.isEmpty()) return

        for (i in days downTo 0) {
            ingestDate(today.minusDays(i.toLong()), events = events, now = now)
        }
    }

    suspend fun ingestDate(
        date: LocalDate,
        events: List<ScreenEvent>? = null,
        now: Long = System.currentTimeMillis()
    ) {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val queryEnd = minOf(end, now)
        if (queryEnd <= start) return

        val timeline = events ?: readScreenEvents(
            from = maxOf(start - STATE_LOOKBACK_MS, 0L),
            to = queryEnd
        )
        val result = ScreenTimeAggregator(isExcluded, zone)
            .aggregate(timeline, start, queryEnd)

        val dateKey = date.toString()
        val entities = result.apps.map { app ->
            AppUsageEntity(
                packageName = app.packageName,
                dateKey = dateKey,
                foregroundMinutes = app.foregroundMinutes,
                pickups = app.pickups,
                longestSessionMin = app.longestSessionMin,
                source = "device"
            )
        }
        val hourlyEntities = (0 until 24).map { h ->
            HourlyUsageEntity(dateKey, h, result.hourlyMinutes[h])
        }

        // Replace, not accumulate: this is what removes the pre-fix inflated rows and the
        // launcher/dialer rows from days that are re-ingested.
        db.withTransaction {
            usageDao.deleteForDate(dateKey)
            if (entities.isNotEmpty()) usageDao.upsertAll(entities)
            hourlyDao.upsertAll(hourlyEntities)
        }

        for (app in result.apps) {
            ensureAppInfo(app.packageName)
        }
    }

    /** `MOVE_TO_FOREGROUND`/`ACTIVITY_RESUMED` and friends, mapped to [ScreenEvent]. */
    private fun readScreenEvents(from: Long, to: Long): List<ScreenEvent> {
        if (to <= from) return emptyList()
        val events = try {
            usm.queryEvents(from, to)
        } catch (e: Exception) {
            return emptyList()
        } ?: return emptyList()

        val out = ArrayList<ScreenEvent>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val type = when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> ScreenEventType.APP_FOREGROUND
                UsageEvents.Event.MOVE_TO_BACKGROUND -> ScreenEventType.APP_BACKGROUND
                UsageEvents.Event.SCREEN_INTERACTIVE -> ScreenEventType.SCREEN_ON
                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.DEVICE_SHUTDOWN -> ScreenEventType.SCREEN_OFF
                else -> continue
            }
            out.add(ScreenEvent(event.packageName, type, event.timeStamp))
        }
        return out
    }

    private suspend fun ensureAppInfo(packageName: String) {
        val label = try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
        appInfoDao.insertIfMissing(packageName, label)
        // Rows written before the label fix hold the package name as their label.
        if (label != packageName) appInfoDao.updatePlaceholderLabel(packageName, label)
    }

    suspend fun usageForDate(date: String): List<AppUsageEntity> = usageDao.forDate(date)

    /**
     * Today's rows with the Digital Wellbeing exclusions applied. Every surface that shows
     * "screen time" must read through this, so the total and the listed rows agree and the
     * comparison against the OS is like for like.
     */
    suspend fun screenUsageForDate(date: String): List<AppUsageEntity> =
        usageDao.forDate(date).filterNot { isExcluded(it.packageName) }

    suspend fun screenMinutesForDate(date: String): Int =
        screenUsageForDate(date).sumOf { it.foregroundMinutes }

    suspend fun hourlyForDate(date: String): List<HourlyUsageEntity> = hourlyDao.forDate(date)

    /** Raw stored sum, exclusions **not** applied. Prefer [screenMinutesForDate]. */
    suspend fun totalForDate(date: String): Int = usageDao.totalForDate(date)

    suspend fun appInfoAll(): List<AppInfoEntity> = appInfoDao.all()

    suspend fun categoriesAll(): List<CategoryEntity> = categoryDao.all()

    suspend fun updateCategory(id: String, budgetMin: Int, kind: String) =
        categoryDao.updateBudget(id, budgetMin, kind)

    suspend fun assignAppCategory(packageName: String, category: String) =
        appInfoDao.updateCategory(packageName, category)

    suspend fun updateAppLabelIfNeeded() {
        // no-op placeholder; labels are set during ingest
    }

    companion object {
        /**
         * How far before a window we look for the screen state and the resumed package.
         * A screen left on overnight is bounded by this, not by the window.
         */
        private const val STATE_LOOKBACK_MS = 48L * 60L * 60L * 1000L
    }
}
