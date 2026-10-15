<!-- SPDX-License-Identifier: MIT -->

# Day Ledger

Bootstrapped from AppFlow @ c8856022ca57cdf23ae78b9491481543db89438d on 2026-10-15

A **personal-use, single-user, offline** Android time-efficiency app built for one
college student's own 24 hours: see how much time really went to which app, get
gentle reminders at the moments he chooses, read the day from a home-screen widget
**without opening the app**, plan today and tomorrow in blocks, put a customizable
time limit on the apps that eat the day, and track the two things that matter to
him — what he earned (offerwall installs at ~₹12.50 each, with real ₹ per hour) and
whether he learned something today.

**Not published.** The owner scoped this app to his own use cases (2026-10-15): no
store listing, no monetization, no accounts, no ads, no analytics. Sideloading the
CI `app-progress-apk` is the normal install path. The full spec is
[`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md).

## What it does

- **Usage insight:** per-app foreground time for today, yesterday, any past day and
  per week — read from the device's own usage statistics, stored on-device, with
  pickups/unlocks and longest session, an hourly heatmap, and week-over-week
  deltas. Owner-editable categories: *Earning*, *Study*, *College*, *Waste*, *Life*.
- **Sweet notifications:** gentle, non-shaming reminders whose wording he can
  rewrite; per-notification on/off, times, repeat days, scope, tone, sound and
  vibration; threshold triggers (a category over budget, total time passed, an app
  used N continuous minutes); quiet hours that are never violated; a daily cap and
  a one-tap "less often" so it never becomes nagware.
- **Home-screen widget:** today's total, top/waste app, next block and its
  countdown, today's ₹, and the learning streak — readable after a screen-on
  without opening the app, with an honest "as of HH:MM" stamp.
- **Planning:** a 24-hour timeline of blocks sized proportional to their duration,
  day templates for *College / Study / Earning / Off* days, per-block exact-time
  reminders, overlap flags, "shift the rest of my day by N minutes" with undo, and
  an evening **Tomorrow plan** reachable from a notification or the widget.
- **Limits and blocking:** per-app and per-category daily limits with a
  customizable block-duration menu (15 min / 30 min / 1 h / until tomorrow /
  until-unlocked), a kind block screen with "extend 10 min once", "skip to next
  block" or a logged hold-and-PIN override, an always-allowed list, and focus
  sessions. **Earning apps are never blocked while an earning session is running.**
- **Earnings and learning:** a two-tap earnings log (ProRewards / OfferPro / other,
  task type, amount, installed app) joined automatically to that app's real
  foreground time to give honest **₹ per hour** per source, plus month-to-date ₹ and
  best-earning hour band; and a daily learning log with a visible streak chain.
- **Review and durability:** a Sunday-night weekly report (planned vs actual, top 3
  time leaks, ₹ and ₹/hour, learning minutes, override count, one suggestion for
  next week), local backup to a file he chooses with restore and CSV export, and a
  weekly backup reminder. History is never deleted, pruned or paywalled.
- **Offline:** no network in the core path — no backend, no account, no sync, no
  analytics, no crash SDK. The only data that leaves the phone is a backup or CSV
  file he explicitly picks.

## Screenshots

[Add current screenshots from the sideloaded build once phase 1 lands. Do not show
features or claims that are not present in the current build.]

## Privacy and ads

- Privacy policy: not applicable while unpublished — no data is collected, and the
  in-app permission disclosure in Settings is the authoritative statement. If this
  app is ever published, a public policy URL becomes a hard requirement first.
- Data Safety: not applicable while unpublished. The inventory it would declare is:
  no ad SDK, no analytics SDK, no crash-reporting SDK, no account, no network calls
  in the core path, all data on-device.
- Ads: **No ads.** No ad SDK is included in any build.
- Ad-free purchase: **Not applicable** — there are no ads to remove, no store
  listing and no monetization of any kind.
- Accounts and backend: **None.** No sign-up, no server, no third-party storage.
- Third parties named in this README (ProRewards, OfferPro) are services the owner
  uses; this app is not affiliated with, endorsed by, or connected to them, and it
  does not read their data automatically. Earnings are numbers he types in.

## Build and test

Requirements: JDK 17, Android SDK, and the checked-in Gradle wrapper. The owner
works phone-first, so **GitHub Actions is the build machine**: every push uploads
the sideloadable `app-progress-apk` debug artifact.

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Signing credentials live in protected Actions secrets, never in this repository.
See `.github/workflows/ci-build.yml`.

## Permissions and data

| Permission | Why this app needs it | What it is actually used for |
|---|---|---|
| `PACKAGE_USAGE_STATS` (special app access, granted in system Settings) | Per-app usage time is the core of the app | Reading foreground duration, pickups and session length **on this device only** |
| `QUERY_ALL_PACKAGES` | Android 11+ hides other apps' names | Showing real app labels/icons in his own usage list |
| `POST_NOTIFICATIONS` (Android 13+) | Reminders must be visible | Only the notifications he configures |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | A block reminder must fire at its start time | His own block reminders and timed unlocks |
| `RECEIVE_BOOT_COMPLETED` | Limits and reminders must survive a reboot | Re-scheduling his own alarms |
| Accessibility Service (**phase 4 only**) | Detecting that a limited app is in the foreground so it can be locked | **Foreground package detection only.** Never reads screen content, never harvests text, never logs keystrokes |
| `SYSTEM_ALERT_WINDOW` (**phase 4 only**) | Drawing the block screen over a limited app | The block screen only |

Explicitly **never** requested: location, contacts, camera, microphone, SMS or call
logs, storage-wide access, `READ_PHONE_STATE`. There is no `INTERNET` use in the
core path. All usage, earnings and learning data stays in an on-device database;
the only export is a file he picks himself.

## Known limits (stated up front, not discovered later)

Full list in [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) §8. In short: usage
access is a manual system grant; Android reports foreground time, not attention, so
every automatic number is labelled device-reported; OEMs delay or kill exact alarms
and usage buckets; widgets cannot refresh continuously; app blocking is the fragile
part and overrides are logged rather than pretended to be impossible; notifications
are dismissible by design — the widget is the real answer; and no app can make him
study.

## License and credits

- **App license:** MIT (owner decision, 2026-10-15). Full text added as `LICENSE` at
  bootstrap, before any app code.
- **Third-party code and assets:** AndroidX, Jetpack Compose, Glance, Room and
  WorkManager (Apache-2.0). Shipped notices appear in the app's About → Open source
  licenses screen; the inventory is recorded in `docs/DECISIONS.md`.

## Repo map

| Path | What it holds |
|---|---|
| `docs/REQUIREMENTS.md` | **the owner's spec** — his own words, a day in his life, R1–R34 with acceptance tests, the insight model, screen sketches, the data model, permissions, hard limits, the personal kill test P0–P3, what is out of scope |
| `rule.md` | this app's own operating manual — laws, merge gate, question policy, work cycle, invariants, definition of done |
| `prompt.md` | the self-contained continuation prompt — paste it into the next chat, then say `start phase N` |
| `docs/ROADMAP.md` | phases 0–6 derived from the requirements, with "Done means" acceptance tests and the `app-progress-apk` |
| `docs/DECISIONS.md` | the anti-re-ask register: owner decisions, law waivers, agent decisions, owner verification tasks |
| `docs/RESEARCH.md` | the market research that shaped the spec — why in-app feature blocking is out, why numbers are labelled device-reported, and the finished dossier for the next app |
| `docs/phases/NN-<slug>.md` | one record per phase: parts, evidence, what shipped, tests, what the APK shows |
| `templates/`, `knowledge/` | frozen snapshot of the pinned playbook — never edited here |

Do not leave bracketed placeholders in a shipped build. Never add signing keys,
service-account JSON, account IDs, or other people's personal data.
