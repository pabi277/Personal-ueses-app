# Phase 08 — Checklist core

- Status: 🚧 **IN PROGRESS — code complete and green in compile + 86 unit tests; one CI
  workflow guard still pins the v5 schema filename and blocks the artifact upload**
  · Date: 2026-10-23 · Branch: `arena/2105d3de-personal-ueses-app`
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

**CI (run `39908508385`, commit `873e0fd`):** units tests and debug build ✅,
signing certificate recorded ✅, OS-backup exclusion in the merged manifest ✅,
**`Record Room schema export` ❌** — see §7. Artifact upload skipped as a consequence.

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

## 7. The one open blocker — a workflow guard, which needs an owner answer

The workflow step `Record Room schema export` runs:

```bash
SCHEMA=$(find app/schemas -name "5.json" | head -n 1)
```

That filename was correct while the database was v5. Phase 8 legitimately moves the
database to v6, so Room now exports `6.json` and the step fails with
`Room did not export the v5 schema` — even though the export itself is fine and the unit
tests that check the schema all pass.

This is a **workflow change**, and the standing constraint is explicit: no workflow change
without an owner gate answer. Hence this record stops here rather than editing
`.github/workflows/`. The three candidate fixes are in the chat message that accompanies
this record; the recommended one pins nothing and cannot rot again:

```bash
SCHEMA=$(ls -1 app/schemas/*/*.json | sort -V | tail -n 1)
```

Notes for whoever applies it: the step exists to prove Room exported *a* schema and to
print its `identityHash`. `schemas/…/5.json` is still absent from the tree (follow-up F3),
and with the version bumped Room will never regenerate it, so any check that hardcodes
`5.json` is dead regardless of this phase.

## 8. What Phase 8 still needs before it can be marked ✅

1. The workflow guard fix above, then a green run with `app-progress-apk` uploaded.
2. An owner install and short smoke check: a weekday habit and a one-off task create only
   the correct occurrences; Done works without a form; Minimum differs from Full; Undo
   works; a restart adds no duplicates; Phase 7 data is intact after the upgrade; the
   first screen shows Daily Logger.
3. Then, and only then, the ROADMAP/`prompt.md` ✅ flip.
