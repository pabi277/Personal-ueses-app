package io.github.pabi277.dayledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.pabi277.dayledger.data.migrations.DatabaseMigrations

@Database(
    entities = [
        AppUsageEntity::class,
        HourlyUsageEntity::class,
        AppInfoEntity::class,
        CategoryEntity::class,
        BlockEntity::class,
        DayPlanEntity::class,
        TemplateEntity::class,
        TemplateItemEntity::class,
        LimitEventEntity::class,
        EarningEntryEntity::class,
        LearningEntryEntity::class,
        SettingEntity::class,
        NotificationRuleEntity::class,
        NotificationLogEntity::class
    ],
    version = DatabaseMigrations.CURRENT_VERSION,
    exportSchema = true
)
abstract class DayLedgerDatabase : RoomDatabase() {
    abstract fun usageDao(): AppUsageDao
    abstract fun hourlyDao(): HourlyUsageDao
    abstract fun appInfoDao(): AppInfoDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingDao(): SettingDao
    abstract fun notificationRuleDao(): NotificationRuleDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun blockDao(): BlockDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun templateDao(): TemplateDao
    abstract fun limitEventDao(): LimitEventDao
    abstract fun earningDao(): EarningDao
    abstract fun learningDao(): LearningDao

    companion object {
        @Volatile
        private var INSTANCE: DayLedgerDatabase? = null

        fun getInstance(context: Context): DayLedgerDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DayLedgerDatabase::class.java,
                    "dayledger.db"
                )
                    // Explicit, non-destructive v1 → v2 → v3 → v4 → v5 steps.
                    // There is deliberately NO fallbackToDestructiveMigration():
                    // an unknown schema must fail loudly instead of silently
                    // deleting the owner's history.
                    .addMigrations(*DatabaseMigrations.ALL)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
