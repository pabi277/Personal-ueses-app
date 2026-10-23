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
> Owner gates closed 2026-10-22: the visible label **Daily Logger** is approved
> for Phase 8 (the current shipped label stays Day Ledger through Phase 7); the
> UI direction is Today checklist / Plan timeline / Focus One Thing; Android OS
> backup is excluded for app-private data; the reminder defaults and controls are
> approved; the repository remains public and is not renamed. Keep package/database
> identity unchanged. The historical four-week statement applies only to Phases
> 0–4; there is **no fixed calendar deadline for proposed Phases 7–15**. MVP is
> Phases 7–10 followed by the 14-day real-use gate before Phase 11. One
> owner-approved phase per chat. DL1–DL28 are now specified in
> [`docs/REQUIREMENTS.md`](REQUIREMENTS.md); this roadmap remains a plan, not
> implementation authorization.
>
> **Install evidence rule:** every implementation phase requires the owner to
> install that phase's `app-progress-apk`, perform and record a short phase-specific
> smoke check. Phase 15 is the full device matrix; it does not replace the smaller
> install/smoke check at each earlier phase. A CI artifact alone is not device proof.

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

> ✅ **Status: Done (2026-10-23) — P0 passes on the owner's phone: Today 2 h 47 m
> against Digital Wellbeing's 2 h 54 m (−4.0 %) and top app Chrome 57 m against 1 h 2 m
> (−8.1 %), both inside the ~10 % the kill test allows.** The signing gate failed first
> and was closed by evidence afterwards: progress APKs were signed by a per-runner
> throwaway key (measured — `DC13157E…` vs `B988DBD3…` on one tree) and the release job
> was missing the name `ANDROID_KEYSTORE_BASE64`; the owner then created a keystore
> outside the repo and set the four secret names, and four runs across three commits now
> print one signer, `533D76ED5C2E264779EE005C…`. Phase 7 work, all CI-verified: Room v5
> schema export; explicit non-destructive `1→2→3→4→5` migrations with the destructive
> fallback removed (no version bump); versioned SAF backup/restore over every table and
> setting; `allowBackup="false"` plus legacy/API-31 extraction rules verified in the
> merged manifest; and a `fix(data)` for `app_info` rows that `INSERT OR IGNORE` had been
> silently dropping since Phase 1, found from the owner's own backup. **On device:** install + launch, a complete backup
> (14 tables/538 rows, then 613 rows after the fix), a non-backup file refused,
> update-over-install of a later owner-keyed APK, and a restore corroborated by the
> owner's own exports — seven historical days return identical after clear-data and six
> configuration tables return row-for-row identical; the `app_info` fix shows 0 → 72
> labelled rows. **P0 was measured, failed at +97 %, the measurement was rebuilt as
> screen-on clipped foreground time with launcher/system UI/dialer excluded, and the
> re-measure passed** — the failure and its diagnosis are kept in the phase record because
> they are what closed the gate. Evidence:
> [`docs/phases/07-data-safety-migration.md`](phases/07-data-safety-migration.md).

#### Historical Room inventory (verified from Git history, 2026-10-22)

The database was introduced in Phase 1 and is still Room version 5 in Phase 6.
The entity-file diffs between these versions add tables; the historical table sets
are:

| Room version | First recorded phase / commit | Tables present at that version |
|---:|---|---|
| 1 | Phase 1, `fe58d9f` | `app_usage`, `hourly_usage`, `app_info`, `categories` |
| 2 | Phase 2, `ad0948a` | Version 1 plus `settings`, `notification_rules`, `notification_log` |
| 3 | Phase 3, `8974737` | Version 2 plus `blocks`, `day_plans`, `templates`, `template_items` |
| 4 | Phase 4, `5970aed` | Version 3 plus `limit_events` |
| 5 | Phase 5, `1ff9a87` | Version 4 plus `earnings`, `learning`; Phase 6 commit `3aec2e0` remains at v5 |

At the observed historical commits, `exportSchema = false`; there are no explicit
Room migration objects. The v2–v5 builders use `fallbackToDestructiveMigration()`;
v1 has no recorded explicit migration path. Consequently, do not assume that any
historical install preserved data across upgrades or that a currently installed
v5 database is empty. A missing/unknown schema must fail safely, not be wiped.

**Supported forward paths for Phase 7:** fresh installs and all existing versions
end at the current Room v5 schema. Implement explicit steps `1 → 2 → 3 → 4 → 5`;
test each individual migration and upgrades from every supported start version
(v1, v2, v3 and v4) into v5. Version 5 is the current baseline, not a new Phase 7
schema bump. No downgrade path is promised. When a later schema version is added
(for example, the Phase 8 checklist schema), add the next explicit migration and
rerun the full v1–v5-to-new-version matrix before shipping it. Keep fictional
fixtures for every legacy table, including representative keys and values, and
assert that all source rows survive.

