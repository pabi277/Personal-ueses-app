package io.github.pabi277.dayledger.backup

/** One cell of one row, typed exactly as SQLite stores it (no floats for money). */
sealed class BackupCell {
    data class Text(val value: String) : BackupCell()
    data class Integer(val value: Long) : BackupCell()
    data class Real(val value: Double) : BackupCell()
    object Null : BackupCell()

    fun toJson(): JsonValue = when (this) {
        is Text -> JsonValue.Str(value)
        is Integer -> JsonValue.Num(value.toString())
        is Real -> JsonValue.Num(value.toString())
        is Null -> JsonValue.Null
    }
}

/** One table of one backup: its columns and its rows, in column order. */
data class BackupTable(
    val name: String,
    val columns: List<String>,
    val rows: List<List<BackupCell>>
)

/**
 * A complete, versioned snapshot of the on-device database.
 *
 * [schemaVersion] is the Room database version the snapshot was taken at;
 * [formatVersion] is the layout of this file itself. They are separate on purpose:
 * the file layout can stay stable while the database version moves.
 */
data class BackupDocument(
    val formatVersion: Int,
    val schemaVersion: Int,
    val appId: String,
    val appVersionName: String,
    val createdAtEpochMs: Long,
    val tables: List<BackupTable>
) {
    fun rowCount(): Int = tables.sumOf { it.rows.size }
}

/** Description of a table as it exists in the database being restored into. */
data class ColumnSpec(val name: String, val notNull: Boolean, val hasDefault: Boolean)

data class TableSpec(val name: String, val columns: List<ColumnSpec>) {
    fun column(name: String): ColumnSpec? = columns.firstOrNull { it.name == name }
}

/** What a single-table restore will do. */
data class TableRestore(val table: String, val columns: List<String>, val rowCount: Int)

sealed class RestoreDecision {
    data class Allowed(
        val tables: List<TableRestore>,
        val untouched: List<String> = emptyList()
    ) : RestoreDecision()
    data class Rejected(val reasons: List<String>) : RestoreDecision()
}
