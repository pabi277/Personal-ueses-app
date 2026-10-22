# Daily Logger — Sanitized Product Handoff

> **Reference copy derived from the owner-provided `Daily-Logger-Handoff-for-New-Chat.md` (2026-10-22).** Personal identifying, academic, financial, timetable, routine and other owner-specific details have been omitted. This is a product reference, not a record of personal data.
>
> **Current phase-order authority:** `docs/ROADMAP.md` is current. The full device-validation phase is **Phase 15, last**. The earlier Phase 7 device-first sequence in the source handoff is superseded. The existing P0 pre-feature check and Phase 7 data-safety gate still apply.
>
> Read this alongside `rule.md`, `docs/REQUIREMENTS.md`, `docs/DECISIONS.md` and `prompt.md`. Trust verified current code and the latest owner decisions over this summary. This file does not authorize implementation or override an open owner gate.

## 1. Product direction and locked constraints

- Evolve the existing Android app into a **personal Daily Logger / external brain**; do not create a separate checklist app.
- Organize the owner's whole life using optional, independently disable-able domains. Keep defaults generic and editable; do not invent a real timetable, routine, task list or personal seed data.
- Make completion **one tap**, with optional detail afterward. A due item should remain visible/remind until the owner chooses an action, while respecting quiet hours, caps, pause controls, Android permission limits and a non-shaming tone.
- Keep the app single-user, offline-first and English-only: no account, backend, ads, analytics, crash SDK, cloud sync, social features or Play Store track.
- Preserve package/database identity `io.github.pabi277.dayledger`, MIT license, the fixed permission philosophy and the sideloaded CI `app-progress-apk` path. Do not add permissions without a gate.
- Usage statistics are evidence, not proof of learning or completion. Never auto-complete a study or other item from foreground app usage.
- Preserve earning-app and always-allowed protections. Checklist completion must never hold essential apps or the phone hostage.
- Display label **Daily Logger** is proposed, not approved. Repository rename and visibility are separate owner gates; this file makes no change to either.

## 2. North star and effort targets

> Capture anything quickly, see what matters now, receive persistent but kind reminders, mark what happened with optional evidence, close the day honestly, and make tomorrow easier to remember.

The first view should answer **What now? What next? What is left today? What did I do? What must tomorrow not forget?** quickly and honestly.

Suggested acceptance targets from the source handoff: capture in ≤10 seconds; Done in ≤2 seconds; morning confirmation ≤60 seconds when a template exists; tomorrow planning ≤2 minutes; evening close ≤3 minutes; weekly reset ≤10 minutes. These are targets to verify in use, not achieved results.

## 3. Product principles and boundaries

- Keep concepts distinct: a recurring habit rule, a one-off task/deadline, a dated occurrence, a completion log and a time block are related but not interchangeable.
- Generate occurrences idempotently. Editing a rule must not rewrite yesterday's occurrence or completion history.
- Treat missed work as data, not failure. Carryover requires an explicit move, reschedule, shrink or drop decision; never roll an unfinished item indefinitely without telling the owner.
- Suggest at most three Core items per day, without hard-blocking an owner who chooses more. Habits should support a clearly labelled Minimum Version and realistic weekly targets, pauses and grace days.
- Evidence such as elapsed time, a learning log, an earning log or device usage may prefill a suggestion; the owner confirms completion.
- Keep all personal routines, actual deadlines, health details, app lists and money values on the device. Committed examples remain fictional.

Explicit MVP exclusions: cloud sync, accounts, AI coach/chat, social accountability, location reminders, calendar/contact scraping, reading other apps' content, automatic earning detection, health diagnosis, phone locking, coins or monetization.

## 4. Generic domain and flow guidance

Possible optional domains include Study, Work/College, Build, Create, Home/Admin, Health/Rest and Money/Earning. These are generic categories, not assumptions that any one item belongs to the owner. Templates such as Work/Study/Build/Off should be editable and use blank or user-selected times.

