<!-- SPDX-License-Identifier: MIT -->

# Day Ledger — continuation prompt

> **Self-contained handoff. Paste this file as the first message of the next chat,
> then give one command** (e.g. start the separately authorized phase or report a
> bug; do not re-ask the closed owner gates).
> This repo is self-contained: `rule.md` holds the full rules, `docs/` the plan,
> the spec and the decisions, THIS FILE the living state. AppFlow is provenance
> only — nothing here depends on it. Update this file at the end of every phase, in
> the same commit as the phase record.

## Identity and product direction

- **Current visible label:** Day Ledger. **Approved future visible label:** Daily
  Logger, introduced with the Phase 8 checklist front door; the current APK is not
  renamed. Package/database identity stays **`io.github.pabi277.dayledger`** and
  license stays MIT.
- Evolve this same app into the owner-approved Daily Logger / external brain, not
  a separate checklist app. The approved UI hybrid is Today = Command Center
  Checklist, Plan/Tomorrow = timeline, Focus = One Thing; primary navigation is
  Today · Plan · Habits · Review · More. Usage is supporting Phone evidence, not
  the Today hero. Existing Usage, Earn, Learn, Limits, Categories, Notifications
  and Settings remain reachable.
- Reminder defaults are approved and editable: Core every 20 minutes per
  occurrence, max four sound/vibration fires per occurrence subject to a separate
  daily Core cap, then silent ongoing/widget visibility; Standard initial +
  one repeat after 45–60 minutes; Flexible one gentle reminder or digest. Quiet
  hours are no-fire; global Pause preserves due state until resume; Less Often is
  scoped; separate daily Core/general checklist caps do not reuse R12's usage cap.
- Android OS cloud backup and device-to-device transfer must exclude the database
  and all app-private data. The only backup is an explicitly user-exported SAF
  file. Phase 7 must implement and verify the Android backup/data-extraction rules;
  this is not the current build's behavior.
- Invariants remain: one user, offline, English-only, no account/backend/ads/
  analytics, no Play Store path, no new permissions without a gate. Starter content,
  examples and tests must be generic/fictional; never commit owner timetable,
  routines, profile or financial details.
- `docs/REQUIREMENTS.md` preserves R1–R34 and P0–P3 and now contains DL1–DL28 with
  priorities, R-ID mappings and acceptance tests. Read it with the approved
  [`docs/UI_DESIGN.md`](docs/UI_DESIGN.md) and `docs/ROADMAP.md` before work.
- The repository remains **public** under its current name; no repository rename
  is authorized. No fixed calendar deadline applies to proposed Phases 7–15; the
  historical four-week statement applies only to Phases 0–4. MVP is Phases 7–10,
  then a 14-day real-use gate before Phase 11. Work one owner-approved phase per
  chat.
- The full device matrix remains Phase 15. P0 still gates Phase 8. Every
  implementation phase also requires an owner APK install and a short recorded
  smoke check. This documentation closure does not start Phase 7.
- Signing continuity remains **unknown**: progress artifacts for runs
  `39766581703` and `39766005737` could not be downloaded in this environment;
  the main signed-release attempt `38845505942` failed during signing-key setup
  and no signed-release artifact was found. Do not promise update-over-install or
  rotate signing until evidence is available; see the Phase 7 owner gate in the
  roadmap. Secret names only; never copy values.
- Provenance: AppFlow @
  `c8856022ca57cdf23ae78b9491481543db89438d` (reference only — this repo is
  self-contained).
- Git author: designated in `docs/DECISIONS.md` · session branch: use the branch
  assigned by Arena; never create/switch branches or push `main`.

## Rules digest (full rules: `./rule.md` in THIS repo — self-contained)

- Work one **phase** per chat (a phase = 3–4 parts, one whole capability). No work
  outside the current phase's "Done means" and its R-IDs.
- Never create/switch branches, push `main`, force-push, or `reset --hard`.
- No PR, merge, release, publish or deploy without my explicit command in chat.
  Green CI is not authorization.
