package io.github.pabi277.dayledger.backup

/**
 * Encodes and decodes the versioned backup file.
 *
 * Pure Kotlin on purpose: the same code is unit-tested on the JVM with fictional
 * data, so the format can be trusted before it ever touches the owner's history.
 */
object BackupCodec {

    /** Layout of the backup file itself. Bump only with a decode path for the old one. */
    const val FORMAT_VERSION: Int = 1

    /** The app id a backup must belong to before it may be restored. */
    const val APP_ID: String = "io.github.pabi277.dayledger"

    const val KEY_FORMAT = "formatVersion"
    const val KEY_SCHEMA = "schemaVersion"
    const val KEY_APP = "app"
    const val KEY_APP_VERSION = "appVersion"
    const val KEY_CREATED = "createdAtEpochMs"
    const val KEY_TABLES = "tables"
    const val KEY_NAME = "name"
    const val KEY_COLUMNS = "columns"
    const val KEY_ROWS = "rows"

    fun encode(document: BackupDocument): String {
        val tables = document.tables.map { table ->
            JsonValue.Obj(
                listOf(
                    KEY_NAME to JsonValue.Str(table.name),
                    KEY_COLUMNS to JsonValue.Arr(table.columns.map { JsonValue.Str(it) }),
                    KEY_ROWS to JsonValue.Arr(
                        table.rows.map { row -> JsonValue.Arr(row.map { it.toJson() }) }
                    )
                )
            )
        }
        return Json.write(
            JsonValue.Obj(
                listOf(
                    KEY_FORMAT to JsonValue.Num(document.formatVersion.toString()),
                    KEY_SCHEMA to JsonValue.Num(document.schemaVersion.toString()),
                    KEY_APP to JsonValue.Str(document.appId),
                    KEY_APP_VERSION to JsonValue.Str(document.appVersionName),
                    KEY_CREATED to JsonValue.Num(document.createdAtEpochMs.toString()),
                    KEY_TABLES to JsonValue.Arr(tables)
                )
            )
        )
    }

    /**
     * Parses a backup file. Never throws: every failure becomes a
     * [DecodeResult.Failure] with a message the UI can show verbatim.
     */
    fun decode(text: String): DecodeResult {
        val root = try {
            Json.parse(text)
        } catch (e: JsonParseException) {
            return DecodeResult.Failure("Not a valid backup file: ${e.message}")
        }
        if (root !is JsonValue.Obj) return DecodeResult.Failure("Not a valid backup file: top level is not an object")

        val formatVersion = root.int(KEY_FORMAT)
            ?: return DecodeResult.Failure("Not a valid backup file: missing $KEY_FORMAT")
        if (formatVersion <= 0) return DecodeResult.Failure("Not a valid backup file: bad $KEY_FORMAT $formatVersion")
        if (formatVersion > FORMAT_VERSION) {
            return DecodeResult.Failure(
                "This backup was written by a newer app version (format $formatVersion, this build reads $FORMAT_VERSION). Update the app, then restore."
            )
        }

        val appId = root.string(KEY_APP)
            ?: return DecodeResult.Failure("Not a valid backup file: missing $KEY_APP")
        if (appId != APP_ID) {
            return DecodeResult.Failure("This backup belongs to \"$appId\", not to $APP_ID.")
        }

        val schemaVersion = root.int(KEY_SCHEMA)
            ?: return DecodeResult.Failure("Not a valid backup file: missing $KEY_SCHEMA")
        val appVersion = root.string(KEY_APP_VERSION) ?: "unknown"
        val createdAt = root.long(KEY_CREATED) ?: 0L

        val tableValues = root.array(KEY_TABLES)
            ?: return DecodeResult.Failure("Not a valid backup file: missing $KEY_TABLES")

        val tables = ArrayList<BackupTable>(tableValues.size)
        for ((index, tableValue) in tableValues.withIndex()) {
            if (tableValue !is JsonValue.Obj) return DecodeResult.Failure("Table #$index is not an object")
            val name = tableValue.string(KEY_NAME)
                ?: return DecodeResult.Failure("Table #$index has no $KEY_NAME")
            val columns = tableValue.array(KEY_COLUMNS)?.mapNotNull { (it as? JsonValue.Str)?.value }
                ?: return DecodeResult.Failure("Table \"$name\" has no column list")
            if (columns.isEmpty()) return DecodeResult.Failure("Table \"$name\" has an empty column list")
            if (columns.distinct().size != columns.size) return DecodeResult.Failure("Table \"$name\" repeats a column name")

            val rowValues = tableValue.array(KEY_ROWS)
                ?: return DecodeResult.Failure("Table \"$name\" has no $KEY_ROWS")
            val rows = ArrayList<List<BackupCell>>(rowValues.size)
            for ((rowIndex, rowValue) in rowValues.withIndex()) {
                if (rowValue !is JsonValue.Arr) return DecodeResult.Failure("Table \"$name\" row $rowIndex is not an array")
                if (rowValue.items.size != columns.size) {
                    return DecodeResult.Failure(
                        "Table \"$name\" row $rowIndex has ${rowValue.items.size} values but ${columns.size} columns"
                    )
                }
                rows.add(rowValue.items.map { toCell(it) })
            }
            tables.add(BackupTable(name, columns, rows))
        }

        return DecodeResult.Success(
            BackupDocument(
                formatVersion = formatVersion,
                schemaVersion = schemaVersion,
                appId = appId,
                appVersionName = appVersion,
                createdAtEpochMs = createdAt,
                tables = tables
            )
        )
    }

    private fun toCell(value: JsonValue): BackupCell = when (value) {
        is JsonValue.Str -> BackupCell.Text(value.value)
        is JsonValue.Bool -> BackupCell.Integer(if (value.value) 1L else 0L)
        is JsonValue.Num -> {
            val asLong = value.asLong()
            if (asLong != null) BackupCell.Integer(asLong) else BackupCell.Real(value.asDouble() ?: 0.0)
        }
        is JsonValue.Null -> BackupCell.Null
        is JsonValue.Arr, is JsonValue.Obj -> BackupCell.Null
    }

    sealed class DecodeResult {
        data class Success(val document: BackupDocument) : DecodeResult()
        data class Failure(val message: String) : DecodeResult()
    }
}