- **Morning:** confirm a day type/template, deadlines and a short Top 3; generate today's occurrences once.
- **During the day:** show Now/Next and remaining Core items; allow one-tap Done/Minimum/Later/Skip/Undo; capture an unorganized thought quickly.
- **Evening:** ask for explicit leftover decisions, an optional one-line reflection and tomorrow's Top 3/template.
- **Weekly:** review consistency, carryovers and linked records with one primary suggestion; let the owner shrink, pause or change routines without shame.

## 5. Proposed requirement families (DL IDs)

The IDs below summarize the source handoff and must be incorporated into `docs/REQUIREMENTS.md` with acceptance tests before implementation. They are not yet a substitute for the checked-in R1–R34 spec.

| IDs | Priority | Coverage |
|---|---|---|
| DL1–DL4 | Must | Checklist-first Today, quick capture, distinct data concepts, fast morning brief |
| DL5–DL10 | Must | Habit schedules, one-off tasks, occurrence actions, optional details, explicit carryover, pause controls |
| DL11 | Should | Pause-aware consistency, weekly targets, Full vs Minimum distinction |
| DL12–DL13 | Must | Item/domain reminder profiles and idempotent notification actions |
| DL14 | Should | Persistent silent visibility after audible repeats |
| DL15–DL16 | Must | Global pause/less-often controls, quiet hours, caps and schedule restoration/health |
| DL17–DL18 | Must | Evening close and deduplicated Tomorrow planning |
| DL19–DL20 | Should | Weekly review and searchable history |
| DL21 | Should | Optional links between blocks, timers and checklist items |
| DL22 | Must | Usage is evidence only; it never silently ticks an item |
| DL23 | Should | Optional Learning/Earning record links without double counting |
| DL24 | Must | Limits integration remains optional and preserves existing safety rules |
| DL25–DL27 | Must | Non-destructive migrations, complete local backup/restore, explicit OS-backup/privacy policy |
| DL28 | Could | Evaluate optional local app lock only through a separate gate |

## 6. Proposed UI direction

The source handoff recommends a hybrid, pending owner confirmation:

- **Today — Command Center Checklist:** Now/Overdue, deadlines, time-window groups, Core/all progress, quick capture, Done/Minimum/Later and optional completion detail. Usage evidence belongs lower on the screen or in Review.
- **Plan/Tomorrow — timeline:** blocks for *when*, checklist items for *what*, plus an Anytime lane, carryover inbox, overlap warnings and editable Top 3.
- **Focus — one thing:** a mode launched from Today/widget/notification, with a timer and explicit completion controls; not the only home screen.
- Suggested destinations: **Today · Plan · Habits · Review · More**. Keep existing usage, learning, earning, limits and settings reachable rather than silently deleting features.
- Keep widget freshness visible with an “as of” time; do not promise continuous refresh. Use accessible touch targets, TalkBack labels, large-text layouts, calm copy and resource-backed strings.

## 7. Persistent reminders: proposed behavior

Model reminders as persisted per-occurrence state, not repeated alarms sharing an ID. A typical flow is `SCHEDULED → DUE ↔ SNOOZED → DONE | MINIMUM_DONE | SKIPPED | RESCHEDULED`; unresolved items go through an explicit day-close decision.

Suggested defaults in the source handoff—not yet approved settings—are Core every 20 minutes up to four audible/vibrating fires, then a silent summary/widget badge; Standard once and possibly one later repeat; Flexible once or digest-only. Separate checklist caps from existing usage-digest caps. Quiet hours suppress sound/vibration, and global Pause/Less Often controls remain visible. Notification actions must be idempotent and stop repeats immediately after an owner action.

Rebuild scheduling after reboot, time/time-zone change, app update and permission restoration. If Android cannot provide exact timing, report the degraded/approximate behavior honestly. These are reminders, not emergency or medical alarms.

## 8. Data safety and privacy reference

