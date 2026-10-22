# Roadmap — Day Ledger (`io.github.pabi277.dayledger`)

Bootstrapped from AppFlow @ c8856022ca57cdf23ae78b9491481543db89438d on 2026-10-15

> One phase = one chat (`rule.md` §4). A phase is a decent chunk: one whole
> capability, 3–4 parts listed in its `docs/phases/NN-<slug>.md` record. Status:
> 📋 planned · 🚧 in progress · ✅ done · ❌ cancelled (keep the row). "Done
> means" always includes a green CI run whose **`app-progress-apk` artifact the
> owner can sideload and see the phase's increment** — sideloading is the normal
> install path for this personal-use app.
>
> **Historical baseline:** Phases 0–6 below retain the 2026-10-15 plan and status.
> On 2026-10-22 the owner directed the product toward a Daily Logger / external
> brain and asked for the comprehensive device-validation phase to be placed last.
> Proposed Phases 7–15 below reflect that order. The original P0–P3 kill test in
> `docs/REQUIREMENTS.md` §9 remains unchanged: **P0 is still a hard pre-feature
> gate before Phase 8**. Phase 15 is the full device matrix and final verdict; the
> earlier P0 check is a prerequisite, not that full device phase.
>
> The earlier four-week budget has not been waived or extended. Display-name,
> repository visibility/rename, detailed UI choice, reminder defaults and Android
> OS-backup policy remain owner gates. The DL IDs refer to the sanitized reference
> [`docs/reference/Daily-Logger-Handoff-for-New-Chat.md`](reference/Daily-Logger-Handoff-for-New-Chat.md)
> and still need to be added to `docs/REQUIREMENTS.md` before implementation.

