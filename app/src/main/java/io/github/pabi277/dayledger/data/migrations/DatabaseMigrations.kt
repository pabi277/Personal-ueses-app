package io.github.pabi277.dayledger.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit, non-destructive upgrade steps for the Day Ledger database.
 *
 * Every historical step is a pure table **addition** — verified against the git
 * history (Phase 1 `fe58d9f` v1, Phase 2 `ad0948a` v2, Phase 3 `8974737` v3,
 * Phase 4 `5970aed` v4, Phase 5 `1ff9a87` v5; Phase 6 `3aec2e0` still v5). No
 * column was ever renamed, retyped or dropped, so no existing row is rewritten and
 * no table is recreated. Nothing here deletes data.
 *
 * The statements are exposed as plain SQL lists so the JVM migration tests can run
 * exactly the SQL that Room runs on a device, without an emulator.
 */
object DatabaseMigrations {

    /** Current database version. Phase 8 adds the checklist tables (v5 → v6). */
    const val CURRENT_VERSION = 6

    /** Every version this build can open. A version outside this set fails. */
    val SUPPORTED_VERSIONS: List<Int> = listOf(1, 2, 3, 4, 5, 6)

    /**
     * v1 → v2 (Phase 2): notifications. Adds `settings`, `notification_rules`,
     * `notification_log`.
     */
    val V1_TO_V2: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))",
        "CREATE TABLE IF NOT EXISTS `notification_rules` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, " +
            "`enabled` INTEGER NOT NULL, `times` TEXT NOT NULL, `days` TEXT NOT NULL, " +
            "`scope` TEXT NOT NULL, `tone` TEXT NOT NULL, `templateText` TEXT NOT NULL, " +
            "`quietFrom` TEXT NOT NULL, `quietTo` TEXT NOT NULL, `dailyCap` INTEGER NOT NULL, " +
            "PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `notification_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`ruleId` TEXT NOT NULL, `firedAt` INTEGER NOT NULL, `dateKey` TEXT NOT NULL)"
    )

    /**
     * v2 → v3 (Phase 3): planning. Adds `blocks`, `day_plans`, `templates`,
     * `template_items`.
     */
    val V2_TO_V3: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `blocks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateKey` TEXT NOT NULL, `startMin` INTEGER NOT NULL, `endMin` INTEGER NOT NULL, " +
            "`title` TEXT NOT NULL, `kind` TEXT NOT NULL, `reminder` INTEGER NOT NULL)",
        "CREATE TABLE IF NOT EXISTS `day_plans` (`dateKey` TEXT NOT NULL, `templateId` TEXT, " +
            "`note` TEXT NOT NULL, PRIMARY KEY(`dateKey`))",
        "CREATE TABLE IF NOT EXISTS `templates` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
            "`lastUsedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `template_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`templateId` TEXT NOT NULL, `startMin` INTEGER NOT NULL, `endMin` INTEGER NOT NULL, " +
            "`title` TEXT NOT NULL)"
    )

    /** v3 → v4 (Phase 4): blocking. Adds `limit_events`. */
    val V3_TO_V4: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `limit_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateKey` TEXT NOT NULL, `packageName` TEXT NOT NULL, `limitMin` INTEGER NOT NULL, " +
            "`reachedAt` INTEGER NOT NULL, `action` TEXT NOT NULL, `blockDurationMin` INTEGER NOT NULL, " +
            "`unlockedAt` INTEGER NOT NULL, `overrideType` TEXT NOT NULL)"
    )

    /** v4 → v5 (Phase 5): earnings and learning. Adds `earnings`, `learning`. */
    val V4_TO_V5: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `earnings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateKey` TEXT NOT NULL, `source` TEXT NOT NULL, `taskType` TEXT NOT NULL, " +
            "`appId` TEXT, `amountPaise` INTEGER NOT NULL, `loggedAt` INTEGER NOT NULL, " +
            "`attributedMinutes` INTEGER NOT NULL)",
        "CREATE TABLE IF NOT EXISTS `learning` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`dateKey` TEXT NOT NULL, `minutes` INTEGER NOT NULL, `text` TEXT NOT NULL, " +
            "`createdAt` INTEGER NOT NULL)"
    )

    /**
     * v5 → v6 (Phase 8): checklist core. Adds `habit_rules`, `checklist_tasks`,
     * `checklist_occurrences`, `completion_logs`.
     *
     * Pure addition, like every step before it — no existing row is rewritten. The
     * occurrence primary key is derived from (kind, source, date), so regenerating a date
     * cannot duplicate a row. `NOT NULL` columns carry SQL defaults so that a v5 backup
     * (which has no checklist tables at all) still restores into this schema.
     */
    val V5_TO_V6: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `habit_rules` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
            "`domain` TEXT NOT NULL, `cadence` TEXT NOT NULL, `weekdays` TEXT NOT NULL, " +
            "`windowStartMin` INTEGER, `windowEndMin` INTEGER, `weeklyTarget` INTEGER, " +
            "`minimumLabel` TEXT, `paused` INTEGER NOT NULL, `archived` INTEGER NOT NULL, " +
            "`createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `checklist_tasks` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
            "`domain` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `dueTimeMin` INTEGER, " +
            "`archived` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `checklist_occurrences` (`id` TEXT NOT NULL, `dateKey` TEXT NOT NULL, " +
            "`kind` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `title` TEXT NOT NULL, `domain` TEXT NOT NULL, " +
            "`windowStartMin` INTEGER, `windowEndMin` INTEGER, `minimumLabel` TEXT, `status` TEXT NOT NULL, " +
            "`snoozeUntil` INTEGER, `generatedAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
            "PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_checklist_occurrences_dateKey` " +
            "ON `checklist_occurrences` (`dateKey`)",
        "CREATE TABLE IF NOT EXISTS `completion_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`occurrenceId` TEXT NOT NULL, `dateKey` TEXT NOT NULL, `action` TEXT NOT NULL, " +
            "`fromStatus` TEXT NOT NULL, `toStatus` TEXT NOT NULL, `minutes` INTEGER, `quantity` INTEGER, " +
            "`topic` TEXT NOT NULL, `note` TEXT NOT NULL, `at` INTEGER NOT NULL)",
        "CREATE INDEX IF NOT EXISTS `index_completion_logs_occurrenceId` " +
            "ON `completion_logs` (`occurrenceId`)"
    )

    /** Ordered steps, used by the tests to replay 1 → 6 statement by statement. */
    val STEPS: List<Step> = listOf(
        Step(1, 2, V1_TO_V2),
        Step(2, 3, V2_TO_V3),
        Step(3, 4, V3_TO_V4),
        Step(4, 5, V4_TO_V5),
        Step(5, 6, V5_TO_V6)
    )

    data class Step(val from: Int, val to: Int, val statements: List<String>)

    /** Every statement needed to reach [CURRENT_VERSION] from [startVersion]. */
    fun statementsFrom(startVersion: Int): List<String> =
        STEPS.filter { it.from >= startVersion }.flatMap { it.statements }

    /** Registered with Room; there is deliberately no destructive fallback. */
    val ALL: Array<Migration> = STEPS.map { step ->
        object : Migration(step.from, step.to) {
            override fun migrate(db: SupportSQLiteDatabase) {
                step.statements.forEach { db.execSQL(it) }
            }
        }
    }.toTypedArray()
}
