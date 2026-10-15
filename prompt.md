<!-- SPDX-License-Identifier: MIT -->

# Day Ledger — continuation prompt

> **Self-contained handoff. Paste this file as the first message of the next chat,
> then give one command** (e.g. `start phase 0` or a bug report).
> This repo is self-contained: `rule.md` holds the full rules, `docs/` the plan,
> the spec and the decisions, THIS FILE the living state. AppFlow is provenance
> only — nothing here depends on it. Update this file at the end of every phase, in
> the same commit as the phase record.

## Identity (fixed)

- App: **Day Ledger** (working name) · package: **`io.github.pabi277.dayledger`**
  (permanent once published) · license: **MIT** (owner, 2026-10-15)
- What it is: a **personal-use, single-user, offline, English-only**
  time-efficiency app for the owner's own day — per-app usage insight
  (device-reported), sweet customizable notifications, a home-screen widget
  readable **without opening the app**, planned 24-hour blocks with a **Tomorrow
  plan**, app limits with **customizable block durations**, an offerwall earnings
  log (~₹12.50 per install) with real ₹/hour attribution, and a daily learning
  streak. No account, no backend, no ads, no analytics, no store listing.
- **The spec is `docs/REQUIREMENTS.md`** (the owner's own words, his day-in-the-life,
  and R1–R34 with acceptance tests). Read it before proposing any feature; nothing
  is built that is not in it.
- Official Play title: Not set — and not applicable: publishing was cancelled by
  the owner on 2026-10-15.
- Provenance: bootstrapped from AppFlow @
  `c8856022ca57cdf23ae78b9491481543db89438d` (reference only — this repo is
  self-contained).
- Git author: designated in `docs/DECISIONS.md` · session branch: arena/be850e43-personal-ueses-app ·
  never push `main`.

## Rules digest (full rules: `./rule.md` in THIS repo — self-contained)

- Work one **phase** per chat (a phase = 3–4 parts, one whole capability). No work
  outside the current phase's "Done means" and its R-IDs.
- Never create/switch branches, push `main`, force-push, or `reset --hard`.
- No PR, merge, release, publish or deploy without my explicit command in chat.
  Green CI is not authorization.
- **Ask less, gate always** (`rule.md` §3): GATE items are asked once, in one
  Summary batch. DECIDE-ONCE answers live in `docs/DECISIONS.md` and are **never
  re-asked** — direction (personal-use time app), English-only, no ads, 4 weeks and
  the core-first scope cut were all answered on 2026-10-15. The six owner-only
  inputs in `docs/REQUIREMENTS.md` §12 are **not** a question batch: use the
  documented default, log it as reversible, let the owner correct it any time.
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
- Next: **phase 4 — Limits & blocking** (R16–R19).

- Open risks / owner items: `docs/REQUIREMENTS.md` §12 lists six things only the
  owner can supply (his earning / waste / never-block app list, his real ceilings
  and floors, college timetable, notification tone + quiet hours, device make/model
  + Android version, whether ₹12.50 is the only rate) — documented defaults are used
  until he answers, each logged as reversible. Phase 4 (Accessibility + overlay
  blocking) is the fragile one; its notification-only fallback is already recorded
  in `docs/ROADMAP.md`. `docs/RESEARCH.md` stays on file: it is why in-app feature
  blocking is out of scope and why every number is labelled device-reported.

## How to continue

1. Read `docs/REQUIREMENTS.md`, `docs/ROADMAP.md`, `docs/DECISIONS.md` and the
   latest `docs/phases/` record — trust the repo, not memory. Ask nothing that is
   already recorded.
2. Implement **phase 0**, all four parts, evidence-first. Then one phase per chat,
   in order, each ending in a sideloadable increment the owner can see on his own
   phone.
3. End with: CI green + the run's downloadable `app-progress-apk` artifact ·
   `docs/phases/00-bootstrap-hello.md` (say what the APK shows) · roadmap row ✅ ·
   this file updated · short report (run id, tip sha, APK, what is now visible) ·
   stop at the merge gate.