| # | Phase (parts) | Done means | Status |
|---|---|---|---|
| 0 | **Bootstrap + hello** — 0.1 freeze `templates/` + `knowledge/` from the pinned SHA with the README provenance line; 0.2 commit these plan docs (`RESEARCH`, `REQUIREMENTS`, `ROADMAP`, `DECISIONS`), `rule.md`, `prompt.md`, MIT `LICENSE`; 0.3 minimal Gradle/AGP skeleton (Kotlin, Compose, Room, targetSdk 36, minSdk 26), one hello screen, one unit test; 0.4 `ci-build.yml` from the template, signing by secret **name** only, `app-progress-apk` debug artifact on every push | Snapshot + plan docs + LICENSE + README committed before any app code; CI green with a signed release artifact **and** the downloadable `app-progress-apk`; the APK installs on the owner's phone and opens on a hello screen showing "Day Ledger" | ✅ |
| 1 | **Usage insight** (R1–R7, R31 permission half) — 1.1 permission onboarding: walk the owner to Usage access + notifications, show honestly what was granted; 1.2 ingestion from `UsageStatsManager` (per-app foreground minutes, pickups, longest session) into Room, batched, offline, labelled device-reported; 1.3 **Today** screen: total, per-app list, category donut, hourly heatmap strip, "as of HH:MM"; 1.4 owner-editable categories (Earning / Study / College / Waste / Life) + per-category and per-app daily budgets (ceiling **and** floor) | Real per-app time for today and the past 7 days renders offline and matches the system's own Digital Wellbeing numbers within ~10% on the owner's device (**kill test P0**); assigning ProRewards to *Earning* and Instagram to *Waste* changes the chart and the budget bars immediately; APK shows the populated Today screen and the category editor | ✅ |
| 2 | **Sweet notifications** (R9–R12) — 2.1 notification engine with warm, non-shaming templates and live-preview wording editor (all text in resources, user-editable); 2.2 full customization: per-notification on/off, times, repeat days, scope (apps/categories), tone, sound/vibration, **quiet hours** that are never violated; 2.3 threshold triggers (category over budget, total passed N, app in foreground N continuous minutes) with exact alarms that survive reboot and doze; 2.4 anti-nag discipline: daily cap (default 6), de-duplication, one-tap "less often" on every notification | A 21:00 nudge reads e.g. *"Today: 5h 40m 🌙 Waste 52m over your 45m"* and the owner can rewrite that sentence; quiet hours verified silent over one night; crossing the Waste budget fires exactly one nudge; a heavy day produces ≤ the cap; APK shows the notification list, the wording editor and a fired nudge | ✅ |
| 3 | **Widget + planning** (R13–R15, R20–R23) — 3.1 Glance home-screen widget: today's total, top/waste app, next block + countdown, today's ₹, study streak, "as of" stamp, dark/light aware, resizable; 3.2 widget tap targets + long-press field configuration, refresh on screen-on and via periodic worker; 3.3 24-hour timeline of proportional blocks with add/edit/move/resize and an explicit overlap indicator; 3.4 **Tomorrow plan** evening screen (template pre-fill, three-tap adjust, reachable from a notification action and a widget tap) + day templates (*College / Study / Earning / Off*, last-used sorting, multi-day apply) + per-block exact-time reminders | The widget shows live-ish numbers after a screen-on **without opening the app**; the 22:00 nudge opens Tomorrow directly and three taps save a plan that becomes today at midnight; a 19:00 study block fires with the screen off; overlapping blocks are visibly flagged; APK shows the widget, the timeline, the template picker and Tomorrow | ✅ |
| 4 | **Limits & blocking** (R16–R19) — 4.1 per-app/per-category daily limits with an owner-editable **block-duration menu** (15 min / 30 min / 1 h / until tomorrow / until-I-unlock); 4.2 Accessibility-service + overlay block screen that is kind, not punitive (what was used, what the plan says, "extend 10 min once", "skip to next block", "unlock anyway" with hold + PIN); 4.3 every override logged and surfaced in the weekly report — no silent cheating; 4.4 **always-allowed list** (dialer, SMS, UPI/banking, maps, camera, college apps, and earning apps while a session runs) + focus session ("study 50 min") with widget countdown | Hitting the Waste ceiling locks the chosen apps; picking "30 min" unlocks exactly 30 minutes later with no further taps; **ProRewards can never be locked by a Waste limit while an earning session runs** (unit-tested); "unlock anyway" needs a 5-second hold + PIN and appears in the report; a 50-min focus session survives the screen turning off. **Fallback recorded now:** if Accessibility/overlay proves unworkable on the owner's device, phase 4 ships limits as notification-only nudges and phases 5–6 proceed unchanged — the app is still complete and useful | ✅ |
| 5 | **Earnings + learning** (R25–R28) — 5.1 two-tap earning log (source: ProRewards / OfferPro / other; task type; amount defaulting to ₹12.50 per install, editable; installed app); 5.2 auto-time join: read the installed app's foreground minutes from phase-1 data and compute **real ₹ per hour** per source and task; 5.3 earnings insight: today's ₹, month-to-date ₹, run-rate, best ₹/hour band, flag when a source falls below his study value — **no income promise anywhere**; 5.4 daily learning tracker: minutes + free text/checklist + streak chain with visible gaps, surfaced on the widget | A ₹12.5 install that took 24 minutes of foreground time is reported as ₹31.25/h and the weekly ranking orders sources by ₹/hour; a day with zero learning shows a gap in the chain and logging one item restores it; APK shows the earning log, the ₹/hour ranking and the streak | ✅ |
| 6 | **Review + durability** (R6, R24, R29–R31, R33) — 6.1 weekly report (Sunday night): planned vs actual by category, top 3 leaks, unlocks, ₹ and ₹/hour, learning minutes, override count, best/worst day, **one** suggestion for next week; 6.2 week-vs-week deltas and "shift the rest of my day by N minutes" with undo; 6.3 local backup to an owner-chosen file (SAF) + restore with schema versioning + CSV export + weekly backup reminder; 6.4 battery/perf audit (batched polling, WorkManager, no needless foreground service), theme + large-font pass, and a Settings screen showing the app's own battery cost and notification count | Clear app data → restore from the backup file → lose nothing; CSV opens correctly in a spreadsheet; every report number is recomputable from raw rows (unit-tested); one normal day adds no visible battery drain versus the day before (owner-observed and recorded); APK shows the weekly report, backup/restore round trip and the Settings audit | ✅ |

> **Durability audit note (2026-10-22):** Phase 6 is historically marked complete,
> but the current source does not prove its backup/restore claim: Settings wires
> Backup to an empty callback and CSV writes a placeholder row
> (`MainActivity.kt:90–100, 180`; `SettingsScreen.kt:24–30`). Keep the historical
> phase record; Phase 7 below closes this gap. Do not treat the old acceptance
> statement as verified on the current tree.

## Proposed phases 7–15 — Daily Logger evolution

> All rows below are **planned**, not implementation authorization. One phase per
> chat. Every phase must end with CI green and a downloadable `app-progress-apk`,
> a phase record and updated roadmap/prompt, then stop at the merge gate. The full
> device-validation phase is deliberately Phase 15. The original P0 rule still
> requires a minimum real-device usage check to pass before Phase 8; if P0 fails,
> stop and fix the foundation rather than building checklist schema on top of it.

