# Daily Logger — Sanitized Product Handoff

> **Reference copy derived from the owner-provided `Daily-Logger-Handoff-for-New-Chat.md` (2026-10-22).** Personal identifying, academic, financial, timetable, routine and other owner-specific details have been omitted. This is a product reference, not a record of personal data.
>
> **Current phase-order authority:** `docs/ROADMAP.md` is current. The full device-validation phase is **Phase 15, last**. The earlier Phase 7 device-first sequence in the source handoff is superseded. The existing P0 pre-feature check and Phase 7 data-safety gate still apply.
>
> Read this alongside `rule.md`, `docs/REQUIREMENTS.md`, `docs/DECISIONS.md` and `prompt.md`. Trust verified current code and the latest owner decisions over this summary. The product choices below are answered, not pending. This reference does not authorize Phase 7 implementation; the current closure is documentation-only.

## 1. Product direction and locked constraints

- Evolve the existing Android app into a **personal Daily Logger / external brain**; do not create a separate checklist app.
- Organize the owner's whole life using optional, independently disable-able domains. Keep defaults generic and editable; do not invent a real timetable, routine, task list or personal seed data.
- Make completion **one tap**, with optional detail afterward. A due item should remain visible/remind until the owner chooses an action, while respecting quiet hours, caps, pause controls, Android permission limits and a non-shaming tone.
- Keep the app single-user, offline-first and English-only: no account, backend, ads, analytics, crash SDK, cloud sync, social features or Play Store track.
- Preserve package/database identity `io.github.pabi277.dayledger`, MIT license, the fixed permission philosophy and the sideloaded CI `app-progress-apk` path. Do not add permissions without a gate.
- Usage statistics are evidence, not proof of learning or completion. Never auto-complete a study or other item from foreground app usage.
- Preserve earning-app and always-allowed protections. Checklist completion must never hold essential apps or the phone hostage.
- Visible label **Daily Logger** is approved for the Phase 8 checklist front door. Until then, the current APK label remains **Day Ledger**; package/database identity stays `io.github.pabi277.dayledger`.
- The repository remains **public** under its current name. Do not rename it in this task. Keep all committed examples and fixtures generic and sanitized; do not restore personal profile, timetable, routine or financial details from an unsanitized source.

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

## 5. Requirement families (DL IDs)

DL1–DL28 are now incorporated into `docs/REQUIREMENTS.md` with priorities,
related R-IDs where applicable and acceptance tests. They supplement, and do not
replace or rewrite, the original R1–R34 and P0–P3 criteria.

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

## 6. Approved UI direction — hybrid A+B+C

The owner approved this direction on 2026-10-22; detailed layouts and states are
in [`../UI_DESIGN.md`](../UI_DESIGN.md):

- **Today — Command Center Checklist:** Now/Overdue, deadlines, time-window
groups, Core/all progress, Quick Capture and Done/Minimum/Later/Reschedule/Skip/
Edit/Undo. Optional detail follows a tick and may be skipped. Usage belongs below
the checklist as supporting **Phone evidence**.
- **Plan/Tomorrow — timeline:** blocks describe *when*, checklist items describe
*what*, with an Anytime lane, explicit carryover, overlap warnings and editable
Top 3.
- **Focus — One Thing:** optional mode launched from Today/widget/notification,
with a timer and explicit completion controls; not the only home screen.
- Primary navigation is **Today · Plan · Habits · Review · More**. Keep Usage, Earn,
Learn, Limits, Categories, Notifications and Settings reachable.
- Define compact and wide widgets, stable deep links, stale-action behavior,
empty/error/permission states, ≥48dp touch targets, TalkBack, large text, dark theme
and calm resource-backed copy. Do not claim continuous widget refresh or exact
Android alarm delivery.

## 7. Persistent reminders: approved defaults and state

Persist state per dated occurrence, not as repeated alarms sharing an ID. A normal
flow is `SCHEDULED → DUE ↔ SNOOZED → FULL | MINIMUM | SKIPPED`; reschedule and
day-close carryover are explicit recorded transitions. Use a stable occurrence
identity and fire sequence. Duplicate or stale notification actions must result in
at most one transition and one completion log.

The owner approved these editable defaults on 2026-10-22:

- **Core:** every 20 minutes per occurrence, at most four sound/vibration fires
  for that occurrence (subject to its separate daily Core cap), then silent
  ongoing/widget visibility until action or day close.
- **Standard:** initial reminder plus one repeat after 45–60 minutes.
- **Flexible:** one gentle reminder or a digest.

