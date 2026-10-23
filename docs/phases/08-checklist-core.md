# Phase 08 — Checklist core

- Status: 🚧 **IN PROGRESS — code and CI complete and green; awaiting the owner's install
  and smoke check before it can be marked ✅** · Date: 2026-10-23 ·
  Branch: `arena/2105d3de-personal-ueses-app`
- Base: `main` @ `2ee8048` (Phase 7 merged) plus F1 (`784b92e`)
- Owner command that started this phase: *"Start phase 8"* (2026-10-23).
- Not started: Phase 9, any release, any PR, any merge.

## 1. Scope taken from the repo, not from memory

`docs/ROADMAP.md` Phase 8 lists four parts and a Done-means clause. `docs/UI_DESIGN.md`
is the presentation contract (v2, approved 2026-10-22) and `docs/DECISIONS.md` records
that the **Daily Logger** label ships with this checklist front door while
`io.github.pabi277.dayledger` and the database identity stay unchanged.

| Part | What it asked for | This round |
|---|---|---|
| 8.1 | Habit rules, one-off tasks, dated occurrences, completion logs; generation idempotent | ✅ |
| 8.2 | Today checklist: Now/Next, time-window groups, Done/Minimum/Later/Skip/Undo, completed collapse | ✅ |
| 8.3 | Optional evidence after ticking; usage may suggest, never complete | ✅ |
| 8.4 | Generic editable habits, weekday/weekly schedules, pause and archive, UI Design v2 front door, Daily Logger label | ✅ (edit-lite; see §6) |

## 2. Schema v5 → v6

Four new tables, added by an explicit migration with no destructive fallback:

| Table | Purpose |
|---|---|
| `habit_rules` | The reusable recurrence definition |
| `checklist_tasks` | A one-off intent with a date-only deadline (optional time) |
| `checklist_occurrences` | The dated instance, with its own status and snooze state |
| `completion_logs` | Append-only audit of every state change, including undo |

The occurrence primary key is **`kind:sourceId:date`**. That single choice is what makes
"restart/reboot adds no duplicates" structural rather than hopeful: regeneration computes
the same key and the insert is conflict-ignoring, so a second, third or hundredth pass can
only find the row already there.

Two details that would have failed on a device but not in a compiler, caught while
writing the DDL:

- **No SQL `DEFAULT` clauses.** Kotlin defaults do not become SQL defaults, so adding them
  in the migration would make Room's runtime schema validation fail on first open. The new
  tables therefore match the entities exactly.
- **Indices are declared on the entities** (`dateKey`, `occurrenceId`), so the migration's
  `CREATE INDEX` statements match Room's expected index set instead of adding an
  unexpected one.

Restore compatibility: a Phase 7 backup (schema v5) has no checklist tables.
`RestorePlanner` leaves tables that are absent from the document untouched, so restoring
an old backup into v6 restores every old row and leaves checklist data alone.

## 3. Generation and ordering are pure functions

`ChecklistGeneration` and `TodayBuckets` contain no Android types and no database handle,
so the two rules that matter most are unit-tested on the JVM:

- **Schedules.** `daily` every day; `weekdays` and `weekly` only on the selected ISO
  weekdays; paused and archived rules generate nothing.
- **One-off tasks.** Exactly one occurrence, on the due date, never recurring.
- **Ordering.** Overdue → Now → exactly one Next → Due today → Anytime → Done. An item
  without a window lands in Anytime and is never given an invented time. A snoozed
  ("Later") item leaves Now/Next and returns when the snooze expires.

## 4. Today — the Command Center Checklist

The first screen is rebuilt to `docs/UI_DESIGN.md` §3:

- Brand line **Daily Logger**, screen title **Today**.
- A summary line: how many items are left, plus separate Full and Minimum counts. Not
  capped at three, and no streak pressure.
- **Quick capture** above the list: a title is the only required field and the task lands
  in today.
