# Phase 10 — Persistent reminder engine

Status: **implemented, CI green, awaiting the owner's device check.** Not marked Done.
Phase 10's own criterion is "survives reboot and creates exactly one log"; CI can prove the
rules and the SQL, but it cannot prove what an OEM's battery manager does to an alarm on
the owner's phone.

## 1. Scope taken from the repo

`docs/ROADMAP.md` Phase 10 has four parts: 10.1 profiles and the state machine, 10.2 the
notification actions, 10.3 the controls, 10.4 reliability health. `docs/REQUIREMENTS.md`
adds the detail that shaped the design: state is persisted **per dated occurrence** with a
stable identity and fire sequence, the three starting profiles are Core / Standard /
Flexible, quiet hours are a no-fire window that never bursts, and Pause, Less Often and the
two daily caps are mandatory and independent.

The existing code had no checklist reminder at all: `notification_rules` covers the usage
reminders from R9/R12 only, and `checklist_occurrences` carried just `status` and
`snoozeUntil`. The `limitMin` lesson from F2 applies here too — a column that no code reads
is not a feature — so this phase wires a write path, a read path and a decision rule
together.

## 2. Schema v7 → v8

Two tables, pure addition. No existing column is renamed, retyped or dropped, so no owner
row is rewritten on upgrade and a v7 backup still restores (the new tables simply start
empty and are rebuilt from the occurrences already present).

| Table | Why it exists |
|---|---|
| `reminder_states` | Current state of one occurrence: profile, `nextFireAt`, `audibleFires`, `totalFires`, `lastFireAt`, and the last `degraded` reason. Primary key **is** the occurrence id, so one occurrence can only have one row. |
| `reminder_events` | Append-only: every fire, action, ignored stale tap, withheld fire and scheduling failure. The caps are counted from here. |

`MigrationTest`'s expectation table is extended to v8 and stays the drift check it has been
since Phase 8 — it is what caught the real v5 autoincrement drift.

## 3. The rules, kept pure

`reminders/ReminderRules.kt` holds every "may this ring, and when" decision with no Android
types, so the awkward cases are testable:

- **Profiles.** Core: every 20 min, at most 4 audible fires. Standard: initial + one repeat
  at 50 min. Flexible: one gentle fire. Less Often doubles the interval and halves the
  audible budget of the selected profile only, never below one.
- **Quiet hours.** A fire that would land inside the window moves to the window's end; a
  window that wraps midnight is handled directly, and a zero-length window means "off".
- **Pause and snooze** preserve due state and resume at their own time. Neither completes,
  skips nor reschedules the item.
- **Caps** are separate for Core and everything else, and reset at local midnight.
- **No catch-up burst.** A computed fire time already in the past is clamped to *now* and
  returned as a single fire. Being three hours late does not mean twelve notifications.
- **Terminal states.** Done, Minimum, Skip, carried, scheduled and dropped all stop the
  occurrence; yesterday's item never rings today.

## 4. Actions and idempotency

The notification carries Done, Minimum, Later and Skip. All four route into
`ChecklistRepository.act` — the same function the Today screen calls — so a tap on a
notification and a tap on the row write the same append-only `completion_logs` entry.

Idempotency is guarded twice, which is DL13's "deliver the same intent twice" test:

1. the receiver reads the status first and, if the occurrence is already settled, records a
   `stale` event and writes nothing else;
2. the repository ignores an action on a closed occurrence.

`Later` is a snooze: the occurrence stays open and the reminder returns when the snooze
expires. The other three cancel the remaining fires and the notification.

Ordering inside the alarm receiver is deliberate — plan, post, record, re-plan. A fire is
written only after the notification was actually posted, so the log never claims a reminder
the owner did not receive, and the next alarm is computed from counters that already
include this fire.

## 5. Reliability (10.4)

