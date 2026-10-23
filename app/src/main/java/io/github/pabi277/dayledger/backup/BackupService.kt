package io.github.pabi277.dayledger.backup

import android.content.ContentValues
import android.database.Cursor
import androidx.room.RoomDatabase

/**
 * Reads a complete snapshot out of the live database and writes one back.
 *
 * It enumerates `sqlite_master` instead of naming tables by hand, so a table added
 * in a later phase is covered by the backup without anyone remembering to extend a
 * list here. Room's own bookkeeping tables are skipped.
 */
class BackupService(private val database: RoomDatabase) {

    companion object {
        private val SYSTEM_TABLES = setOf("room_master_table", "android_metadata", "sqlite_sequence")
    }

    /** Every user table in the database, with the column facts a restore needs. */
    fun tableSpecs(): List<TableSpec> = userTables().map { tableSpec(it) }

    fun currentSchemaVersion(): Int = database.openHelper.readableDatabase.version

    fun exportDocument(appVersionName: String, nowEpochMs: Long): BackupDocument {
        val db = database.openHelper.readableDatabase
        val tables = userTables().map { name ->
            val cursor = db.query("SELECT * FROM `$name`")
            cursor.use {
                val columns = it.columnNames.toList()
                val rows = ArrayList<List<BackupCell>>()
                while (it.moveToNext()) rows.add(readRow(it, columns.size))
                BackupTable(name, columns, rows)
            }
        }
        return BackupDocument(
            formatVersion = BackupCodec.FORMAT_VERSION,
            schemaVersion = db.version,
            appId = BackupCodec.APP_ID,
            appVersionName = appVersionName,
            createdAtEpochMs = nowEpochMs,
            tables = tables
        )
    }

    /**
     * Writes a decoded backup back into the database, in one transaction.
     *
     * Refuses (without touching a row) when [RestorePlanner] rejects the file. On a
     * SQLite error the transaction is rolled back, so a failed restore leaves the
     * previous data exactly as it was.
     */
    fun restore(document: BackupDocument): RestoreOutcome {
        val decision = RestorePlanner.plan(
            document = document,
            currentTables = tableSpecs(),
            currentSchemaVersion = currentSchemaVersion(),
            supportedSchemaVersions = io.github.pabi277.dayledger.data.migrations.DatabaseMigrations.SUPPORTED_VERSIONS
        )
        if (decision is RestoreDecision.Rejected) {
            return RestoreOutcome.Refused(decision.reasons)
        }
        val allowed = decision as RestoreDecision.Allowed
        val db = database.openHelper.writableDatabase
        db.beginTransaction()
        return try {
            var inserted = 0
            for (table in document.tables) {
                db.execSQL("DELETE FROM `${table.name}`")
                val columns = table.columns
                for (row in table.rows) {
                    val values = ContentValues(columns.size)
                    for ((index, column) in columns.withIndex()) {
                        when (val cell = row[index]) {
                            is BackupCell.Text -> values.put(column, cell.value)
                            is BackupCell.Integer -> values.put(column, cell.value)
                            is BackupCell.Real -> values.put(column, cell.value)
                            is BackupCell.Null -> values.putNull(column)
                        }
                    }
                    db.insert("${table.name}", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
                    inserted++
                }
            }
            db.setTransactionSuccessful()
            RestoreOutcome.Restored(
                tables = allowed.tables,
                rowsWritten = inserted,
                untouched = allowed.untouched
            )
        } catch (e: Exception) {
            RestoreOutcome.Failed("Restore failed and was rolled back: ${e.message ?: e::class.java.simpleName}")
        } finally {
            db.endTransaction()
        }
    }

    private fun userTables(): List<String> {
        val db = database.openHelper.readableDatabase
        val names = ArrayList<String>()
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                if (!SYSTEM_TABLES.contains(name)) names.add(name)
            }
        }
        return names
    }

    private fun tableSpec(name: String): TableSpec {
        val db = database.openHelper.readableDatabase
        val columns = ArrayList<ColumnSpec>()
        db.query("PRAGMA table_info(`$name`)").use { cursor ->
            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) != 0
                val hasDefault = !cursor.isNull(cursor.getColumnIndexOrThrow("dflt_value"))
                columns.add(ColumnSpec(columnName, notNull, hasDefault))
            }
        }
        return TableSpec(name, columns)
    }

    private fun readRow(cursor: Cursor, columnCount: Int): List<BackupCell> {
        val cells = ArrayList<BackupCell>(columnCount)
        for (index in 0 until columnCount) {
            cells.add(
                when (cursor.getType(index)) {
                    Cursor.FIELD_TYPE_NULL -> BackupCell.Null
                    Cursor.FIELD_TYPE_INTEGER -> BackupCell.Integer(cursor.getLong(index))
                    Cursor.FIELD_TYPE_FLOAT -> BackupCell.Real(cursor.getDouble(index))
                    Cursor.FIELD_TYPE_STRING -> BackupCell.Text(cursor.getString(index))
                    else -> throw IllegalStateException(
                        "The database contains a binary (BLOB) value in column ${cursor.getColumnName(index)}, which this backup format does not support yet."
                    )
                }
            )
        }
        return cells
    }
}

sealed class RestoreOutcome {
    data class Restored(
        val tables: List<TableRestore>,
        val rowsWritten: Int,
        val untouched: List<String>
    ) : RestoreOutcome()

    data class Refused(val reasons: List<String>) : RestoreOutcome()

    data class Failed(val message: String) : RestoreOutcome()
}