- **Ask less, gate always** (`rule.md` §3): GATE items are asked once in a
  Summary batch. The 2026-10-15 language, no-ads, personal-use and package
  decisions remain closed. The 2026-10-22 label timing, UI, reminder defaults,
  OS-backup exclusion, public repository/no-rename decision, no-fixed-deadline
  scope and Phase 15 device ordering are answered in `docs/DECISIONS.md`; do not
  re-ask them. Agree measurable criteria for the 14-day post-Phase-10 use gate
  before that trial begins. Owner-local routine details are entered on-device, not
  asked as a blocker.
- Invariants that are easy to break by accident: the **fixed permission set**
  (`PACKAGE_USAGE_STATS`, `QUERY_ALL_PACKAGES`, `POST_NOTIFICATIONS`, exact alarms,
  boot, and Accessibility + overlay **for phase-4 blocking only**) — nothing else,
  ever; Accessibility detects the foreground package and nothing more; **never block
  earning apps or the always-allowed list**; every automatic number labelled
  *device-reported foreground time* with an "as of HH:MM" stamp; daily notification
  cap + quiet hours + one-tap "less often"; history never deleted, pruned or
  paywalled; no ads, no income or behaviour-change claims; offline single-user with
  no analytics SDK and no `INTERNET` in the core path; integer minutes and integer
  paise; the personal kill test **P0–P3** (`docs/REQUIREMENTS.md` §9) is never
  rewritten after results.
- Evidence before hypothesis (`file:line`). CI green required, with the downloadable
  `app-progress-apk` artifact uploaded — the owner sideloads it to see the phase.
  Docs ride with the code commit: phase record, roadmap status, `prompt.md`.

## State (update at every phase end)

- **Phase 0 ✅ — Bootstrap + hello** (2026-10-15). Snapshot + plan docs + MIT
  LICENSE + Gradle/AGP skeleton + CI green; APK opens on a "Day Ledger" hello
  screen. See `docs/phases/00-bootstrap-hello.md`.
- CI run: 38748908795 green · APK: `app-progress-apk` (9.2 MB) shows the hello
  screen.
- Decisions digest: **personal use only, publishing cancelled** · English-only
  (`en`, `en-IN`) · **no ads, no ad SDK** · 4-week budget, **law 3 kept, no
  waiver**, covering phases 0–4 · **MIT** · git author = the owner's GitHub
  noreply · per-app usage tracking is **in**. **Never re-ask any of these.**
- **Phase 1 ✅ — Usage insight** (2026-10-15). Permission onboarding,
  UsageStats ingestion into Room, Today screen, categories + budgets. CI run
  38752868739 green; APK `app-progress-apk` shows Today + Categories.
  See `docs/phases/01-usage-insight.md`.
- **Phase 2 ✅ — Sweet notifications** (2026-10-15). Engine, customization, thresholds, anti-nag. CI run 38753510344 green.
  See `docs/phases/02-sweet-notifications.md`.
- **Phase 3 ✅ — Widget + planning** (2026-10-15). Glance widget, timeline, templates, Tomorrow. CI run 38753962790 green.
  See `docs/phases/03-widget-planning.md`.
- **Phase 4 ✅ — Limits & blocking** (2026-10-15). Accessibility service, block screen, always-allowed. CI run 38755465576 green.
  See `docs/phases/04-limits-blocking.md`.
- **Phase 5 ✅ — Earnings + learning** (2026-10-15). Earning log, ₹/hour, learning streak. CI run 38755944663 green.
  See `docs/phases/05-earnings-learning.md`.
- **Phase 6 ✅ — Review + durability** (2026-10-15). Weekly report, shift-my-day, CSV export, settings. CI run 38756961555 green.
  See `docs/phases/06-review-durability.md`.
- The 2026-10-22 phase plan places the full device-validation phase last
  (Phase 15). Phase 7 is data-safety/migration readiness; the existing P0
  real-device usage check remains a hard pre-feature gate before Phase 8.
