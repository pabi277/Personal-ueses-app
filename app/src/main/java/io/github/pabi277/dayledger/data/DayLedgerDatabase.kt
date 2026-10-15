package io.github.pabi277.dayledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
        SettingEntity::class,
        NotificationRuleEntity::class,
        NotificationLogEntity::class
    ],
    version = 3,
    exportSchema = false
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

    companion object {
        @Volatile
        private var INSTANCE: DayLedgerDatabase? = null

        fun getInstance(context: Context): DayLedgerDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DayLedgerDatabase::class.java,
                    "dayledger.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
