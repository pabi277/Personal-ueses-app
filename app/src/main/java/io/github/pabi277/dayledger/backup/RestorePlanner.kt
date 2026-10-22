package io.github.pabi277.dayledger.backup

/**
 * Decides whether a decoded backup may be written into the current database, and
 * exactly which columns each table will receive — before anything is deleted.
 *
 * Pure Kotlin so every rejection path is unit-tested. The rule it enforces: a
 * restore either applies completely or is refused with a readable reason; it never
 * half-restores and never silently drops a required column.
 */
object RestorePlanner {

    fun plan(
        document: BackupDocument,
        currentTables: List<TableSpec>,
        currentSchemaVersion: Int,
        supportedSchemaVersions: List<Int>
    ): RestoreDecision {
        val reasons = ArrayList<String>()

        if (document.schemaVersion > currentSchemaVersion) {
            reasons.add(
                "This backup is from a newer database schema (v${document.schemaVersion}); this build is v$currentSchemaVersion. Update the app first."
            )
            return RestoreDecision.Rejected(reasons)
        }
        if (document.schemaVersion !in supportedSchemaVersions) {
            reasons.add(
                "This backup uses database schema v${document.schemaVersion}, which this build cannot restore (supported: ${supportedSchemaVersions.joinToString()})."
            )
            return RestoreDecision.Rejected(reasons)
        }

        val byName = currentTables.associateBy { it.name }
        val restores = ArrayList<TableRestore>()
        val untouched = ArrayList<String>()

        for (table in document.tables) {
            val spec = byName[table.name]
            if (spec == null) {
                reasons.add("The backup contains a table this build does not have: \"${table.name}\".")
                continue
            }
            for (column in table.columns) {
                if (spec.column(column) == null) {
                    reasons.add("Table \"${table.name}\" has no column \"$column\" in this build.")
                }
            }
            for (column in spec.columns) {
                if (table.columns.contains(column.name)) continue
                if (column.notNull && !column.hasDefault) {
                    reasons.add(
                        "Table \"${table.name}\" needs a value for \"${column.name}\", which this older backup does not contain."
                    )
                }
            }
            restores.add(TableRestore(table.name, table.columns, table.rows.size))
        }

        val covered = document.tables.map { it.name }.toSet()
        currentTables.mapTo(untouched) { it.name }
        untouched.removeAll(covered)

        return if (reasons.isEmpty()) {
            RestoreDecision.Allowed(restores, untouched)
        } else {
            RestoreDecision.Rejected(reasons)
        }
    }

    /** Tables present in the database but absent from the backup are left as they are. */
    fun untouchedTables(document: BackupDocument, currentTables: List<TableSpec>): List<String> {
        val covered = document.tables.map { it.name }.toSet()
        return currentTables.map { it.name }.filterNot { covered.contains(it) }
    }
}
