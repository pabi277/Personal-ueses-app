# Decisions — Day Ledger

> The anti-re-ask register (`rule.md` §3). Every question is answered here ONCE.
> Later chats read this file before asking anything. **Re-asking a recorded
> answer is a defect, not caution.** Changing an owner answer is a GATE.
> Provenance: AppFlow @ `c8856022ca57cdf23ae78b9491481543db89438d`.

## Owner decisions

- 2026-10-15 — **Direction?** — A **personal 24-hour time-management app built to
  the owner's own requirements**, not the researched mess-ledger option. Owner's
  words: *"i am now playing to create a personal app for use my 24 hour
  effectively all screen time app is good but not very much suitable for my use
  so i thinking if i build a app with my recommendations i can manage my time
  effectively."* Recorded as the chosen direction; the mess-hisab dossier stays
  in `docs/RESEARCH.md` as the designated next app.
- 2026-10-15 — **Languages / locales?** — **English only** (`en`, `en-IN`
  default). No Bengali in v1. Strings are still kept in resources so a later
  locale stays cheap.
- 2026-10-15 — **Monetization model?** — **Paid upfront, no ads.** No ad SDK of
  any kind ships in v1. Consequences recorded in `docs/RESEARCH.md`
  (Monetization) and reflected in kill test K1.
- 2026-10-15 — **Time budget?** — **4 weeks, RULES.md law 3 kept — no waiver.**
- 2026-10-15 — **Scope cut that protects the budget?** — Ship the ledger core
  (plan → capture → review → durability) inside the four weeks; **monetization
  wiring, store listing and release prep are sequenced after it** (phases 5–6),
  outside the four-week build clock. Owner selected the "4 weeks — ship core,
  defer monetization to the next phase" option.

### Owner update — gate answers (2026-10-22)

- The owner approved evolving this **existing app** into the Daily Logger / external
  brain, not creating a separate checklist app. Keep it single-user, offline and
  English-only, with no account, backend, ads or analytics. Package and database
  identity remain `io.github.pabi277.dayledger`; all new examples, tests and
  starter content remain generic and fictional.
- **Visible label:** “Daily Logger” is approved, but ships with the Phase 8
  checklist front door. Until Phase 8, the current installed label remains “Day
  Ledger.” Do not rename the package or database.
- **UI:** the approved hybrid is Today = Command Center Checklist, Plan/Tomorrow =
  timeline, Focus = One Thing. Primary navigation is Today · Plan · Habits ·
  Review · More. Usage, Earn, Learn, Limits, Categories, Notifications and
  Settings remain reachable; usage evidence supports Today and is not its hero.
- **Android backup:** exclude the app database and all app-private Daily Logger
  data from Android OS cloud backup and device-to-device transfer. The only backup
  is a file explicitly exported by the user through SAF. Phase 7 must implement
  and verify `android:allowBackup="false"`, applicable legacy full-backup rules
  and API 31+ data-extraction rules; this is not yet implemented in the current
  build.
- **Reminder defaults:** Core every 20 minutes per occurrence, at most four
  sound/vibration fires for that occurrence subject to its separate daily Core cap,
  then silent ongoing/widget visibility until action or day close; Standard initial
  plus one repeat after 45–60 minutes; Flexible one gentle reminder or digest.
  Defaults are editable per item/domain/global. Quiet hours are a no-fire window;
  global Pause suppresses checklist reminders through the selected resume time
  while preserving due state; Less Often is scoped to the selected trigger/profile.
  Separate daily Core/general checklist caps are mandatory and distinct from the
  existing R12 usage-reminder cap.
- **Repository:** remain PUBLIC and keep its existing name. Do not rename the
  repository in this task. Committed documentation/examples/fixtures must remain
  generic and sanitized; do not restore owner profile, timetable, routine or
  financial details from an unsanitized handoff.
- **Time/scope:** no fixed calendar deadline for proposed Phases 7–15. The
  historical four-week statement applies to Phases 0–4 only. MVP is Phases 7–10,
  followed by a 14-day real-use gate before Phase 11. Work one owner-approved
  phase per chat; define measurable trial criteria before starting that gate.
- **Device order:** the full device matrix remains Phase 15; P0 remains required
  before Phase 8. Every implementation phase also requires an owner APK install
  and a short recorded smoke check. This documentation-only closure does not
  authorize Phase 7 implementation or a Phase 8 label change.
- **Signing evidence:** two progress APK runs and the main signed-release attempt
  were audited at metadata level, but the artifact downloads were not accessible
  in this environment, so certificate fingerprints/update-install compatibility
  remain unknown. Phase 7 must stop at an owner gate if continuity is unstable or
  cannot be established; use GitHub Actions secret names only in any plan, never
  secret values.

