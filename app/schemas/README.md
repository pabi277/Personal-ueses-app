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

## Why the file is not here yet

Nothing in this directory has ever been committed. The agent's sandbox cannot download
GitHub Actions artifacts: `gh api …/artifacts/<id>/zip` returns HTTP 302 to
`productionresultssa8.blob.core.windows.net`, which is unreachable from there, and job-log
downloads fail the same way. The owner agreed on 2026-10-23 to download the 2,406-byte
artifact by hand (latest: `room-schema` id `12490252325` from run `39927725461`, commit
`6c2da08`).

## Rules for this directory

- One file per schema version, named `<version>.json`, never edited after the version
  ships. A schema bump adds a file; it never rewrites an old one.
- The KSP task rewrites the current version's file in place on every build, so a local
  build will not silently corrupt an old version, but it does mean the committed copy of
  the *current* version must match what CI exported (compare `identityHash`).
- Migration tests do not read these files. They build a v1 database and replay the
  migration statements, so the export is a baseline for future work and for review, not a
  test fixture — which is why its absence has never failed CI.