`ReminderScheduler` is the only class that touches `AlarmManager`, and it is **rebuildable
rather than repairable**: `BootReceiver` now rebuilds on boot, `TIME_SET`,
`TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED` by re-deriving every alarm from persisted
state — the same path as a normal launch, so there is one implementation instead of three.
The request code comes from the occurrence id and the pending intent is replaced in place,
so repeated syncs cannot multiply alarms. The app also rebuilds on every launch, which is
the recovery path after an update or a permission re-grant.

**Degradation is recorded, not hidden.** When `canScheduleExactAlarms()` is false the alarm
is still set with `setAndAllowWhileIdle`; `reminder_states.degraded` is stamped, an event is
appended, and the Reminders screen says the timing is approximate instead of showing a
punctual time that may never happen. The same applies when notifications are switched off:
nothing is recorded as delivered, and the alarm is retried in ten minutes.

## 6. What the owner sees

A **Reminders** screen under More:

- **Right now** — on or paused until a visible time, the next scheduled reminder, and Pause
  1 h / Until tomorrow / Resume now.
- **How Android is treating us** — notifications allowed or blocked, exact alarms allowed or
  approximate, and how many of today's reminders are on approximate timing.
- **Quiet hours** — editable from/to with the wrap-midnight behaviour stated.
- **Daily caps** — separate Core and general steppers, with the note that the usage-reminder
  cap from R12 is a different cap and is unaffected.
- **Less often** — one switch per profile, with the cadence spelled out.
- **Today's items** — every open occurrence with its next fire, how many times it has
  sounded, and a Core / Standard / Flexible selector. That is the per-item edit DL12 needs.

## 7. Evidence — CI only so far

Run `5f61bc2`, Android CI, success:

- **147 unit tests** (113 before this phase; 28 rule tests + 6 SQL tests).
- `app-progress-apk` 18,688,002 B, `room-schema` 6,413 B, signer unchanged at
  `533D76ED5C2E264779EE005C`.
- The migration guard proves v1–v8 all reach v8 with every row intact and the exact
  expected table set.

**This is build evidence, not device evidence.** Nothing in the list below has been
observed on a phone.

## 8. Owner device check (owed)

Install `app-progress-apk` from run `5f61bc2` and record each:

1. **Fires at all** — a reminder appears for an open item; Done from the notification
   completes it in Today.
2. **Exactly one log** — tapping Done twice (or tapping after completing it in the app)
   leaves one completion, and the second tap shows no second entry.
3. **Core cadence** — a Core item sounds every ~20 minutes, stops making noise after four,
   and stays visible until acted on.
4. **Quiet hours** — with quiet hours covering now, nothing sounds; the item is still
   pending afterwards, and its next reminder is the moment quiet hours end.
5. **Pause** — Pause 1 h suppresses reminders and says the resume time; Resume now brings
   them back.
6. **Survives reboot** — reboot the phone; the reminder still fires at its time.
7. **Honest timing** — if Android denies exact alarms, the screen says timing is
   approximate rather than claiming a precise time.

## 9. Deliberately not in this phase

- **Widget and Now/Next** — Phase 11.
- **Focus mode and block reminders** (R23 per-block alarms) — Phase 11 and the planning
  increment; this phase covers checklist items only.
- **Per-domain profile editing UI** — the storage key and lookup exist
  (`reminder_profile_domain_<domain>`), but only per-item and per-app-wide profiles are
  exposed. Deliberate: a half-built domain editor is worse than none, and the owner has no
  domains set yet.
- **A silent "ongoing" notification re-posts itself** — the last audible fire posts as
  ongoing and stays until acted on; it is not refreshed on a timer.

## 10. Known limitation, stated plainly

Android's Doze and OEM battery managers can delay even exact alarms. This phase makes the
app honest about that (a visible health panel and a per-item "approximate timing" marker)
rather than promising punctuality it cannot guarantee. If the owner's phone turns out to
suppress alarms entirely, that is a Phase 15 permission-matrix finding, not something to
paper over here.