### Phase 7 — Data safety and migration foundation (no checklist features)

**Parts**

- **7.1 — Establish the v5 baseline:** inventory the existing Room tables and
  supported installed DB versions; reconstruct and test the exact v5 schema using
  fictional fixtures. Do not copy owner data into the repository.
- **7.2 — Full local backup/restore:** implement an owner-chosen SAF backup with a
  format/schema version and restore path covering every existing table; prove a
  round-trip in an isolated database and prepare a verified owner backup before any
  later schema-changing APK is installed.
- **7.3 — Migration safety:** export/check in the v5 Room schema, add explicit
  migrations for supported versions, remove destructive fallback only when the
  upgrade paths are covered, and fail safely rather than wiping unknown data.
- **7.4 — Privacy and CI hygiene:** record the chosen Android OS-backup policy and
  verify its effective rules; stop duplicate CI-failure issue creation without
  closing the existing issues; retire or correct the unused signed-release path so
  the required personal-use CI result is green.

**Done means:** v5 fictional rows survive backup/restore and migration tests;
current data can be saved to an owner-selected file; no supported upgrade path can
silently destroy data; P0 passes on the owner's phone before Phase 8; CI uploads
`app-progress-apk`. No checklist UI or checklist-table migration ships in this
phase. The full device matrix remains Phase 15.

### Phase 8 — Checklist core

**Parts**

- **8.1 — Model and generation:** add distinct habit rules, one-off tasks, dated
  checklist occurrences and completion logs; schedule generation is idempotent.
- **8.2 — Today checklist:** Now/Next and time-window groups; one-tap Done,
  Minimum, Later, Skip and Undo; completed items collapse.
- **8.3 — Optional evidence:** after ticking, allow minutes/quantity/topic/note or
  no detail; usage evidence may suggest but never silently complete an item.
- **8.4 — Habits and defaults:** generic editable domains/templates, weekday and
  weekly schedules, archive and pause basics; no owner routine or timetable seeded.

**Done means:** a weekday habit and one-off task create only the correct
occurrences; Done works without a detail form; Minimum is distinct from Full; Undo
works; restart/reboot adds no duplicates; Phase 7 data remains intact after upgrade.

### Phase 9 — Capture, deadlines and carryover

**Parts**

- **9.1 — Quick Capture/Inbox:** title-first capture from Today and useful entry
  points, with organisation optional.
- **9.2 — Due tasks:** date-only and timed deadlines, sorted by urgency and shown
  in Today/Plan.
- **9.3 — Tomorrow merge:** template blocks, due tasks and carryovers compose
  without duplicates; Top 3 is editable.
- **9.4 — Leftover decisions:** Move, Schedule, Shrink to Minimum or Drop/archive;
  show carryover count and repeated-carryover flag.

**Done means:** title-only capture saves within 10 seconds; template reapply and
midnight rollover do not duplicate occurrences; every unfinished item gets an
explicit next action rather than silently rolling forever.

### Phase 10 — Persistent reminder engine

**Parts**

- **10.1 — Profiles/state machine:** per-item/domain/global cadence, repeat state,
  quiet-hour handling, caps and persisted next-fire state.
- **10.2 — Actions:** Done, Minimum, Later/Snooze, Reschedule and Skip where Android
  permits; deduplicate stale or repeated taps and write one completion log.
- **10.3 — Controls:** global Pause (1 hour/today), Less Often, separate general
  and Core caps, and silent visibility after audible repeats.
- **10.4 — Reliability health:** restore scheduling after reboot, clock/time-zone
  change, app update and permission re-grant; show when Android timing is approximate.

**Done means:** a Core reminder repeats only within its editable profile, stops on
an action, never sounds during quiet hours, survives reboot and creates exactly one
log; Flexible reminders remain gentle. Android/OEM limits are disclosed honestly.

> **MVP use gate after Phase 10:** before Phase 11, use the checklist MVP for 14
> real days. Agree the measurable criteria before starting. Record capture/tick/day
> close effort, reminder pauses/disables, battery observations and forgotten
> captured Core items. If the flow feels like homework or reminders are disabled,
> fix friction before expanding. This does not replace or rewrite P0–P3.

### Phase 11 — Now/Next widget and Focus mode

