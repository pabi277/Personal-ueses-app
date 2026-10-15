package io.github.pabi277.dayledger.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import io.github.pabi277.dayledger.data.NotificationLogEntity
import io.github.pabi277.dayledger.data.NotificationRuleEntity
import io.github.pabi277.dayledger.data.SettingEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationRepository(private val context: Context) {

    private val db = DayLedgerDatabase.getInstance(context)
    private val ruleDao = db.notificationRuleDao()
    private val logDao = db.notificationLogDao()
    private val settingDao = db.settingDao()

    companion object {
        const val CHANNEL_ID = "dayledger_reminders"
        const val ACTION_LESS_OFTEN = "io.github.pabi277.dayledger.LESS_OFTEN"
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Day Ledger reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    suspend fun seedDefaultsIfNeeded() {
        if (ruleDao.all().isEmpty()) {
            ruleDao.upsertAll(
                listOf(
                    NotificationRuleEntity(
                        id = "daily_summary",
                        type = "scheduled",
                        times = "21:00",
                        templateText = "Today: {total} 🌙 Waste {waste}m. Earning done ₹{earned}. Tomorrow's plan is one tap away."
                    ),
                    NotificationRuleEntity(
                        id = "waste_threshold",
                        type = "threshold",
                        times = "",
                        templateText = "Waste is over budget: {waste}m. Take a breath."
                    )
                )
            )
        }
        if (settingDao.get("daily_cap") == null) {
            settingDao.upsert(SettingEntity("daily_cap", "6"))
        }
    }

    fun dailyCap(): Int {
        // synchronous default; callers may read from DB via suspend
        return 6
    }

    suspend fun getDailyCap(): Int = settingDao.get("daily_cap")?.toIntOrNull() ?: 6

    fun isQuietNow(rule: NotificationRuleEntity): Boolean {
        return isQuiet(LocalTime.now(), rule.quietFrom, rule.quietTo)
    }

    private fun isQuiet(now: LocalTime, from: String, to: String): Boolean {
        return try {
            val f = LocalTime.parse(from)
            val t = LocalTime.parse(to)
            if (f.isBefore(t)) {
                now.isAfter(f) && now.isBefore(t)
            } else {
                now.isAfter(f) || now.isBefore(t)
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun canFire(rule: NotificationRuleEntity): Boolean {
        if (!rule.enabled) return false
        if (isQuietNow(rule)) return false
        val cap = getDailyCap()
        val today = LocalDate.now().toString()
        val firedToday = logDao.countForDate(today)
        if (firedToday >= cap) return false
        // dedup: threshold rules fire at most once per day
        if (rule.type == "threshold") {
            if (logDao.countForRuleDate(rule.id, today) > 0) return false
        }
        return true
    }

    fun fire(rule: NotificationRuleEntity, text: String) {
        ensureChannel()
        val pi = PendingIntent.getBroadcast(
            context,
            rule.id.hashCode(),
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_LESS_OFTEN
                putExtra("ruleId", rule.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Day Ledger")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Less often", pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(rule.id.hashCode(), notification)
    }

    suspend fun logFired(rule: NotificationRuleEntity) {
        logDao.insert(NotificationLogEntity(ruleId = rule.id, firedAt = System.currentTimeMillis(), dateKey = LocalDate.now().toString()))
    }

    suspend fun fireIfAllowed(rule: NotificationRuleEntity, text: String): Boolean {
        if (!canFire(rule)) return false
        fire(rule, text)
        logFired(rule)
        return true
    }

    fun scheduleDaily(rule: NotificationRuleEntity) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (time in rule.times.split(",")) {
            val trimmed = time.trim()
            if (trimmed.isEmpty()) continue
            val parts = trimmed.split(":")
            if (parts.size != 2) continue
            val h = parts[0].toIntOrNull() ?: continue
            val m = parts[1].toIntOrNull() ?: continue
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, h)
                set(java.util.Calendar.MINUTE, m)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            val intent = Intent(context, ScheduledNotificationReceiver::class.java).apply {
                putExtra("ruleId", rule.id)
            }
            val pi = PendingIntent.getBroadcast(
                context,
                (rule.id + trimmed).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                }
            } catch (e: SecurityException) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        }
    }

    suspend fun rules(): List<NotificationRuleEntity> = ruleDao.all()

    suspend fun setEnabled(id: String, enabled: Boolean) = ruleDao.setEnabled(id, enabled)

    suspend fun updateText(id: String, text: String) = ruleDao.updateText(id, text)

    suspend fun updateSchedule(id: String, times: String, qf: String, qt: String) =
        ruleDao.updateSchedule(id, times, qf, qt)

    suspend fun seedAndSchedule() {
        seedDefaultsIfNeeded()
        ensureChannel()
        for (rule in ruleDao.all()) {
            if (rule.enabled && rule.type == "scheduled") scheduleDaily(rule)
        }
    }
}