#### Signing-continuity audit status (evidence gathered 2026-10-22)

- Two separate assigned-branch progress runs are known: run `39766581703` (commit
  `c07b939`, artifact `12421000391`, success) and run `39766005737` (commit
  `ef3aca0`, artifact `12420331419`, success). Their artifacts are named
  `app-progress-apk`. Metadata is available, but attempts to download both archives
  were blocked because GitHub redirects them to `blob.core.windows.net`, outside
  this environment's outbound-host allowlist. No APK certificate fingerprints
  have therefore been inspected; matching file sizes are not signing evidence.
- The available main run `38845505942` succeeded for tests/progress APK but failed
  the `Signed release AAB and APK` job at **Prepare signing key on runner**. Its
  artifact inventory contains only `app-progress-apk`; no accessible
  `signed-android-release` artifact was found in the repository inventory. The
  main signed-release fingerprint is unknown, not presumed stable.
- The workflow names the release secrets
  `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
  `ANDROID_KEY_PASSWORD`. No secret values were read or copied. Do not retire,
  rotate or replace any signing identity until evidence is known.
- When artifacts are accessible, record SHA-256 signer-certificate fingerprints
  for at least two distinct progress APK runs and any accessible main signed
  release, then verify update-over-install on the owner's device. If continuity is
  different **or cannot be established**, stop at an owner gate for a stable,
  owner-controlled signing plan. Document secret **names only**; never request or
  store secret values in repository files or chat.

**Parts**

- **7.1 — Schema baseline:** export/check in the current Room v5 schema; use the
  historical inventory above to build fictional fixtures for v1–v5. Phase 7 does
  not bump the database version or add checklist tables. No owner database or
  personal data enters the repository.
- **7.2 — Complete local SAF backup/restore:** implement a versioned export and
  restore for every table, relation, setting and reminder state; verify a
  round-trip in an isolated install and prepare an explicit user-selected backup
  before later schema-changing APKs.
- **7.3 — Non-destructive migrations:** implement and test every supported Phase 7
  forward step v1→v2→v3→v4→v5; remove destructive fallback; preserve all fixture
  rows; fail safely and clearly for an unsupported schema instead of clearing it.
  Any later schema bump must add the next explicit migration and rerun all
  supported legacy start-version paths before that APK ships.
- **7.4 — Exclude Android OS backup:** implement `android:allowBackup="false"`,
  legacy `android:fullBackupContent` rules where applicable and API 31+
  `android:dataExtractionRules` that exclude all app-private data (including
  database, files and preferences) from both cloud backup and device transfer.
  Verify the merged manifest and actual platform backup/data-extraction behavior.
  Only a file the user explicitly exports through SAF is a backup.
- **7.5 — Signing and update-install proof:** compare the certificate fingerprints
  above, then install a prior progress APK and update it with a later APK without
  uninstalling; verify package identity, app launch and seeded fictional database
  history. If update-over-install is impossible, Phase 7 needs an explicit
  owner-approved install/migration procedure that demonstrably preserves database
  history. An unstable or unknown progress signer stops at the owner gate described
  above; do not silently switch to a different key or sign-and-ship path.

**Done means:** every supported v1–v4 upgrade into the current v5 schema is
non-destructive; the complete SAF backup/restore round-trip preserves all rows and
settings; Android OS backup/data-transfer exclusion is verified; P0 passes on the
owner's phone before Phase 8; and CI uploads `app-progress-apk`. Signing continuity
has evidence from at least two progress runs and any accessible main signed
release, with either update-over-install proof or an explicit owner-approved,
data-preserving install/migration procedure. If progress signing is unstable or
still unknown, Phase 7 stops at the owner gate and is not marked Done. The owner
installs the Phase 7 APK and records a short smoke check. No checklist UI, database
version bump or checklist-table migration ships in this phase; the visible label
stays Day Ledger. A later checklist schema increment must add an explicit migration
from v5 and rerun the full supported-source matrix. The full device matrix remains
Phase 15.

### Follow-up queue (owner-directed, after Phase 7 closes)

| # | Item | Scope | Status |
|---|---|---|---|
| F1 | **R2a — suggested default categories** (owner, 2026-10-23): the Categories screen suggests a category per unclassified app from the device's `ApplicationInfo.category` + system-life-app rule; the owner accepts with one tap; existing assignments are never overwritten or re-suggested; ambiguous apps get no suggestion; earning apps are never auto-suggested. No schema change, no new permission, mapping unit-tested with fictional packages. **Delivered 2026-10-23:** `CategorySuggestionRules` (pure) + a "Suggested categories" section with **Use …** / **Not now**; dismissals are stored in `settings` (no schema change); earning apps are identified by package marker *and* by any package the owner has logged earnings against. One interpretation is recorded for correction: "system → Life" is applied to the phone/messaging/system surfaces (dialer, SMS, home, system UI, Settings, contacts), **not** to every preinstalled app, because suggesting life for a preinstalled browser would invite a wrong one-tap accept. **15 new unit tests** (67 in the tree; run `39907405321`, commit `784b92e`, success — an earlier commit message said 12, which was miscounted). | small increment, after Phase 7 | ✅ done |
| F2 | **Per-app `limitMin` has neither a write nor a read path** (inspected 2026-10-23). The Limits screen only toggles always-allowed, and `BlockerAccessibilityService` compares `foregroundMinutes` against the **category** budget (`categoryDao().all().firstOrNull { it.name == category }?.budgetMin`) — the only writes to `limitMin` in the codebase are `0` placeholders in `BlockScreenActivity`'s limit-event rows. So the column is unwired in both directions, and R16's per-app limits are unimplemented rather than merely unpersisted. Implementing it means a write path *and* a precedence rule (app limit versus category budget versus always-allowed versus an active earning session), which is more than one UI field. Placement is an owner decision. | **Owner decision 2026-10-23:** its own dedicated limits increment after Phase 8 closes, with the **per-app limit overriding the category budget** (the category budget then applies only to apps without an explicit limit). Everything else — earning-session and always-allowed precedence, whether a reached limit writes a `limit_events` row — is settled inside that increment. | Phase 4 scope | 📋 planned |
| F3 | **Check in the canonical Room schema** (`app/schemas/…/5.json`) — CI exports and verifies it every run, but the file is not in the tree because artifact downloads are blocked in the agent's environment. One download closes it. | housekeeping | 📋 planned |

### Phase 8 — Checklist core

> 🚧 **Status: code and CI complete (2026-10-23), awaiting the owner install.** Parts
> 8.1–8.3 are implemented and 8.4 is edit-lite; schema v6, idempotent generation, the Today
> checklist front door and the **Daily Logger** label are in. Run `39908874710` is green:
> 86 unit tests, one signer `533D76ED5C2E264779EE005C…`, `room-schema` 2,378 B and
> `app-progress-apk` 18,511,120 B. The schema-export guard that hardcoded `5.json` was
> fixed after an owner gate answer and no longer pins a version. Phase 8 is **not** marked
> ✅: nine Done-means clauses passed on the owner's device, but the *positive* half of
> generation (a habit appearing on its own scheduled day) and tomorrow's non-recurrence
> are still owed. Three UI defects the smoke check caught are fixed, and the Plan screen's
> overflowing template row and its four pre-filled seeded routines were fixed on the
> owner's command (`6738430`). Full record:
> [`docs/phases/08-checklist-core.md`](phases/08-checklist-core.md).

**Parts**

- **8.1 — Model and generation:** add distinct habit rules, one-off tasks, dated
  checklist occurrences and completion logs; schedule generation is idempotent.
- **8.2 — Today checklist:** Now/Next and time-window groups; one-tap Done,
  Minimum, Later, Skip and Undo; completed items collapse.
- **8.3 — Optional evidence:** after ticking, allow minutes/quantity/topic/note or
  no detail; usage evidence may suggest but never silently complete an item.
- **8.4 — Habits, defaults and front door:** generic editable domains/templates,
  weekday and weekly schedules, archive and pause basics; implement the approved
  UI Design v2 and ship the visible label **Daily Logger** with this Phase 8
  checklist front door. Preserve application/package and Room database identity;
  do not seed an owner routine or timetable.

**Done means:** a weekday habit and one-off task create only the correct
occurrences; Done works without a detail form; Minimum is distinct from Full; Undo
works; restart/reboot adds no duplicates; Phase 7 data remains intact after
upgrade; the first screen follows UI Design v2 and displays Daily Logger while
`io.github.pabi277.dayledger` remains unchanged.

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
- **10.3 — Controls:** global Pause with a visible resume time, Less Often,
  separate daily Core/general caps, and silent visibility after audible repeats.
- **10.4 — Reliability health:** restore scheduling after reboot, clock/time-zone
  change, app update and permission re-grant; show when Android timing is approximate.

**Done means:** a Core reminder repeats only within its editable profile, stops on
an action, posts no new alert or sound/vibration during quiet hours, preserves due
state for the next eligible time without a catch-up burst, survives reboot and
creates exactly one log; Flexible reminders remain gentle. Android/OEM limits are
disclosed honestly.

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