**Parts:** 11.1 compact/wide Glance widgets with Now/Next/Core-left and an “as of”
stamp; 11.2 Focus One Thing with timer and explicit Done/Minimum; 11.3 link focus
blocks and optional minutes evidence; 11.4 safe deep links/Undo and optional limits
integration that preserves earning/always-allowed protections.

**Done means:** the widget answers Now/Next/Core-left without opening the app and
shows freshness; timer minutes are only suggested until confirmed; checklist use
works with Accessibility off and never requires a checklist to unlock essential
apps.

### Phase 12 — Evening Close, history and consistency

**Parts:** 12.1 short Evening Close with leftovers, one-line learning and tomorrow
Top 3; 12.2 weekly targets, pause-aware consistency and Full/Minimum distinction;
12.3 date/domain/text history search with honest edited timestamps; 12.4 weekly
reset/report with linked raw logs and one main suggestion.

**Done means:** a normal close meets the agreed time target; paused/unscheduled days
are excluded correctly; 5-of-7 targets work without a perfect streak; report figures
drill to source records.

### Phase 13 — Onboarding, domains and accessibility polish

**Parts:** 13.1 skippable upgrade onboarding and domain/template manager; 13.2
large-font, TalkBack, dark-theme, touch-target and reduced-motion pass; 13.3 battery
and performance audit; 13.4 resource-backed copy and empty/error/permission states.

**Done means:** each domain can be disabled without orphan items/reminders; the
core journey works with large text and TalkBack; no invented personal schedule or
routine appears in defaults.

### Phase 14 — Exports and durability regression

**Parts:** 14.1 CSV exports for checklist/completion/day-close records; 14.2 backup
format and restore round-trip for the complete current schema; 14.3 migration-chain
regression from v5 through current, including representative fictional rows in every
legacy table; 14.4 privacy/OS-backup, deletion, battery and notification-count audit.

**Done means:** restore preserves old and new history/settings; exports are
spreadsheet-valid; migration tests cover every supported version; privacy copy
matches actual backup behavior. No personal records are committed.

### Phase 15 — Full device verification and final personal verdict (last phase)

**Parts**

- **15.1 — Install/upgrade and durability:** fresh install, upgrade-over-v5,
  owner-chosen backup/restore, offline operation and date/time-zone rollover.
- **15.2 — Permission and reminder matrix:** notifications/exact-alarm denied and
  re-granted, quiet hours, pause, reboot, battery saver/OEM delays and degraded-mode
  explanations.
- **15.3 — Daily Logger journey:** one-handed capture/tick/close, widget refresh
  honesty, large text/TalkBack, Accessibility-off path, and never-block earning and
always-allowed checks.
- **15.4 — Record the original P0–P3 outcome and a personal verdict:** use the
  existing criteria unchanged; report observed effort, reminder behavior, battery
  observations and forgotten captured items. Do not claim a pass without owner-side
  evidence.

**Done means:** the full owner-device matrix is recorded with evidence and
reproductions; P0–P3 results are reported without rewriting their thresholds; the
owner can decide keep/shrink/stop from actual use. Reuse the Phase 10 trial data
unless the owner separately approves a second trial.

## Held / cancelled (keep visible)

- **Publishing track (store listing, monetization, closed test, release prep)** —
  ❌ cancelled 2026-10-15: the owner scoped the app to **his own use cases only**.
  The earlier "paid upfront, no ads" decision is moot with no store listing; the
  **no-ads, no-analytics, no-account** invariants stay (nobody revoked them).
  Publishing may only be reconsidered as a fresh decision with a fresh plan after
  kill test P1–P3 pass.
- **In-app feature blocking** (block Reels/Shorts inside an app, R8) — ❌ out of
  scope 2026-10-15: needs Accessibility reading other apps' UI trees, is fragile
  across app updates and OEMs, and is precisely where the 1Cr+-install incumbent
  fails in its own 1★ reviews. Whole-app blocking (phase 4) covers the need.
- **Automatic earning detection from ProRewards/OfferPro** — ❌ out of scope
  2026-10-15: no API, no scraping, no reading their notifications without an
  explicit owner request. Earnings stay a two-tap manual log.
- **Bengali locale** — ❌ out of v1 (owner chose English-only, 2026-10-15). All
  strings live in resources so `values-bn/` stays cheap later.
- **Cloud sync, multi-device, web companion, accounts** — ❌ never (law 13:
  offline-first, no backend without proven need; and this app has one user).
- **Our own gamification/coins** — ❌ rejected 2026-10-15: he already earns coins
  elsewhere; a second reward loop would compete with his real goal.