Quiet hours are a no-fire window: no new alert, sound or vibration; due state is
preserved for the next eligible time without a catch-up burst. Global Pause
suppresses checklist reminders through its selected resume time/day close without
changing occurrence state or habit schedules. Less Often applies only to the
selected trigger/profile. Separate daily Core/general checklist caps reset at local
day close and remain distinct from the existing R12 usage-reminder cap. Profiles
and caps are editable per item, domain and globally. Rebuild scheduling after
reboot, time/time-zone change, app update and permission restoration. If Android
cannot provide exact timing, explain the degraded/approximate behavior honestly.
These are reminders, not emergency or medical alarms.

## 8. Data safety, migration and signing reference

The current-source audit found Room version 5, `exportSchema = false` and
`fallbackToDestructiveMigration()`; no first-class habit/task/occurrence/completion
model; an unwired Settings backup callback; placeholder CSV output; and no explicit
Android OS backup/data-extraction policy. Recheck source before implementation.

Git history records Room versions 1–5 (table inventory and supported forward
paths are in `docs/ROADMAP.md`): v1 first appears in Phase 1, v2 in Phase 2, v3 in
Phase 3, v4 in Phase 4, and v5 in Phase 5; Phase 6 remains v5. Versions 2–5 used
destructive fallback, and no explicit migration objects/schema exports were
observed. Do not assume historical user data was preserved or currently empty.
Phase 7 must cover explicit v1→v2→v3→v4→v5 paths with fictional fixtures,
ending at the current v5 schema; it does not bump the version or add checklist
tables. When a later schema (for example, Phase 8) is introduced, add the next
migration and rerun the full supported v1–v5 source-version matrix.

The owner approved **excluding the app database and all app-private data from
Android cloud backup and device-to-device transfer**. The only backup is a file the
user explicitly exports through SAF. Phase 7 must implement and verify
`android:allowBackup="false"`, applicable legacy full-backup rules and API 31+
`android:dataExtractionRules` for both cloud backup and device transfer. A
scheduled reminder may prompt the user to export but must not silently create an
app-private or OS backup.

Signing evidence remains unresolved as of 2026-10-22:

- Progress runs `39766581703` and `39766005737` both produced `app-progress-apk`
  artifacts and passed CI, but artifact download attempts were blocked by the
  sandbox's outbound-host allowlist. Certificate fingerprints have not been
  inspected.
- Main run `38845505942` passed tests/progress APK but failed its signed-release
  job at **Prepare signing key on runner**. Its accessible artifact inventory has
  no signed-release artifact. No release fingerprint is known.
- Workflow secret names only: `ANDROID_KEYSTORE_BASE64`,
  `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. No
  secret values were accessed. Do not retire or rotate signing until evidence is
  known.

Phase 7 must compare fingerprints from at least two distinct progress APKs and
any accessible main signed release. If continuity is unstable **or cannot be
established**, stop at an owner gate for a stable owner-controlled signing plan;
write secret names only. Phase 7 Done also requires update-over-install proof or
an explicit owner-approved install/migration procedure that preserves database
history. No checklist schema ships before these safety gates are complete.

## 9. Current phase order and acceptance gates

`docs/ROADMAP.md` is the source of truth for complete phase definitions. Approved
planned sequence:

1. **Phase 7 — Data safety and migration foundation** (no checklist features).
2. **Phase 8 — Checklist core**; the visible label becomes Daily Logger here,
   without changing package/database identity.
3. **Phase 9 — Capture, deadlines and carryover.**
4. **Phase 10 — Persistent reminder engine.**
5. **Phase 11 — Now/Next widget and Focus mode.**
6. **Phase 12 — Evening Close, history and consistency.**
7. **Phase 13 — Onboarding, domains and accessibility polish.**
8. **Phase 14 — Exports and durability regression.**
9. **Phase 15 — Full device verification and final personal verdict (last).**

There is no fixed calendar deadline for proposed Phases 7–15. MVP is Phases 7–10,
then a **14-day real-use gate before Phase 11**; agree measurable criteria before
that trial. Work one owner-approved phase per chat. The original P0 real-device
usage check remains mandatory before Phase 8, and P0–P3 wording/criteria are not
rewritten.

Every implementation phase requires green CI, its `app-progress-apk`, an owner
install and a short phase-specific smoke check recorded with evidence. Phase 15 is
the full device matrix; it does not waive earlier install/smoke checks. The current
closure is documentation-only and does not start Phase 7. Stop at the merge gate.

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
