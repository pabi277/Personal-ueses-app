<!-- SPDX-License-Identifier: MIT -->

# Day Ledger

**Current checked-in app label:** Day Ledger

**Application/package and database identity:** `io.github.pabi277.dayledger`

This public repository contains an early, personal-use Android app and its
requirements/roadmap. The CI `app-progress-apk` is a development artifact, not a
release or a guarantee that every documented screen has been verified on a device.

## Current build boundary

The source contains early implementations/screens for device-reported usage,
categories, planning, notifications, limits, learning/earning records, weekly
review and a home-screen widget. Their presence in the code is not evidence that
every flow is complete, reliable on every device or owner-device tested.

The approved future product direction is **Daily Logger**: a checklist-first Today
screen, distinct habit/task/occurrence/completion records, persistent reminders and
the approved Plan/Focus experience. Those Daily Logger checklist features are **not
in the current build**. The visible label change is scheduled for Phase 8; until
then the installed app remains labelled Day Ledger. Do not change the package or
database identity.

Known current gaps, verified from the source:

- The database is Room version 5 with `exportSchema = false` and
  `fallbackToDestructiveMigration()`; explicit non-destructive upgrade paths have
  not yet been added.
- The Settings backup action is not wired to an implementation, and the current
  CSV output includes a placeholder row. Do not treat either action as a verified
  backup or restore.
- The manifest does not yet implement or verify the approved exclusion of
  app-private data from Android cloud backup/device transfer.
- Progress-APK signing continuity and update-over-install have not been verified.
  No stable signing identity is claimed.
- The original P0 usage check on the owner's device remains required before Phase 8.

Phase 7 is planned to close the migration, explicit SAF backup/restore, Android OS
backup-exclusion, signing and install-safety gaps. It is a documentation/plan item
until separately authorized; it does not add checklist features or rename the
current app.

## Build and test

Requirements: JDK 17, Android SDK and the checked-in Gradle wrapper.

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

GitHub Actions builds a debug `app-progress-apk` on pushes and pull requests. It is
for progress review/sideloading, not a signed release artifact. Signing continuity
must be established before promising an update install over an existing app.

## Privacy and permissions

The current manifest has no `INTERNET` permission and declares local usage,
notification, alarm, boot, package-visibility and accessibility-related
capabilities. Usage access and blocking capabilities are sensitive and require
clear user-facing explanations. The app is intended to keep data local.

**Android OS backup exclusion is approved but not implemented/verified yet.** Do
not assume app-private data is excluded from Android backup or device transfer.
The target policy is to exclude it; the only backup should be a file the user
explicitly exports through the app's Storage Access Framework (SAF) flow once the
Phase 7 implementation is complete and verified.

## Future product plan

The owner-approved navigation is **Today · Plan · Habits · Review · More**. Today
is the checklist command center; Plan/Tomorrow is the timeline; Focus is One Thing.
Usage evidence supports the checklist but never silently marks an item complete.
Usage, Earn, Learn, Limits, Categories, Notifications and Settings remain
reachable. See [`docs/UI_DESIGN.md`](docs/UI_DESIGN.md) and
[`docs/ROADMAP.md`](docs/ROADMAP.md). The historical four-week statement applies
to Phases 0–4 only; there is no fixed calendar deadline for proposed Phases 7–15.

## Repository map

| Path | What it contains |
|---|---|
| [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) | Existing R1–R34 and P0–P3, plus the additive DL1–DL28 Daily Logger requirements and acceptance tests |
| [`docs/UI_DESIGN.md`](docs/UI_DESIGN.md) | Approved UI Design v2; future target, not the current APK |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Historical Phases 0–6 and proposed Phases 7–15 with readiness gates |
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | Owner decisions and gate status; read before asking or implementing |
| [`docs/reference/Daily-Logger-Handoff-for-New-Chat.md`](docs/reference/Daily-Logger-Handoff-for-New-Chat.md) | Sanitized product and continuation reference |
| [`prompt.md`](prompt.md) | Living continuation prompt |
| `rule.md` | Repository workflow, phase and merge-gate rules |
| `docs/phases/` | Phase records and evidence |

All new examples, tests and starter content must be generic and fictional. Never
commit signing secrets, personal records or private profile data.