### Plan revision (owner, 2026-10-15) — the gate answer "revise", now specified

- 2026-10-15 — **Who is this app for?** — **The owner only.** His words: *"I want
  the app for only my use cases."* Consequence recorded: **publishing is
  cancelled** — no store listing, no closed test, no release prep, no
  monetization wiring. The install path is sideloading the CI
  `app-progress-apk`. The earlier "paid upfront, no ads" answer is **moot** with no
  listing; the **no-ads / no-analytics / no-account** invariants stay because
  nothing revoked them. Publishing may be reconsidered only as a fresh decision
  with a fresh plan after kill test P1–P3 passes.
- 2026-10-15 — **Owner context (the `[MY CONTEXT]` that arrived blank).** College
  student. Earns a little through **ProRewards** and the **OfferPro** offerwall /
  unlimited-coin earn games: **≈ ₹12.5 per app install**. Self-imposed rule:
  **learn something every day**. Finds existing screen-time apps "good but not
  very much suitable" for his use, and wants one built to his own requirements.
- 2026-10-15 — **Core of the app (owner's own priority order).** (1) see **how
  much time was spent on which app**; (2) **sweet notifications** reminding him how
  much time he has spent on the phone — called *"the main thing"*; (3) a
  **home-screen widget** so the truth is visible **even without opening the app**;
  (4) **customizable notification options**; (5) **customizable time duration for
  blocking an app**; (6) **tomorrow plan**; (7) *"other features that are needed
  for a time efficient app"* — delegated to the agent and answered as R4–R8,
  R19, R24, R29–R34 in `docs/REQUIREMENTS.md`, each with its acceptance test.
- 2026-10-15 — **Per-app usage tracking is IN.** The owner explicitly asked to see
  time per app, which requires `PACKAGE_USAGE_STATS` (plus `QUERY_ALL_PACKAGES` for
  labels) and, for blocking, an Accessibility service + overlay. This **reverses
  the agent decision of the same date** that kept sensitive permissions out of v1
  (that decision was reversible and is now reversed by the owner — recorded, not
  re-asked). Because the app is not published, Play's sensitive-permission review
  does not apply; if publishing is ever revisited, that review becomes a live
  blocker and is re-read that day.
- 2026-10-15 — **Kill test replaced.** The store-based test K0–K3 in
  `docs/RESEARCH.md` is **void** (no store). It is replaced by the **personal-use
  test P0–P3** in `docs/REQUIREMENTS.md` §9, written on the same day and before any
  code existed: P0 buildability (real per-app time on his own phone within ~10% of
  the system's number), P1 21 consecutive days of honest use, P2 a ≥10% improvement
  in one of Waste↓ / Study↑ / ₹-per-hour↑ between week 1 and week 4 **and** still
  wanting it on the home screen on day 21, P3 no battery regression and the
  notification cap respected every day. **P0 gates all later phases.** Never
  rewritten after results.

### Merge authorization (owner, 2026-10-15)

- 2026-10-15 — **Merge to `main`?** — **YES, explicitly commanded** ("Ok merge it
  to main"), executed as a **fast-forward** of `main` from `16c6ad3` to the plan
  commit `c29fefc16c7508edc64c10450ee41c9d79c5c1a7` in THIS repository
  (`pabi277/Personal-ueses-app`). No pull request was created, because the command
  was to merge, not to open a PR. The session branch is unchanged and remains the
  working branch.
- 2026-10-15 — **What this merge does NOT authorize.** It merges the *plan files*
  only. The plan itself is still **unapproved** (gate item 1 = "revise", followed
  by the owner's rescoping to personal use), so **the Day Ledger app repository
  was not created** and no app code, `LICENSE`, playbook snapshot or CI exists. The
  recorded repo authorization (public + MIT), git author identity and
  name/package stay in force for the moment the owner approves.
- 2026-10-15 — **Starting prompt delivered in chat, not as a branch file** — the
  owner asked for the app's starting prompt to be written in the conversation
  rather than added to the branch. `plan/files/prompt.md` remains the future app
  repo's continuation prompt; the chat text is the stage-2 kickoff for the new
  repository.

### Exit gate (batch B, asked 2026-10-15) — answers

- 2026-10-15 — **Plan: approve / revise / hold?** — **REVISE.** The owner chose
  "revise first" without yet naming what to change. **Consequence: the plan is
  not approved, so stage 2 does not start.** No repository, branch, commit, app
  code or Play/account action is created until the owner sends the revision
  list and then approves the revised plan. Re-answering this item after a
  revision is the same gate, not a second batch.
- 2026-10-15 — **Repository creation authorized?** — **Yes, conditionally:
  PUBLIC repository, MIT license.** This authorization is recorded but **not
  acted on**, because the plan itself is still at "revise". It is executed only
  after the plan is approved; if the revision changes the product materially,
  the owner is asked whether this authorization still stands (changing a
  recorded answer is a GATE).
- 2026-10-15 — **License?** — **MIT** (owner, at the same gate). Full text added
  as `LICENSE` at bootstrap, before any app code (law 7).
- 2026-10-15 — **Git author identity designated for THIS repo?** —
  `204871773+pabi277@users.noreply.github.com` (the owner's GitHub noreply for
  `pabi277`), chosen explicitly so no personal email enters public Git metadata.
  Committer identity stays the owner-approved GitHub noreply/bot identity.
  AppFlow's own approved author addresses are **not** inherited by this repo
  (AppFlow `START.md`: never copy them into a new app repository) and are not
  repeated anywhere in these files. No commit is made before the plan is
  approved.
- 2026-10-15 — **Working name + package ID?** — Working name **Day Ledger**;
  package ID **`io.github.pabi277.dayledger`** — permanent once published, so it
  is fixed now and must not be changed after the first Play upload. The official
  Google Play title stays `Not set` and is recorded in AppFlow's `APP_LIST.md`
  only when confirmed in Play Console or on the live listing; the name contains
  no "screen time", "digital wellbeing" or third-party trademark.

## Law waivers (owner-approved exceptions to this repo's `rule.md`)

- **None.** Law 3 (idea → published ≤ 4 weeks) was **kept, not waived**
  (2026-10-15). The historical four-week statement applies only to Phases 0–4;
  phases 5–6 were sequenced outside that historical window. Owner clarification
  (2026-10-22): it creates **no fixed calendar deadline for proposed Phases 7–15**.
  Do not invent a schedule or imply that this clarification is authorization to
  implement multiple phases at once.

## Agent decisions (reversible — object and they change)

- 2026-10-22 — **Phase 7 data-safety implementation choices** (all reversible on
  objection): (a) migrations stay **additive-only** — explicit `1→2→3→4→5` steps
  that create tables and never rewrite a row, because the full git history shows
  no column was ever renamed, retyped or dropped; (b) database version stays **5**
  and `fallbackToDestructiveMigration()` is removed so an unknown schema fails
  loudly instead of wiping history; (c) the backup file is **in-tree JSON**
  (`formatVersion` 1 envelope + `schemaVersion`, app-id stamped) rather than a new
  dependency, so the restore path is unit-testable on the JVM; (d) restore is
  **transactional and refuses before deleting** anything when the file is from a
  newer schema, another app, or cannot fill a required column; (e) OS backup is
  excluded twice (`allowBackup=false` plus explicit per-domain rules) so a later
  manifest edit cannot silently re-enable it.

- 2026-10-15 — **Playbook pin** — the launcher's `<SHA>` placeholder arrived
  unreplaced, so the pin was resolved to AppFlow `main` tip
  `c8856022ca57cdf23ae78b9491481543db89438d` (2026-10-11) and every playbook
  file required by PLAN.md stage 1 was read at that commit. Reversible: name a
  different SHA and the plan is re-checked against it.
- 2026-10-15 — **Product angle** — originally planned-vs-actual on one timeline
  with a no-permission core. **Superseded the same day** by the owner's revision:
  the product is now *his* time-efficiency cockpit (usage insight → sweet
  notifications → widget → limits/blocking → planning/tomorrow → earnings +
  learning → weekly review), and planned-vs-actual survives as one part of it
  (phase 3 + the weekly report) rather than the whole idea. Reversible on
  objection.
- 2026-10-15 — ~~**No `PACKAGE_USAGE_STATS`, no Accessibility, no overlay in v1**~~
  — **REVERSED by the owner the same day** (see "Plan revision" above): per-app
  usage insight is the core of the app, so `PACKAGE_USAGE_STATS`,
  `QUERY_ALL_PACKAGES`, and (phase 4 only) an Accessibility service + overlay are
  in, bounded by the fixed permission set in `rule.md` §5. The original reasoning
  is kept visible because it still governs two things: **in-app feature blocking
  stays out of scope** (R8) and every automatic number stays labelled
  *device-reported foreground time*.
- 2026-10-15 — **RULES 9 reading** — with **no ad SDK shipped**, the required
  ad-free one-time purchase is not applicable. Originally paired with a
  paid-upfront listing; since the owner cancelled publishing, there is **no
  monetization at all**, which satisfies RULES 9 a fortiori. Reversible on
  objection.
- 2026-10-15 — ~~**Kill test calibration** (K0–K3, store-based)~~ — **VOID**: no
  store. Replaced by the personal-use test P0–P3 (`docs/REQUIREMENTS.md` §9),
  written before any code existed.
- 2026-10-15 — **Tech shape** — Kotlin, Jetpack Compose, Room, WorkManager,
  single-activity, no DI framework, no analytics/crash SDK (Play Vitals is
  enough and keeps Data Safety empty and true). targetSdk **36** (current Play
  requirement as checked in AppFlow on 2026-10-10 — re-check on release day),
  minSdk **26**. Reversible.
- 2026-10-15 — **Data model shape** — `Activity`, `DayPlan`, `Block` (planned),
  `ActualEntry`, `Template`, `TemplateItem`, `CheckIn`, `DayAggregate`,
  `Setting`; money-free, single-user, no sync. Amounts of time are integer
  minutes; days are local-date keys; deletion is soft for history. Sketch only —
  no code in plan docs. Reversible.
- 2026-10-15 — **Phase sizing** — 7 phases (0–6), each 3–4 parts, each ending in a
  downloadable `app-progress-apk`; anything smaller than half a day merged into a
  neighbour. Re-cut after the revision to follow the owner's own priority order:
  1 usage insight · 2 sweet notifications · 3 widget + planning/tomorrow ·
  4 limits + blocking · 5 earnings + learning · 6 weekly review + durability. The
  risky phase (4, Accessibility/overlay) is deliberately **after** the value he
  asked for first, with a notification-only fallback recorded in the roadmap.
  Reversible.
- 2026-10-15 — **Evidence limits stated, not hidden** — the 10×1-star audit and
  the install-and-observe pass could not be completed from this environment
  (public pages expose 2–3 reviews; indexes paywall the rest). Recorded as an
  owner verification task before submission rather than filled with invented
  reviews.

- 2026-10-15 — **Earning time is protected, not limited.** Because ProRewards /
  OfferPro / coin-earn games are income, *Earning* is modelled as a **floor**
  budget and the always-allowed list can never be locked by a Waste limit while an
  earning session runs (R18, unit-tested). A generic screen-time app would treat
  ₹12.50-per-install work as doomscrolling; this one must not. Reversible.
- 2026-10-15 — **₹ per hour as the headline earning metric** (R26): a logged
  ₹12.50 install is joined to that app's real device-reported foreground minutes to
  produce honest ₹/hour per source, so his limited time goes to the best-paying
  offerwall. Presented as his own logged arithmetic — **never** as an income
  forecast or promise. Reversible.
- 2026-10-15 — **In-app feature blocking (Reels/Shorts inside an app) is out**
  (R8): it needs an Accessibility service reading other apps' UI trees, breaks on
  every app update, differs per OEM, and is exactly where the 1Cr+-install
  incumbent fails in its own 1★ reviews. Whole-app limits cover the need.
  Reversible only on an explicit owner request.
- 2026-10-15 — **No automatic earning detection** from ProRewards/OfferPro (no API,
  no scraping, no reading their notifications); earnings stay a two-tap manual log.
  Reversible on an explicit owner request.
- 2026-10-15 — **No second reward loop of our own** (coins/streak-gems/points): he
  already earns coins elsewhere, and a competing gamification loop would work
  against the actual goal. The learning streak is the only gamification. Reversible.
- 2026-10-15 — **Widget honesty** (R15): Android cannot refresh a widget
  continuously without battery cost, so it refreshes on screen-on, on relevant
  change and via a periodic worker, and always shows an "as of HH:MM" stamp rather
  than implying a live-to-the-second number. Reversible.
- 2026-10-15 — **Documented defaults instead of questions** for the six owner-only
  inputs in `docs/REQUIREMENTS.md` §12 (app classification, budgets, college
  timetable, notification tone/quiet hours, device model, ₹ rate): each ships with
  a sane editable default and is logged here as reversible, so building is never
  blocked waiting on an answer.
- 2026-10-15 — **Tech shape (updated)** — Kotlin, Jetpack Compose, **Glance** for
  the widget, Room, WorkManager, `UsageStatsManager` for ingestion,
  `AccessibilityService` + `SYSTEM_ALERT_WINDOW` only in phase 4, single activity,
  no DI framework, no analytics/crash SDK, no `INTERNET` in the core path.
  targetSdk **36** (current Play requirement as checked in AppFlow on 2026-10-10 —
  re-checked only if publishing is ever revisited), minSdk **26**. Reversible.
- 2026-10-15 — **Twelve candidate features proposed, none added to the roadmap**
  (`docs/REQUIREMENTS.md` §11.5, C1–C12): deadline/exam countdown, class-gap
  earning advisor, opportunity-cost insight, payout tracker, source reliability
  notes, bedtime shutdown, morning protection, focus whitelist, learning
  continuity, auto-backup triggers, audit export, permission/battery health. They
  were awaiting a choice at that time. The owner-approved 2026-10-22 Daily Logger
  direction supersedes that candidate list for Phases 7–15; C1–C12 are not selected
  and are not current owner gates. Do not re-offer them unless the owner reopens
  the scope.
- 2026-10-15 — **Data model shape** — see `docs/REQUIREMENTS.md` §6:
  `AppUsageSample`, `AppInfo`, `Category`, `DayPlan`, `Block`, `Template` +
  `TemplateItem`, `NotificationRule`, `LimitEvent`, `EarningEntry`,
  `LearningEntry`, `CheckIn`, `DayAggregate`, `Setting`. Integer minutes, integer
  paise, local-date keys, append-only history, every aggregate recomputable from raw
  rows and unit-tested. Sketch only — no code in plan docs. Reversible.

## Owner update — default categories (2026-10-23)

- **Asked:** whether app categories should be filled in "by default" instead of being
  assigned app by app. **Answer: suggestions the owner confirms — not silent auto-fill**,
  and the work is scheduled **after Phase 7 closes**, not inside the data-safety phase.
- Recorded as **R2a** in `docs/REQUIREMENTS.md`. Rationale kept with the decision:
  silently pre-filling would make the app's own guess look like the owner's judgement,
  and the Categories screen is the one place where his intent is the data. A suggestion
  he taps is unambiguously his.
- Constraints carried into that work: use only the device's own
  `ApplicationInfo.category` plus the system-life-app rule, never a committed list of
  real third-party packages, never overwrite or re-suggest an existing assignment, give
  **no** suggestion when the signal is ambiguous, and never auto-suggest earning apps.
- Trigger for starting it: Phase 7 marked ✅ (its P0 item) and merged.

## Open (current owner gates)

- The 2026-10-15 decision batches and the 2026-10-22 product/delivery gate answers
  are closed. Do not re-ask the label, UI, repository, timebox, reminder-default or
  Android-backup decisions recorded above.
- This documentation-closure request authorizes documentation only. Phase 7
  implementation remains a separate phase and is not started here; no checklist
  schema may ship before its backup, migration and signing/install evidence is
  complete.
- Remaining Phase 7 work is **technical evidence, not an unanswered product
  preference**: non-destructive paths from Room v1–v5; complete SAF backup/restore;
  verified Android OS-backup exclusion; signing-certificate continuity and an
  update-over-install proof or owner-approved data-preserving procedure; and the
  P0 owner-device check before Phase 8. If signing continuity is unknown or
  unstable, stop at the owner gate in `docs/ROADMAP.md`.
- **OPEN — Phase 7 signing gate (2026-10-22).** Evidence gathered in
  `docs/phases/07-data-safety-migration.md` shows the progress APK signer is a
  per-runner throwaway debug key and the release job is missing the secret name
  `ANDROID_KEYSTORE_BASE64` while Gradle has no `signingConfigs`, so no stable
  identity exists. Awaiting, from the owner only: the device update-over-install
  result (both run IDs), the installed data's value (disposable / valuable /
  absent) and approval of the one-key owner-controlled identity. No signing,
  workflow or Gradle change is made before that answer.
- Phases 7–15 have no fixed calendar deadline. Before the 14-day real-use gate
  after Phase 10, agree its measurable criteria; it does not replace P0–P3.
- Owner-local domains, routines, tasks, app lists and reminder windows remain
  private on-device choices. Use generic editable defaults and do not ask for
  personal timetable/routine details in chat.
- The repository remains public under its current name. A repository rename is
  not authorized by this work and is not a pending blocker for the approved plan.

## Owner verification tasks (before any store submission)

- [ ] Complete the 10 most-recent 1-star review audit for StayFree, Actiondash
      and TimeTune in Play, in the IN store, and paste findings into
      `docs/RESEARCH.md` (loop step 3).
- [ ] Install and use StayFree and TimeTune for a day each; note real ad/permission
      friction and planner overlap UX (loop step 6). No claims of "ad-poison"
      without direct observation.
- [ ] Re-check the live Play target API requirement, sensitive-permission
      expectations and the closed-test rule for the account type on release day.
- [ ] Confirm the owner's own 21-day K0 log (kill test) before spending anything
      on publishing.
