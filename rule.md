<!-- SPDX-License-Identifier: MIT -->

# Day Ledger — rules

> The operating manual for THIS repository — **self-contained**: follow it even
> if no other repo is reachable. Derived from AppFlow @
> `c8856022ca57cdf23ae78b9491481543db89438d` (provenance only; the text below is
> authoritative for this app). Owner answers and waivers live in
> `docs/DECISIONS.md`; the requirements live in `docs/REQUIREMENTS.md`; the living
> state lives in `prompt.md`.

## 1. Identity

- App: **Day Ledger** (working name) · package: **`io.github.pabi277.dayledger`**
  (permanent once published) · license: **MIT** (owner, 2026-10-15)
- What it is: a **personal-use, single-user, offline, English-only**
  time-efficiency app for the owner's own day — device-reported per-app usage
  insight, sweet customizable notifications, a home-screen widget readable
  **without opening the app**, planned 24-hour blocks with a **Tomorrow plan**,
  app limits with **customizable block durations**, an offerwall earnings log
  (~₹12.50 per app install, with real ₹/hour attribution) and a daily learning
  streak. No account, no backend, no ads, no analytics, no store listing.
- **One user: the owner** — a college student who earns a little through
  ProRewards / OfferPro offerwalls and coin-earn games and has committed to
  learning something every day. Sideloading the CI `app-progress-apk` is the
  normal install path. Publishing is cancelled (owner, 2026-10-15) and may only be
  reconsidered as a fresh decision, with a fresh plan, after the personal kill
  test P1–P3 passes.
