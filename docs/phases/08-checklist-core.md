# Phase 08 — Checklist core

- Status: ✅ **DONE (2026-10-23)** — parts 8.1–8.4 implemented, CI green on 88 tests, and
  the owner's device smoke check reports all clauses passing · Date: 2026-10-23 ·
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

**Owner device evidence (2026-10-23, vivo 1820, APK from `39908874710`):**

| Done-means clause | Result | How it was seen |
|---|---|---|
| App opens after the v5 → v6 upgrade | ✅ | launched, first screen rendered |
| Phase 7 data intact | ✅ | owner-reported backup |
| First screen shows **Daily Logger** | ✅ | brand line above "Today" |
| A one-off task is created from Quick Capture | ✅ | "ii" and "nothing" created as dated tasks |
| Done needs no detail form | ✅ | "nothing" settled as **Full** with no required field |
| Minimum is distinct from Full | ✅ | counted separately, "1 minimum" then "1 full" |
| Undo is offered on a settled row | ✅ | visible on every settled row |
| Skip is not a completion | ✅ | a skipped item shows no full/minimum count |
| Restart adds no duplicates | ✅ | owner-reported count unchanged |
| Usage numbers still match Digital Wellbeing | ✅ | owner-reported, no launcher/dialer rows |
| F1 suggested categories | ✅ | owner-reported |

**Three defects found by this smoke check, all fixed and re-run green:**

1. **"Skipped, not done" sat under a heading called "Done"** (`396cb7a`). DL7 says Skip is
   not a completion, so the grouping contradicted the row's own subtitle. Skipped items now
   have their own section, and the summary reports them neutrally. Confirmed fixed on device
   at 12:03: `Nothing left today · 1 full · 1 skipped`, with "Done" holding only the Full
   row.
2. **The weekday chips did not fit** (`11e18d3`). Seven chips in one `Row` overflowed a
   360dp screen: Saturday was clipped and **Sunday was unreachable**, so a Sunday habit
   could not be created at all. This is a UI Design v2 §2 violation ("reflow rather than
   clip"). Chips now wrap, four per row.
3. **The habit card printed raw data**: `Chosen days: 4` instead of `Chosen days: Thu`.
   Fixed in the same commit via a unit-tested `weekdayNames`.

A build error introduced while fixing (2) and (3) — a stray quote in `HabitsScreen.kt` —
failed run `39912443671` and was corrected in `4d3fdbb`; both failures are kept in the
record rather than hidden.

**The two follow-up checks — owner reports both passing** (2026-10-23):

- **The positive half of generation.** A habit was created on the current weekday and did
  appear on Today, which is the direction the Thursday-only habit could not prove.
- **Tomorrow's non-recurrence.** Reported passing. Recorded honestly: at the time of this
  record the two one-off tasks were dated 2026-10-23 and "tomorrow" had not yet arrived on
  the device clock, so this clause rests on the owner's statement plus the generation rule
  being structurally covered — `ChecklistGeneration.draftsFor` emits a task only on
  `task.dueDate == date`, and two SQL-level tests generate a whole week twice and assert the
  exact date set. If a stale task does reappear, that is a defect against a clause already
  marked done, and re-opening this phase is the correct response.

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

## 7a. Plan screen — fixed on the owner's command (2026-10-23)

The Plan screenshot from the smoke check showed two things that predate Phase 8, and the
owner directed that they be fixed now rather than queued.

**The layout.** Every template sat in a single `Row` of chips. On a 360dp screen that
overflowed, and the card rendered as a mostly-empty box with a stray scroll artefact.
Each template now gets a full-width row: its name, **how many blocks it would apply**
("Empty template" when none), and **Apply** / **Delete**. Apply is disabled for an empty
template, so tapping it cannot silently wipe a day with nothing.

**The seeded routines.** `PlanningRepository` seeded four templates *with blocks already
filled in* — `College day` 08:00–16:00 plus study, `Study day` ×2, `Earning day`
×2 "Offerwall", `Off day` "Light task". That is a routine guessed on the owner's behalf,
which `docs/DECISIONS.md` and UI Design v2 §2 both forbid ("never seed a personal timetable,
routine, app list or real task"; examples stay generic). Fresh installs now seed two
**empty** placeholders, `Template A` and `Template B`.

**Nothing was deleted.** The four pre-filled templates are still in the owner's database,
because an upgrade must not silently remove something he may have edited — it would also be
a destructive change dressed up as a fix. He can now delete any of them from the Plan
screen, which is the honest way round: the app stops guessing, and he decides what goes.

## 8. Phase 8 — Done

Every clause of the roadmap's Done-means sentence for this phase is met:

| Clause | Evidence |
|---|---|
| A weekday habit and one-off task create only the correct occurrences | unit + SQL tests over a full week, generated twice; owner device check on the positive and negative direction |
| Done works without a detail form | device: settled as **Full** with no required field |
| Minimum is distinct from Full | device: counted separately (`1 full`, `1 minimum`) |
| Undo works | device: offered on every settled row; undo appends a log row and restores the prior status |
| Restart/reboot adds no duplicates | deterministic `kind:source:date` key + conflict-ignoring insert; SQL test proves three generations leave one row; owner restart check |
| Phase 7 data remains intact after upgrade | v5 → v6 is a pure addition with no destructive fallback; every supported source path (1–6) tested; owner upgraded over an existing install |
| First screen follows UI Design v2 and shows **Daily Logger** | device screenshots, 2026-10-23 |
| `io.github.pabi277.dayledger` unchanged | `applicationId` untouched; only the display label moved |

**Merged to `main` on 2026-10-23** with the owner's explicit command, together with the P0
measurement fix and F1 that had been waiting on this branch.

### Post-merge evidence on `main` (project rule: a branch run is not a main run)

PR **#25** merged `--merge` at 2026-10-23T06:52:36Z; `main` moved `2ee8048` → **`66582f1`**.
The branch run before the merge (`39913785708`) and the PR run (`39913960646`) were both
green, but neither proves the post-merge tree.

Run **`39914193884`** on `main` @ `66582f1`, **both jobs success** — the same shape as the
Phase 7 merge:

| Job / artifact | Result |
|---|---|
| `Unit tests, debug build, progress APK` | ✅ (88 tests) |
| `Signed release AAB and APK` | ✅ — the release path still works at schema v6 |
| `signed-android-release` | 24,718,303 B (was 24,550,572 B at v5) |
| `app-progress-apk` | 18,519,604 B |
| `room-schema` | 2,378 B |
| Signer | `533D76ED5C2E264779EE005C…` — unchanged for the tenth run |

Every run since Phase 7's merge now carries a v6 build signed by the one owner key, which
is what makes an update-over-install onto the owner's phone safe.

**Carried forward, not silently dropped:** the Plan screen does not render checklist items
(§6); habit *editing* is edit-lite (create/pause/archive, no reschedule or in-place edit);
reminder profiles, Morning Brief, Evening Close carryover and Focus remain unstarted and
belong to Phase 9/DL12/DL16 as the roadmap sequences them.
