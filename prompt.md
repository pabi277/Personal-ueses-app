<!-- SPDX-License-Identifier: MIT -->

# Day Ledger — continuation prompt

> **Self-contained handoff. Paste this file as the first message of the next chat,
> then give one command** (e.g. answer the remaining plan gates or report a bug).
> This repo is self-contained: `rule.md` holds the full rules, `docs/` the plan,
> the spec and the decisions, THIS FILE the living state. AppFlow is provenance
> only — nothing here depends on it. Update this file at the end of every phase, in
> the same commit as the phase record.

## Identity and product direction

- Technical/current label: **Day Ledger** · proposed visible label **Daily Logger**
  (owner confirmation pending) · package/database identity:
  **`io.github.pabi277.dayledger`** (must remain unchanged) · license: MIT.
- The owner’s 2026-10-22 direction is to evolve this same personal app into a
  whole-life Daily Logger / external brain: optional domains, one-tap completion
  with optional details, and persistent-until-action reminders with quiet hours,
  caps, pause controls and kind copy. No separate checklist app.
- Invariants remain: one user, offline, English-only, no account/backend/ads/
  analytics, no Play Store path, no new permissions without a gate. Starter content
  and tests must be generic/fictional; never commit the owner’s timetable or routine.
- `docs/REQUIREMENTS.md` R1–R34 remains the current checked-in spec. DL1–DL28 are
  summarized in the sanitized [`docs/reference/Daily-Logger-Handoff-for-New-Chat.md`](docs/reference/Daily-Logger-Handoff-for-New-Chat.md)
  and still need to be incorporated before implementation; roadmap references
  alone are not authorization.
- The owner asked that the **full device-validation phase be last**. See
  `docs/ROADMAP.md` Phase 15. The existing P0 pre-feature gate remains mandatory
  before Phase 8; the order change does not waive P0 or migration safety.
- Repository rename/visibility and the display-label choice are separate pending
  gates. Do not change either without explicit approval.
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
  decisions remain closed. The 2026-10-22 Daily Logger direction and full-device-
  phase-last ordering are recorded in `docs/DECISIONS.md`; do not re-ask them.
  Display label, repository visibility/rename, UI choice, expanded time budget and
  use-gate criteria, reminder defaults, and Android OS-backup policy remain pending.
  Owner-local routine details are entered on-device, not asked as a blocker.
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
- After the plan's remaining owner gates are answered, the next implementation
  chat is Phase 7 only. Do not implement checklist features before its backup and
  migration-safety acceptance is met.
- The Phase 10 MVP use-gate is a separate 14-day trial before Phase 11; it does
  not replace the original P0–P3 criteria.
- Current-source audit: Phase 6's backup/restore claim is not demonstrated by the
  tree (`MainActivity.kt` backup callback is empty and its CSV output is a
  placeholder). Phase 7 must close this gap before any schema-changing phase.

- Owner-local setup remains optional and on-device: enabled domains, timetable,
  app lists, reminder windows and routines are not committed or asked as a blocking
  chat questionnaire. The Phase 4 Accessibility path remains optional/fragile;
  checklist use must work with it disabled. `docs/RESEARCH.md` remains the source
  for why in-app feature blocking is out of scope and why automatic usage numbers
  are labelled device-reported.

## How to continue

1. Read `docs/REQUIREMENTS.md`, `docs/ROADMAP.md`, `docs/DECISIONS.md` and the
   latest `docs/phases/` record — trust the repo, not memory. Ask nothing that is
   already recorded.
2. Do not start implementation until the remaining owner gates are answered.
   Then implement **Phase 7 only** (data safety and migration foundation),
   evidence-first. Keep full device validation at Phase 15 and preserve the P0
   pre-feature gate before Phase 8.
3. End each phase with CI green + the run's downloadable `app-progress-apk`, a
   phase record describing what the APK shows, roadmap/prompt updates and a short
   report — then stop at the merge gate. No PR or merge without an explicit command.