- The spec is **`docs/REQUIREMENTS.md`** (owner's own words + R1–R34). Nothing is
  built that is not in that file.
- Git author: designated by the owner in `docs/DECISIONS.md` (kept out of prose
  elsewhere). AppFlow's own approved addresses are **not** inherited by this repo.
- Work only on the assigned session branch.
- Time budget: **4 weeks — law 3 kept, no waiver** (owner, 2026-10-15), covering
  phases 0–4. Phases 5–6 sit in the tail the owner's scope decision placed outside
  that clock.

## 2. Branching and the merge gate (law)

- Work only on the assigned session branch. Never create/switch branches, push
  `main`, force-push, or `git reset --hard`.
- Commit and push the session branch freely. Before committing, verify
  author/committer identity with `git var` and inspect the commit before pushing.
  Never use an agent's personal email/account.
- The agent never opens a PR, merges, pushes `main`, tags, releases, publishes,
  deploys, or submits anything anywhere without my explicit command in chat.
  Green CI is evidence, not authorization. "Open a PR" does not mean "merge".
- One phase at a time; never a second PR while one is open.

## 3. Questions — ask less, gate always

**GATE — always ask; never act without an explicit yes:**
repo creation · app name + package ID (permanent once published) ·
git author identity · license if unfixed · PR · merge · `main` push ·
release · publish · deploy · store submission · money in any form ·
legal/tax/store-account actions · other people's personal data · waiving a law of
this file · changing an answer recorded in `docs/DECISIONS.md`.
→ One Summary batch per gate moment, ≤ 4 questions, each naming its gate.

**DECIDE-ONCE — ask once (normally the plan chat), record in
`docs/DECISIONS.md` with date:** languages/locales · monetization model ·
scope cuts · time budget · design direction.
→ Never re-ask a recorded answer — that is a defect, not caution. Changing one
is a GATE. **Already answered on 2026-10-15: direction (personal-use time app),
English-only, no ads, 4 weeks, core-first scope. Do not ask these again.**

**DEFAULT — never ask:** researchable facts (research, cite source + date) ·
reversible implementation choices (log under Agent decisions) · anything already
answered in chat or docs · taste with an obvious default. The six owner-only
inputs listed in `docs/REQUIREMENTS.md` §12 are **not** questions to ask in a
batch: use the documented default, log it as reversible, and let the owner
correct it whenever he likes.

## 4. Work cycle — one phase per chat

1. Verify state first: `git status`, `git log`, PRs, CI runs. Trust the
   repository, not memory.
2. Evidence before hypothesis: `file:line` reads against the current tree. No fix
   on a guess.
3. Implement the current phase only (3–4 parts; one whole capability). Its "Done
   means" cell in `docs/ROADMAP.md` is the acceptance test, and its requirement
   IDs (R…) in `docs/REQUIREMENTS.md` are the scope. Nothing outside it — in
   particular: no in-app feature blocking, no automatic earning detection, no
   accounts, no cloud sync, no second locale, no store work.
4. Tests + CI green. CI is the executor of record. The run must upload the
   downloadable **`app-progress-apk`** artifact — every phase must be visible to me
   as a sideloadable build on my own phone.
5. Docs ride with the code commit: `docs/phases/NN-<slug>.md` record (including
   what the APK shows), roadmap status, `docs/REQUIREMENTS.md` if a requirement
   changed, `docs/DECISIONS.md` if anything new, and `prompt.md` updated.
6. Report (run id, tip sha, APK artifact name, what is now visible) — then
   **stop at the merge gate** (§2).

## 5. Invariants — do not break

- **Offline-first, single-user, on-device.** No backend, login, analytics or
  crash-reporting SDK; no `INTERNET` use in the core path. The only data that ever
  leaves the phone is a backup/CSV file the owner explicitly picks (law 13).
- **Permission set is fixed and owner-directed** (2026-10-15, reversing the
  earlier agent "no sensitive permissions" decision — see `docs/DECISIONS.md`):
  `PACKAGE_USAGE_STATS` (per-app time), `QUERY_ALL_PACKAGES` (app labels),
  `POST_NOTIFICATIONS`, exact alarms, `RECEIVE_BOOT_COMPLETED`, and — for phase-4
  blocking only — an Accessibility service plus `SYSTEM_ALERT_WINDOW`.
  **Never** requested: location, contacts, camera, microphone, SMS/call logs,
  storage-wide access, `READ_PHONE_STATE`. Adding anything beyond the fixed set is
  a GATE-level change. The Accessibility service is used **only** to detect the
  foreground package and show the block screen — never to read screen content,
  never to harvest text; that sentence must also appear in the in-app disclosure.
- **Never block his income.** Earning apps (ProRewards, OfferPro, coin-earn games)
  and the always-allowed list (dialer, SMS, UPI/banking, maps, camera, college
  apps) can never be locked by a Waste limit while an earning session is running.
  Unit-tested, not merely intended.
- **Honest numbers.** Every automatic figure is labelled *device-reported
  foreground time* and carries an "as of HH:MM" stamp. No precision the OS does
  not actually provide; no productivity, income or behaviour-change promise
  anywhere in the UI.
- **Anti-nag.** A daily notification cap the owner sets (default 6), quiet hours
  that are never violated, de-duplicated triggers, and a one-tap "less often" on
  every notification. A reminder he swipes away forever is a failed feature.
- **History is sacred.** Past days are never deleted, pruned or paywalled.
  Destructive actions are undoable and must actually work.
- **No ads, ever** (owner decision 2026-10-15). No ad SDK, no mediation, no
  "remove ads" purchase — with no ads there is nothing to remove. There is also no
  store listing and no monetization; adding either is a GATE + a
  `docs/DECISIONS.md` change.
- No secrets, signing keys, account IDs, or anyone's personal data in git or chat.
  The owner's own earnings and usage figures stay on-device; any seed/sample data
  committed to the repo must be fictional.
- **No behaviour or outcome claims.** No "cures phone addiction", no guaranteed
  productivity, no income promise, no implied affiliation with Google, Play,
  ProRewards, OfferPro or any device maker.
- Agent-invented facts are never shipped. Every number in the UI (adherence %,
  ₹/hour, streaks, time leaks) must be reproducible from the stored rows and
  covered by unit tests.
- **Kill fast.** The pre-written **personal-use kill test P0–P3** in
  `docs/REQUIREMENTS.md` §9 (written 2026-10-15, before any code existed) decides
  keep-building vs iterate vs stop. It replaces the store-based K0–K3 test, which
  is void because there is no store. Never rewrite it after seeing results. **P0
  gates everything:** if real per-app usage cannot be shown on the owner's own
  phone, no later phase is built until it can.
- **Battery discipline.** Batched usage polling, WorkManager for periodic work, no
  foreground service unless phase-4 blocking genuinely requires one, and Settings
  shows the app's own battery cost and notification count. If the app costs more
  time or battery than it saves, that is a defect.
- App-specific invariants appended as phases decide:
  - _phase 1:_ times and durations are integer minutes in the device's local
    timezone; a day is keyed by local date; overnight blocks belong to the day they
    start; money is integer paise, never a float.
  - _phase 1:_ all user-facing strings (including notification templates) live in
    resources from day one, so a later Bengali locale stays cheap.
  - _phase 2:_ every notification the app can send is described in one place in
    Settings, with its trigger, schedule and wording visible.
  - _phase 3:_ the widget never shows a number without its "as of" stamp.
  - _phase 4:_ every limit override is logged and surfaced in the weekly report —
    no silent cheating.
  - _phase 6:_ backups carry a schema version; restore must handle an older schema
    without data loss.

## 6. Definition of done (any change)

CI green · tests · docs updated (`docs/phases/` record, roadmap status,
`prompt.md`, and `docs/REQUIREMENTS.md` if a requirement moved) ·
`app-progress-apk` uploaded and sideloadable · the owner can see this phase's
increment on his own phone · short report — then stop at the merge gate. There is
no release process: this app is not published, so no store checklist applies
unless the owner ever opens that decision again.
