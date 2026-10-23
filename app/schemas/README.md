# Room schema baseline

This directory is where Room exports the schema of `DayLedgerDatabase`, and where the
exported JSON is meant to be **committed** — it is the baseline that future versions must
migrate from.

- Configured in `app/build.gradle.kts` (`ksp { arg("room.schemaLocation", "$projectDir/schemas") }`).
- Produced on every CI run and uploaded as the `room-schema` artifact (90-day retention),
  and its `identityHash` is printed by the *Record Room schema export* step.
- Path for the current version — **v7**:

  ```
  app/schemas/io.github.pabi277.dayledger.data.DayLedgerDatabase/7.json
  ```

## Status

**Committed on 2026-10-23.** `7.json` is the export from the v7 build, downloaded by the
owner from the `room-schema` artifact of run `39932190461` (`29d90aa`) and verified before
committing:

- `version` **7**, `identityHash` **`29eea1eeb853cfa0825788e7abb39bb9`**, 19 tables.
- Every table, column, affinity, `notNull` flag and primary-key position matches the
  expectation table in `MigrationTest` (`migrated schema matches the v7 entity definitions
  exactly`) — 19/19 tables, zero differences, checked mechanically rather than by eye.
- The two indices the entities declare are present:
  `index_checklist_occurrences_dateKey` and `index_completion_logs_occurrenceId`.
- `autoGenerate` is true on exactly the seven `INTEGER` ids the entities mark
  `@PrimaryKey(autoGenerate = true)`: `notification_log`, `blocks`, `template_items`,
  `limit_events`, `earnings`, `learning`, `completion_logs`.

The agent's sandbox still cannot download Actions artifacts (302 to
`productionresultssa8.blob.core.windows.net`, unreachable), so any future schema version
needs the same one-time owner download.

## Rules for this directory

- One file per schema version, named `<version>.json`, never edited after the version
  ships. A schema bump adds a file; it never rewrites an old one.
- The KSP task rewrites the current version's file in place on every build, so a local
  build will not silently corrupt an old version, but it does mean the committed copy of
  the *current* version must match what CI exported (compare `identityHash`).
- Migration tests do not read these files. They build a v1 database and replay the
  migration statements, so the export is a baseline for future work and for review, not a
  test fixture — which is why its absence has never failed CI.
