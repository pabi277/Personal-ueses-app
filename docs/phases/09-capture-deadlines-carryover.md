# Phase 09 — Capture, deadlines and carryover

- Status: 🚧 **IN PROGRESS — code complete, CI green on 102 tests, awaiting the owner's
  install and smoke check** · Date: 2026-10-23 · Branch
  `arena/8322cba4-personal-ueses-app`
- Base: `main` @ `66582f1` (Phase 8 merged)
- Owner command that started this phase: *"Start phase 9"* (2026-10-23).
- Not started: Phase 10, any PR, any merge, any release.

## 1. Scope taken from the repo

`docs/ROADMAP.md` Phase 9 defines four parts and one Done-means sentence;
`docs/REQUIREMENTS.md` supplies DL2, DL9, DL17 and DL18; `docs/UI_DESIGN.md` §4–§5
defines the interaction.

| Part | What it asked for | This round |
|---|---|---|
| 9.1 | Quick Capture/Inbox: title-first capture from Today and useful entry points, organisation optional | ✅ |
| 9.2 | Due tasks: date-only and timed deadlines, sorted by urgency, shown in **Today and Plan** | ✅ |
| 9.3 | Tomorrow merge: template blocks, due tasks and carryovers compose without duplicates; Top 3 editable | ✅ (template merge was already idempotent; Top 3 new) |
| 9.4 | Leftover decisions: Move, Schedule, Shrink to Minimum, Drop/archive; carryover count and repeated-carryover flag | ✅ |

**Done means:** title-only capture saves within 10 seconds; template reapply and midnight
rollover do not duplicate occurrences; every unfinished item gets an explicit next action
rather than silently rolling forever.

## 2. Schema v6 → v7

One new table, one more pure addition with no destructive fallback:

```
inbox_items(id TEXT PK, title, domain, note, createdAt)
```

**Why a new table rather than a nullable `dueDate`.** Making the existing
`checklist_tasks.dueDate` nullable would mean recreating that table inside a migration, and
every migration in this project is a pure addition with no `DROP`/`RENAME`. An inbox item is
also genuinely a different thing: it is a capture that has *no* date, not a task with an
empty one. Scheduling it creates a task and deletes the inbox row — an explicit owner action,
never a migration.

`room-schema` grew 2,378 B → 2,406 B, which is that one table. Every supported start version
(1–7) is still proven to reach v7 with all its rows intact, and the schema-drift test pins all
19 tables.

## 3. Phase 9 rules, kept pure

`data/checklist/DayClose.kt` has no Android types and no database handle, so the clauses that
matter are unit-tested:

- **Every leftover gets a decision.** `isLeftover` is true only for `pending` and `later`;
  every other status is settled. Nothing carries itself.
- **Decisions map to distinct, recorded states.** `carried`, `scheduled`, `dropped` join the
  vocabulary as settled-but-not-completed, so a moved item never inflates a completion count.
- **Carry is idempotent.** A carried occurrence's id is `carried:<sourceId>:<date>` — a
  different kind from `habit:`/`task:`, so it cannot collide with the habit row generated for
  the same day, and the insert ignores conflicts. A double tap, or a retried rollover, can
  only find the row already there. A SQL-level test proves it against real SQLite.
- **Carryover is counted from the log, not stored on the row.** Undoing a decision therefore
  cannot corrupt the count. The flag appears from the second carry onward, stated plainly
  ("Carried 2 times already") and without a streak penalty.
- **Top 3 is capped and never silently evicts.** A fourth add is refused rather than
  displacing a choice the owner made.
- **Urgency is honest.** Due order is date, then time, then title; an item with no date sorts
  last and is never given an invented deadline (`Today`, `Tomorrow`, `in 3 days`,
  `Overdue by 2 days`).

## 4. What the owner sees

- **Today** — capture now saves on the title alone and lands in the **Inbox**, with an Undo,
  instead of Phases 8's behaviour of putting it straight onto today (which invented a
  deadline). Two new entry points sit under the capture field: `Inbox (n)` and `Close day`.
- **Inbox** — capture field first, then the waiting items, each offering **Today**,
  **Tomorrow**, **Pick date** or **Discard**. Organisation is entirely optional.
- **Close day** — one card per leftover with **Move**, **Schedule**, **Shrink to Minimum**
  and **Drop**, plus the target-date field and the carryover flag. Nothing rolls over by
  itself; an item stays on this screen until it has a decision.
- **Plan** — now lists the date's checklist items (habit, task and carried rows), which is
  the gap Phase 8 left open, and each row carries a **Top 3** toggle. Applying a template
  still replaces only that day's *blocks* and never touches checklist history.

## 5. Evidence

**CI — ✅ green, run `39915691906` (commit `e8f5a5e`):**

| Item | Result |
|---|---|
| `Unit tests, debug build, progress APK` | ✅ **102 tests** (88 before this phase: +14 in `DayCloseTest`) |
| `Record Room schema export` | ✅ (picks the highest version, as fixed in Phase 8) |
| Signer | ✅ `533D76ED5C2E264779EE005C…` — unchanged |
| `app-progress-apk` | 18,584,762 B |
| `room-schema` | 2,406 B |

An earlier commit message said "16 new pure tests"; the file has **14**, and 102 − 88 = 14.
Recorded here rather than rewritten, because this branch is never force-pushed.

**Owner device evidence:** none yet. Nothing in this record claims otherwise.

## 6. Deliberately not in this phase

- **Reminders and notifications** — Phase 10. A leftover decision writes state and history;
  it posts nothing.
- **The full Evening Close review** — DL17's one-line learning reflection and DL19/DL20
  history and consistency are Phase 12. This phase ships the leftover *mechanics* only.
- **Archiving the habit rule from a leftover** — Drop settles the dated occurrence. Archiving
  the rule itself stays on the Habits screen, so a single decision cannot silently end a
  habit's future.
- **Plan's timeline/overlap drawing** — Plan gains the checklist list it was missing, not the
  proportional timeline UI Design v2 describes.

## 7. What Phase 9 still needs before it can be marked ✅

An owner install of `app-progress-apk` from `39915691906` and this smoke check:

1. **Capture is fast and undated.** Today → type a title → Add. It must appear under
   `Inbox (1)` and **not** on today's checklist. One tap **Undo** removes it.
2. **Scheduling works.** Inbox → **Today** → the item appears on Today's checklist.
   **Tomorrow** → it appears on Plan for tomorrow, not today.
3. **Day close asks about everything.** Close day → every unfinished item is listed.
   Use each of the four decisions once and confirm the item leaves the list and the day's
   counts stay honest (a Moved item must not count as Full or Minimum).
4. **Carryover is visible and does not duplicate.** Move the same item to the same target
   date twice (close the screen and reopen, then Move again if it reappears) — the target
   day must show **one** occurrence, not two.
5. **Top 3.** Plan → Tomorrow → toggle three items, confirm a fourth toggle is refused, and
   that the list survives leaving and returning to the screen.
6. **Phase 8 and 7 data intact.** The upgrade is v6 → v7; a backup should still read 19
   tables with the previous rows unchanged.