The audit snapshot that accompanied the source handoff found:

- Room database version 5, `exportSchema = false`, and `fallbackToDestructiveMigration()` in `DayLedgerDatabase.kt`.
- No first-class habit/task/occurrence/completion model in the current database.
- Settings backup wired to an empty callback and CSV output containing a placeholder row in `MainActivity.kt`; the visible backup action is not proof of backup/restore.
- No explicit Android Auto Backup/data-extraction policy in the app manifest.

These observations must be rechecked against current code before execution. The safe sequence in the current roadmap is:

1. Phase 7 establishes a v5 baseline, complete owner-selected backup/restore, exported Room schema and explicit supported migration paths. Do not add checklist schema in this phase.
2. Before any schema-changing APK, the owner has a verified backup file. Test restores in isolation; use fictional migration fixtures; never use destructive fallback to conceal a missing migration.
3. The owner chooses whether Android OS backup is allowed or excluded. Do not infer this from defaults. Keep app data local except for a file the owner explicitly chooses.
4. New checklist history remains append/audit-friendly; archive before hard-delete; no committed personal records.

## 9. Current phase order and acceptance gates

`docs/ROADMAP.md` is the source of truth for complete phase definitions. The current proposed sequence is:

1. **Phase 7 — Data safety and migration foundation** (no checklist features).
2. **Phase 8 — Checklist core.**
3. **Phase 9 — Capture, deadlines and carryover.**
4. **Phase 10 — Persistent reminder engine.**
5. **Phase 11 — Now/Next widget and Focus mode.**
6. **Phase 12 — Evening Close, history and consistency.**
7. **Phase 13 — Onboarding, domains and accessibility polish.**
8. **Phase 14 — Exports and durability regression.**
9. **Phase 15 — Full device verification and final personal verdict (last).**

**Hard prerequisite:** the original P0 real-device usage check must pass before Phase 8; P0–P3 criteria are not rewritten. **MVP use gate:** after Phase 10, use the checklist MVP for 14 real days before Phase 11; agree measurable criteria before that trial. This does not replace the existing P0–P3 test. Each phase ends with green CI, `app-progress-apk`, a phase record and a stop at the merge gate.

The comprehensive device matrix is Phase 15. The earlier P0 prerequisite is deliberately retained because the repository rules say it gates later work; moving the full device phase last does not waive that rule or migration safety.

## 10. Acceptance-test themes

- Schedule generation: weekday/weekly/interval rules, archive/pause, date boundaries, template reapply and reboot must not duplicate occurrences.
- Actions: Done/Minimum/Skip/Reschedule/Carryover/Undo, stale notification actions and linked Learning/Earning records must be idempotent and auditable.
- Consistency: scheduled-day denominator, pauses/grace days, weekly targets, and Full vs Minimum must calculate honestly.
- Reminder engine: cadence/caps, quiet hours crossing midnight, snooze, permission denial/re-grant, reboot/time changes and separate general/Core budgets.
- Migration and backup: representative fictional rows across every legacy table; upgrade and restore preserve counts and key values.
- Device matrix: install/upgrade, offline flows, Usage Access/Accessibility degraded modes, notification/exact-alarm/battery restrictions, widget freshness, large text/TalkBack, and never-block protections.
- Every number must come from stored records and every automatic usage number must be labelled device-reported. Do not claim owner-device verification without evidence.

## 11. Reference authority

- Current phase order and Done means: [`../ROADMAP.md`](../ROADMAP.md).
- Current requirements and original P0–P3: [`../REQUIREMENTS.md`](../REQUIREMENTS.md).
- Owner decisions and open gates: [`../DECISIONS.md`](../DECISIONS.md).
- Repository rules: [`../../rule.md`](../../rule.md).

This sanitized reference intentionally omits personal profile details and the source attachment's superseded Phase 7–14 ordering. It should be updated only when owner decisions or verified repository evidence change.