- Grouped rows with a one-tap **Done** that never opens a required form; **Minimum**,
  **Later**, **Skip** as secondary actions; **Undo** on the affected row.
- **Optional detail** dialog after Done or Minimum (minutes, topic, note) with an obvious
  **No detail** dismiss that keeps the completion.
- **Phone evidence** demoted to a lower, collapsible card: device-reported usage with an
  "as of" stamp and a Refresh button. It never marks anything done (DL22).

## 5. Evidence

**CI (run `39908874710`, commit `e65f33f`) — ✅ green, both the compile/test step and
every upload:**

| Item | Result |
|---|---|
| `Run unit tests and build debug APK` | ✅ (86 tests) |
| `Record APK signing certificate` | ✅ `533D76ED5C2E264779EE005C…` — still one signer |
| `Verify OS-backup exclusion in the merged manifest` | ✅ |
| `Record Room schema export` | ✅ after the owner-approved guard fix (§7) |
| `app-progress-apk` | ✅ 18,511,120 B |
| `room-schema` | ✅ 2,378 B (was 1,805 B at v5 — the four new tables, as expected) |
| `signer-533D76ED5C2E264779EE005C` | ✅ |

The earlier run `39908508385` is kept in the record as the red one: compile and all tests
passed there too, and only the schema-filename guard failed.

Counts: **86 unit tests** in the tree (was 67 before this phase: +19 checklist tests).
The migration suite now proves every supported start version (1–6) reaches v6 with all
rows intact, and the schema-drift test pins all 18 tables including the four new ones.

**Owner device evidence:** none yet. Nothing in this record claims otherwise.

## 6. What is deliberately edit-lite

UI Design v2 describes a full item/habit editor, reminder profiles (Core/Standard/
Flexible), a Morning Brief, Evening Close carryover, Focus mode and a linked-evidence
flow. Phase 8's Done-means clause does not require all of that, and Phase 9 owns
carryover while DL12/DL16 own reminder profiles. This round therefore ships:

- Habit creation with cadence, weekday selection, an optional Minimum label, pause and
  archive.
- One-off task creation for today.
- No starter routine, no seeded timetable, no owner seed data — the empty state is the
  honest starting state.

Editing an existing habit's schedule, archiving with confirmation, reschedule, the Morning
Brief and day close remain open and are listed in `docs/ROADMAP.md` as unstarted.

## 7. The workflow guard — resolved by owner gate answer

The workflow step `Record Room schema export` used to run:

```bash
SCHEMA=$(find app/schemas -name "5.json" | head -n 1)
```

That filename was correct while the database was v5. Phase 8 legitimately moves the
database to v6, so Room exported `6.json` and the step failed with
`Room did not export the v5 schema` — even though the export itself was fine.

A workflow edit needs an explicit owner gate answer, so it was **asked, not assumed**. The
owner approved the version-agnostic option on 2026-10-23, and commit `e65f33f` now does:

```bash
SCHEMA=$(ls -1 app/schemas/*/*.json 2>/dev/null | sort -V | tail -n 1) || true
```

It still proves Room exported a schema and still prints the `identityHash`, but it cannot
rot at v7. The `|| true` on the substitution keeps `set -uo pipefail` from failing the
whole step on an empty glob before the readable error can be printed.

Still open, unchanged: `schemas/…/5.json` is absent from the tree (follow-up F3) and Room
will not regenerate a superseded version, so F3 needs an artifact download rather than a
normal commit.

## 8. What Phase 8 still needs before it can be marked ✅

1. ~~The workflow guard fix, then a green run with `app-progress-apk` uploaded.~~ Done:
   `39908874710`.
2. An owner install of `app-progress-apk` from `39908874710` and the short smoke check: a weekday habit and a one-off task create only
   the correct occurrences; Done works without a form; Minimum differs from Full; Undo
   works; a restart adds no duplicates; Phase 7 data is intact after the upgrade; the
   first screen shows Daily Logger.
3. Then, and only then, the ROADMAP/`prompt.md` ✅ flip.
