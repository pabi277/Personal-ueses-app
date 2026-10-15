# Roadmap — Day Ledger (`io.github.pabi277.dayledger`)

Bootstrapped from AppFlow @ c8856022ca57cdf23ae78b9491481543db89438d on 2026-10-15

> One phase = one chat (`rule.md` §4). A phase is a decent chunk: one whole
> capability, 3–4 parts listed in its `docs/phases/NN-<slug>.md` record. Status:
> 📋 planned · 🚧 in progress · ✅ done · ❌ cancelled (keep the row). "Done
> means" always includes a green CI run whose **`app-progress-apk` artifact the
> owner can sideload and see the phase's increment** — sideloading is the normal
> install path for this personal-use app.
>
> **This roadmap is derived from `docs/REQUIREMENTS.md`** (owner spec, 2026-10-15)
> and replaces the earlier publishing-oriented roadmap. Requirement IDs (R1…)
> point back to it. Personal-use kill test P0–P3 lives in `docs/REQUIREMENTS.md` §9.
> Time budget: **4 weeks, law 3 kept** (owner, 2026-10-15) — covering phases 0–4;
> phases 5–6 follow immediately after and are the "beyond four weeks" tail the
> owner's scope decision already placed outside the clock.

| # | Phase (parts) | Done means | Status |
|---|---|---|---|
| 0 | **Bootstrap + hello** — 0.1 freeze `templates/` + `knowledge/` from the pinned SHA with the README provenance line; 0.2 commit these plan docs (`RESEARCH`, `REQUIREMENTS`, `ROADMAP`, `DECISIONS`), `rule.md`, `prompt.md`, MIT `LICENSE`; 0.3 minimal Gradle/AGP skeleton (Kotlin, Compose, Room, targetSdk 36, minSdk 26), one hello screen, one unit test; 0.4 `ci-build.yml` from the template, signing by secret **name** only, `app-progress-apk` debug artifact on every push | Snapshot + plan docs + LICENSE + README committed before any app code; CI green with a signed release artifact **and** the downloadable `app-progress-apk`; the APK installs on the owner's phone and opens on a hello screen showing "Day Ledger" | ✅ |
| 1 | **Usage insight** (R1–R7, R31 permission half) — 1.1 permission onboarding: walk the owner to Usage access + notifications, show honestly what was granted; 1.2 ingestion from `UsageStatsManager` (per-app foreground minutes, pickups, longest session) into Room, batched, offline, labelled device-reported; 1.3 **Today** screen: total, per-app list, category donut, hourly heatmap strip, "as of HH:MM"; 1.4 owner-editable categories (Earning / Study / College / Waste / Life) + per-category and per-app daily budgets (ceiling **and** floor) | Real per-app time for today and the past 7 days renders offline and matches the system's own Digital Wellbeing numbers within ~10% on the owner's device (**kill test P0**); assigning ProRewards to *Earning* and Instagram to *Waste* changes the chart and the budget bars immediately; APK shows the populated Today screen and the category editor | ✅ |
| 2 | **Sweet notifications** (R9–R12) — 2.1 notification engine with warm, non-shaming templates and live-preview wording editor (all text in resources, user-editable); 2.2 full customization: per-notification on/off, times, repeat days, scope (apps/categories), tone, sound/vibration, **quiet hours** that are never violated; 2.3 threshold triggers (category over budget, total passed N, app in foreground N continuous minutes) with exact alarms that survive reboot and doze; 2.4 anti-nag discipline: daily cap (default 6), de-duplication, one-tap "less often" on every notification | A 21:00 nudge reads e.g. *"Today: 5h 40m 🌙 Waste 52m over your 45m"* and the owner can rewrite that sentence; quiet hours verified silent over one night; crossing the Waste budget fires exactly one nudge; a heavy day produces ≤ the cap; APK shows the notification list, the wording editor and a fired nudge | ✅ |
| 3 | **Widget + planning** (R13–R15, R20–R23) — 3.1 Glance home-screen widget: today's total, top/waste app, next block + countdown, today's ₹, study streak, "as of" stamp, dark/light aware, resizable; 3.2 widget tap targets + long-press field configuration, refresh on screen-on and via periodic worker; 3.3 24-hour timeline of proportional blocks with add/edit/move/resize and an explicit overlap indicator; 3.4 **Tomorrow plan** evening screen (template pre-fill, three-tap adjust, reachable from a notification action and a widget tap) + day templates (*College / Study / Earning / Off*, last-used sorting, multi-day apply) + per-block exact-time reminders | The widget shows live-ish numbers after a screen-on **without opening the app**; the 22:00 nudge opens Tomorrow directly and three taps save a plan that becomes today at midnight; a 19:00 study block fires with the screen off; overlapping blocks are visibly flagged; APK shows the widget, the timeline, the template picker and Tomorrow | ✅ |
| 4 | **Limits & blocking** (R16–R19) — 4.1 per-app/per-category daily limits with an owner-editable **block-duration menu** (15 min / 30 min / 1 h / until tomorrow / until-I-unlock); 4.2 Accessibility-service + overlay block screen that is kind, not punitive (what was used, what the plan says, "extend 10 min once", "skip to next block", "unlock anyway" with hold + PIN); 4.3 every override logged and surfaced in the weekly report — no silent cheating; 4.4 **always-allowed list** (dialer, SMS, UPI/banking, maps, camera, college apps, and earning apps while a session runs) + focus session ("study 50 min") with widget countdown | Hitting the Waste ceiling locks the chosen apps; picking "30 min" unlocks exactly 30 minutes later with no further taps; **ProRewards can never be locked by a Waste limit while an earning session runs** (unit-tested); "unlock anyway" needs a 5-second hold + PIN and appears in the report; a 50-min focus session survives the screen turning off. **Fallback recorded now:** if Accessibility/overlay proves unworkable on the owner's device, phase 4 ships limits as notification-only nudges and phases 5–6 proceed unchanged — the app is still complete and useful | 📋 |
| 5 | **Earnings + learning** (R25–R28) — 5.1 two-tap earning log (source: ProRewards / OfferPro / other; task type; amount defaulting to ₹12.50 per install, editable; installed app); 5.2 auto-time join: read the installed app's foreground minutes from phase-1 data and compute **real ₹ per hour** per source and task; 5.3 earnings insight: today's ₹, month-to-date ₹, run-rate, best ₹/hour band, flag when a source falls below his study value — **no income promise anywhere**; 5.4 daily learning tracker: minutes + free text/checklist + streak chain with visible gaps, surfaced on the widget | A ₹12.5 install that took 24 minutes of foreground time is reported as ₹31.25/h and the weekly ranking orders sources by ₹/hour; a day with zero learning shows a gap in the chain and logging one item restores it; APK shows the earning log, the ₹/hour ranking and the streak | 📋 |
| 6 | **Review + durability** (R6, R24, R29–R31, R33) — 6.1 weekly report (Sunday night): planned vs actual by category, top 3 leaks, unlocks, ₹ and ₹/hour, learning minutes, override count, best/worst day, **one** suggestion for next week; 6.2 week-vs-week deltas and "shift the rest of my day by N minutes" with undo; 6.3 local backup to an owner-chosen file (SAF) + restore with schema versioning + CSV export + weekly backup reminder; 6.4 battery/perf audit (batched polling, WorkManager, no needless foreground service), theme + large-font pass, and a Settings screen showing the app's own battery cost and notification count | Clear app data → restore from the backup file → lose nothing; CSV opens correctly in a spreadsheet; every report number is recomputable from raw rows (unit-tested); one normal day adds no visible battery drain versus the day before (owner-observed and recorded); APK shows the weekly report, backup/restore round trip and the Settings audit | 📋 |

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
