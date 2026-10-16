package io.github.pabi277.dayledger.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class UsageRepository(private val context: Context) {

    private val db = DayLedgerDatabase.getInstance(context)
    private val usageDao = db.usageDao()
    private val hourlyDao = db.hourlyDao()
    private val appInfoDao = db.appInfoDao()
    private val categoryDao = db.categoryDao()
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val pm = context.packageManager

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
        val today = LocalDate.now()
        for (i in days downTo 0) {
            ingestDate(today.minusDays(i.toLong()))
        }
    }

    suspend fun ingestDate(date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        val queryEnd = minOf(end, now)

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, queryEnd)
            ?: emptyList()

        // Event-based pickups / longest session / hourly buckets
        val pickups = mutableMapOf<String, Int>()
        val longest = mutableMapOf<String, Int>()
        val openStart = mutableMapOf<String, Long>()
        val hourlyTotals = IntArray(24)

        val events = usm.queryEvents(start, queryEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    pickups[pkg] = (pickups[pkg] ?: 0) + 1
                    openStart[pkg] = event.timeStamp
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val s = openStart.remove(pkg)
                    if (s != null && event.timeStamp > s) {
                        val sessionMin = ((event.timeStamp - s) / 60000L).toInt()
                        if (sessionMin > (longest[pkg] ?: 0)) longest[pkg] = sessionMin
                        addSessionToHours(hourlyTotals, s, event.timeStamp)
                    }
                }
            }
        }
        // close any still-open sessions at queryEnd
        for ((pkg, s) in openStart) {
            if (queryEnd > s) {
                val sessionMin = ((queryEnd - s) / 60000L).toInt()
                if (sessionMin > (longest[pkg] ?: 0)) longest[pkg] = sessionMin
                addSessionToHours(hourlyTotals, s, queryEnd)
            }
        }

        val usageEntities = mutableListOf<AppUsageEntity>()
        for (u in stats) {
            val pkg = u.packageName ?: continue
            val minutes = (u.totalTimeInForeground / 60000L).toInt()
            if (minutes <= 0 && (pickups[pkg] ?: 0) == 0) continue
            usageEntities.add(
                AppUsageEntity(
                    packageName = pkg,
                    dateKey = date.toString(),
                    foregroundMinutes = minutes,
                    pickups = pickups[pkg] ?: 0,
                    longestSessionMin = longest[pkg] ?: 0,
                    source = "device"
                )
            )
            // ensure app info exists
            val label = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                pkg
            }
            appInfoDao.insertIfMissing(pkg, label)
        }

        if (usageEntities.isNotEmpty()) {
            usageDao.upsertAll(usageEntities)
        }

        // hourly
        val hourlyEntities = (0 until 24).map { h ->
            HourlyUsageEntity(date.toString(), h, hourlyTotals[h])
        }
        hourlyDao.upsertAll(hourlyEntities)
    }

    private fun addSessionToHours(totals: IntArray, start: Long, end: Long) {
        var t = start
        val cal = Calendar.getInstance()
        while (t < end) {
            cal.timeInMillis = t
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val nextHour = cal.apply {
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.HOUR_OF_DAY, 1)
            }.timeInMillis
            val chunkEnd = minOf(end, nextHour)
            val mins = ((chunkEnd - t) / 60000L).toInt()
            if (mins > 0) totals[hour] += mins
            t = chunkEnd
        }
    }

    suspend fun usageForDate(date: String): List<AppUsageEntity> = usageDao.forDate(date)

    suspend fun hourlyForDate(date: String): List<HourlyUsageEntity> = hourlyDao.forDate(date)

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
}