- The 2026-10-22 product and delivery gates are closed, and PR #11 was merged
  into `main` at `8070713`. Phase 7 is owner-authorised as of 2026-10-22 but
  **stopped at its signing gate** before any implementation code; see
  `docs/phases/07-data-safety-migration.md` and the ROADMAP status banner. Do not
  implement checklist features before Phase 7's migration, backup and
  signing/install acceptance is met.
- The Phase 10 MVP use-gate is a separate 14-day real-use trial before Phase 11;
  agree measurable criteria before that trial. It does not replace original P0–P3.
- Phase 7 closed the Phase 6 durability gap in code (2026-10-22): the Settings
  backup writes a versioned JSON file covering every table, restore validates and
  runs in one transaction, the CSV exports real rows, Room is v5 with
  `exportSchema = true` and explicit `1→2→3→4→5` migrations and **no** destructive
  fallback, and the manifest excludes all app-private data from OS backup and
  device transfer. These are CI-proven; **device proof is still owed** — do not
  describe them as verified on the owner's phone. Database version remains 5; any
  later schema bump must add the next explicit migration and rerun the full
  v1–v5 matrix before shipping.
- Signing status (2026-10-22, Phase 7 gate): the gate failed on evidence — the
  progress APK was signed by an AGP-generated debug key recreated on every runner
  (no keystore restore, no `signingConfigs`), and `main` run `39771615912` failed
  with `Missing required Actions secret: ANDROID_KEYSTORE_BASE64` (only that name
  is proven absent; the loop exits at the first empty one). Artifact downloads are
  still blocked here (`*.blob.core.windows.net`), so no fingerprint is known. The
  owner answered the gate: data is **valuable** and one owner-controlled identity
  is **approved**. Round 2 therefore wired the plumbing only — an `owner`
  `signingConfigs` entry used by **both** debug and release, gated on
  `ANDROID_KEYSTORE_PATH` plus the four secret names, a runner keystore restore in
  both jobs, and an `apksigner` SHA-256 certificate fingerprint written to the log
  and the artifact (fingerprints are public; keystore values are not). No Phase 7
  feature code was written: with valuable installed data, the first owner-key
  install needs one uninstall, so an ADB `run-as` database rescue must be verified
  first (`docs/phases/07-data-safety-migration.md` §6b). Update-over-install is
  **not** yet proven, and the two-run fingerprint comparison is still owed.

- Owner-local setup remains optional and on-device: enabled domains, timetable,
  app lists, reminder windows and routines are not committed or asked as a blocking
  chat questionnaire. The Phase 4 Accessibility path remains optional/fragile;
  checklist use must work with it disabled. `docs/RESEARCH.md` remains the source
  for why in-app feature blocking is out of scope and why automatic usage numbers
  are labelled device-reported.

## How to continue

1. Read `docs/REQUIREMENTS.md`, `docs/ROADMAP.md`, `docs/DECISIONS.md`,
   `docs/UI_DESIGN.md` and the latest `docs/phases/` record — trust the repo, not
   memory. Ask nothing that is already recorded.
2. Phase 7 is **in progress**: parts **7.1–7.4 are implemented and CI-verified**
   (run `39777555069`, 26 tests green — schema export, non-destructive
   v1→v5 migrations, versioned SAF backup/restore, OS-backup exclusion), and
   **nothing has been installed on the owner's phone**. Remaining, owner-only:
   (a) create the keystore outside the repo and add the four secret **names**
   (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
   `ANDROID_KEY_PASSWORD`; values his alone, never asked for in chat);
   (b) complete the database rescue (`docs/phases/07-data-safety-migration.md`
   §6b/§6c — PC+USB or Termux wireless ADB, since the owner has one phone and no
   second device); (c) one uninstall + install of an owner-key APK; then part 7.5
   (update-over-install proof) and the device smoke check in §10 of that record.
   The owner chose **option A**: build everything CI can prove, install nothing
   until a rescue path exists. Keep the full device matrix at Phase 15 and
   preserve P0 before Phase 8.
3. End each implementation phase with CI green and its `app-progress-apk`; the
   owner installs it and records a short smoke check plus the phase-specific
   evidence in the phase record. Update roadmap/prompt and stop at the merge gate.
   No PR or merge without an explicit command.
