package io.github.pabi277.dayledger.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.pabi277.dayledger.MainActivity
import io.github.pabi277.dayledger.data.checklist.ChecklistOccurrenceEntity
import io.github.pabi277.dayledger.reminders.ReminderActions
import io.github.pabi277.dayledger.reminders.ReminderProfile

/**
 * Builds and posts one checklist reminder, with the four actions the requirements list:
 * Done, Minimum, Later/Snooze and Skip. Reschedule lives in the app, which Android's
 * notification surface has no room for without hiding one of these.
 *
 * The notification is a *view* of persisted state, never the state itself: everything it
 * shows is re-derived from the database when an action arrives, so a stale notification can
 * never complete the wrong item.
 */
class ReminderNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "dayledger_checklist"
        private const val REQUEST_BASE = 31_000
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Checklist reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Core, Standard and Flexible checklist reminders"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun notificationId(occurrenceId: String): Int = occurrenceId.hashCode()

    /**
     * Post a reminder for [occurrence].
     *
     * @param audible false when the occurrence has used its sound/vibration budget; the
     *   notification is then posted silently and stays on screen until it is acted on.
     * @param ongoing true when this was the last audible fire, so the item remains visible.
     */
    fun post(
        occurrence: ChecklistOccurrenceEntity,
        profile: ReminderProfile,
        audible: Boolean,
        ongoing: Boolean,
        fireNumber: Int,
        maxAudible: Int
    ): Boolean {
        ensureChannel()
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(occurrence.title)
            .setContentText(subtitle(occurrence, profile, fireNumber, maxAudible, audible))
            .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle(occurrence, profile, fireNumber, maxAudible, audible)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(!audible)
            .setContentIntent(openIntent(occurrence.id))

        if (!audible) {
            builder.setSilent(true)
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        builder.addAction(0, "Done", actionIntent(occurrence.id, ReminderActions.ACTION_DONE, 1))
        builder.addAction(0, "Minimum", actionIntent(occurrence.id, ReminderActions.ACTION_MINIMUM, 2))
        builder.addAction(0, "Later", actionIntent(occurrence.id, ReminderActions.ACTION_LATER, 3))
        builder.addAction(0, "Skip", actionIntent(occurrence.id, ReminderActions.ACTION_SKIP, 4))

        return try {
            NotificationManagerCompat.from(context)
                .notify(notificationId(occurrence.id), builder.build())
            true
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS revoked after the check. Nothing was delivered; the caller
            // must not record a fire.
            false
        }
    }

    fun cancel(occurrenceId: String) {
        runCatching { NotificationManagerCompat.from(context).cancel(notificationId(occurrenceId)) }
    }

    private fun subtitle(
        occurrence: ChecklistOccurrenceEntity,
        profile: ReminderProfile,
        fireNumber: Int,
        maxAudible: Int,
        audible: Boolean
    ): String {
        val domain = occurrence.domain.takeIf { it.isNotBlank() }?.let { "$it · " } ?: ""
        val minimum = occurrence.minimumLabel?.takeIf { it.isNotBlank() }
            ?.let { "Minimum: $it · " } ?: ""
        val cadence = when {
            !audible -> "no more sounds today for this one"
            maxAudible <= 1 -> "one gentle reminder"
            else -> "$profile.label · $fireNumber of $maxAudible"
        }
        return "$domain$minimum$cadence"
    }

    private fun openIntent(occurrenceId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_OCCURRENCE_ID, occurrenceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionIntent(occurrenceId: String, action: String, slot: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderScheduler.EXTRA_OCCURRENCE_ID, occurrenceId)
            data = android.net.Uri.parse("dayledger://reminder/$occurrenceId/$slot")
        }
        return PendingIntent.getBroadcast(
            context,
            occurrenceId.hashCode() * 10 + slot,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
