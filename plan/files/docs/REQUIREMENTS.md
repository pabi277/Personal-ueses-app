# Requirements — Day Ledger (`io.github.pabi277.dayledger`)

> The owner asked for this file **before any code**: the full usage picture, his
> own requirements, and the insight the app must give him. This is the
> source-of-truth spec. `docs/ROADMAP.md` is derived from it; nothing is built
> that is not in here.
>
> Status: **draft v0.2 — owner review, 2026-10-15.** Add, cut or reword anything;
> every change is dated in `docs/DECISIONS.md`.
> Provenance: AppFlow @ `c8856022ca57cdf23ae78b9491481543db89438d`.

## 1. Who this app is for (owner statement, 2026-10-15)

> *"I want the app for only my use cases… I am a college student and I earn a
> little bit using a app name prorewards, offerwall- offerpro, unlimited coin
> earn games per app install i can earn 12.5 rupees. I have to learn something
> parday. The main thing of the app will be sweet notifications to remind me how
> much I spend time on phone and a wished system for Home screen show even I don't
> open the app. I can see how much I spend time on which app, customizable
> notification option, customised time duration of notification block option app.
> Etc. Other features that are needed for a time efficient app. Add another
> facility like tomorrow plan etc"*

Consequences that shape everything below:

- **One user.** No accounts, no sync, no backend, no store listing, no
  monetization, no localization project. Sideloading the CI debug APK is the
  normal install path. This is recorded in `docs/DECISIONS.md` and it supersedes
  the publishing half of the earlier plan.
- **His day has four kinds of time, and they are not equal.** College time ·
  **earning time** (ProRewards, OfferPro offerwall, unlimited-coin earn games —
  ~₹12.5 per app install) · **learning time** (his self-imposed "learn something
  every day") · waste time (reels/shorts/social). A generic screen-time app
  treats earning time and doomscrolling as the same thing — **that is exactly
  why the existing apps are "not very much suitable"** for him. Day Ledger must
  never tell him to stop earning.
- **The app must be visible without being opened** (widget + notifications),
  because the failure mode is forgetting, not refusing.

## 2. A day in the life (the usage the app has to survive)

| When | What happens | What the app must do |
|---|---|---|
| Wake, first 10 min | Reaches for the phone before anything else | Widget already shows yesterday's total + today 0h 00m; a soft "good morning, yesterday was 6h 12m, today's plan is ready" notification |
| Morning → afternoon | College: classes, commute, gaps between periods | A *College* template applied to today; phone mostly idle; class gaps are the natural offerwall windows |
| Gaps / evening | Offerwall grind: install an app, wait, earn ~₹12.5 per install; coin-earn games | Earning session started with one tap; per-app time attributed to *Earning*; ₹/hour computed automatically |
| Evening | Must learn something (his daily rule) | Study block with a reminder; streak; "you have learned 0 minutes today" if empty |
| Night | Wind-down, reels/shorts creep in | Sweet nudge at the waste budget; optional block with a chosen duration; tomorrow planned in 2 minutes |
| Any moment | Does not want to open the app | Widget on the home screen carries the truth: total, top app, next block, today's ₹ |

## 3. Requirements

Priority: **M = must have in v1** · **S = should** · **C = could (only if the
owner asks)** · **W = won't (explicitly out, with reason)**.
Every M/S carries its acceptance test — that is what "done" means, verified on
the owner's own phone.

### A · Usage insight (R1–R8)

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R1** | Show **how much time was spent on which app** — today, yesterday, any past day, and per week. Read from the device's own usage statistics; nothing leaves the phone. | **M** | Grant the special usage access once, then: today's list shows real per-app durations that match the system Settings → Digital Wellbeing numbers within ~10%; scrolling back 7 days works offline. |
| **R2** | **Categories**, owner-editable: *Earning* (ProRewards, OfferPro, earn games), *Study*, *College*, *Social/Waste*, *Life* (calls, maps, banking, UPI). Every app gets exactly one category; unknown apps default to *Unclassified* and can be assigned in one tap. | **M** | Assigning Instagram to *Waste* and ProRewards to *Earning* immediately changes the donut chart and the weekly report. |
| **R3** | **Budgets per category and per app**, in minutes per day (e.g. Waste ≤ 45 min, Earning ≥ 30 min, Study ≥ 30 min). Budgets can be a floor *or* a ceiling — earning and study are floors, waste is a ceiling. | **M** | Setting Waste ≤ 45 shows a remaining/overrun number on Today and drives R9/R11. |
| **R4** | **Pickups/unlocks count** and the longest continuous session per app, not just total minutes — because 90 pickups of 40 seconds is the real disease. | **S** | Today shows unlock count and "longest session: YouTube 41 min". |
| **R5** | **Hourly heatmap** of the day (24 cells) so the leak hours are visible at a glance. | **S** | The heatmap for yesterday highlights the 22:00–01:00 band when that is where the time went. |
| **R6** | **Comparison**: this week vs last week, per category and per app, with ▲▼ deltas. | **S** | Weekly screen shows "Waste −38 min vs last week, Earning +₹112". |
| **R7** | **Honest-labelling rule:** every automatic number is labelled *device-reported foreground time*. Where the OS under-reports (OEM bugs, background audio, split screen), the app says so instead of pretending to precision. | **M** | The Today screen footer carries the label; no number is presented as exact to the second. |
| **R8** | **In-app feature blocking** (e.g. block Reels/Shorts *inside* an app while leaving messages usable). | **W** | Out of scope: it needs an Accessibility service reading other apps' UI trees, is fragile across app updates and OEMs, and is exactly where the 1Cr+-install incumbent fails in its own 1★ reviews (*"Does not work for TikTok! … the app remains openable and functional"*). Whole-app blocking (R13) covers the need. Revisit only on an explicit owner request. |

### B · Sweet notifications (R9–R12) — the owner's stated "main thing"

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R9** | **Sweet usage reminders**: at moments the owner chooses, a gentle notification tells him how much time he has spent — total and on the app/category that is over budget. Wording is warm and non-shaming, and **fully customizable** (templates + free text + emoji). | **M** | At 21:00 the phone shows e.g. *"Today: 5h 40m 🌙 Waste 52m over your 45m. Earning done ₹87. Tomorrow's plan is one tap away."* — and the owner can rewrite that entire sentence. |
| **R10** | **Customizable notification options**: per-notification on/off, time of day, repeat days, which categories/apps trigger it, tone (gentle / plain / firm), sound & vibration, and a **quiet hours** window that is never violated. | **M** | Turning off the midday nudge stops it; setting quiet hours 23:30–07:00 means nothing fires inside it, verified over one night. |
| **R11** | **Threshold triggers**, not just clock times: notify when a category crosses its budget, when total screen time passes a chosen number, or when an app has been in the foreground for N continuous minutes. | **M** | With Waste ≤ 45, crossing 45 produces exactly one nudge (not one per minute). |
| **R12** | **Anti-nag discipline**: a maximum of N notifications per day (owner-set, default 6), automatic de-duplication, and a one-tap "less often" action on every notification. A reminder that gets swiped away forever is a failed feature. | **M** | With N = 6, a heavy day produces ≤ 6; tapping "less often" reduces that trigger's frequency and says so. |

### C · Home-screen widget (R13) — visible without opening the app

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R13** | **Home-screen widget** showing, without opening the app: today's total screen time, the top app (or top *Waste* app), the next planned block and its countdown, today's earnings ₹, and the study-streak. Resizable; dark/light aware. | **M** | Widget placed on the home screen shows live-ish numbers after a screen-on; the owner can read his day's total without unlocking into the app. |
| **R14** | **Tap targets** on the widget: tap total → Today; tap ₹ → Earnings; tap next block → start/shift it; long-press → configure which fields show. | **S** | Each target opens the right screen in under a second. |
| **R15** | **Refresh honesty**: the widget updates on screen-on, on app-usage change where the OS allows, and on a periodic worker. **Android does not permit a truly continuous per-second widget refresh** without draining the battery, so the widget shows a small "as of HH:MM" stamp. | **M** | The stamp is visible; the owner knows the number's age instead of trusting a stale one. |

### D · Limits and blocking (R16–R19)

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R16** | **Customized time duration for blocking an app**: per app or per category, a daily limit; when it is used up, the app is locked and the owner chooses the **block duration** — e.g. 15 min / 30 min / 1 h / until tomorrow / permanent-until-I-unlock. The duration list itself is customizable. | **M** | Hitting the 45-min Waste limit locks the chosen apps; picking "30 min" unlocks them exactly 30 minutes later without any further taps. |
| **R17** | **Block screen** that is kind, not punitive: shows what was used, what the plan says, and offers "extend by 10 min (once)", "skip to next block", or "unlock anyway" with a deliberate hold-and-wait + optional PIN. Every override is logged and appears in the weekly report — no silent cheating. | **M** | "Unlock anyway" requires a 5-second hold + PIN and the override is listed in Sunday's report. |
| **R18** | **Always-allowed list**: phone/dialer, SMS, UPI/banking, maps, camera, college apps, and **the earning apps while an earning session is running** — the app must never block his income. | **M** | With ProRewards marked *Earning* and a session running, the Waste limit cannot lock ProRewards; verified by test. |
| **R19** | **Focus session**: start "study 50 min" → chosen apps blocked, a countdown on the widget, and a completion record feeding the streak. | **S** | A 50-min focus session blocks Instagram, survives the screen turning off, and lands in the study streak. |

### E · Planning, tomorrow plan, routine (R20–R24)

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R20** | **24-hour timeline of blocks** (planned), sized proportional to duration, with activities, colours and notes; add/edit/move/resize; explicit overlap indicator. | **M** | A full day is planned in under 2 minutes from a template; overlapping blocks are visibly flagged. |
| **R21** | **"Tomorrow plan"** — the owner's explicit ask: an evening screen that pre-fills tomorrow from a template, lets him adjust in a few taps, and can be reached **from a notification and from the widget** without opening the day view. Plus "plan the rest of today" for mid-day resets. | **M** | At 22:00 the nudge opens Tomorrow directly; three taps produce a saved plan that becomes today at midnight. |
| **R22** | **Day templates** for his real week: *College day*, *Study day*, *Earning day*, *Off day*. Create, edit, apply, apply to several days, sorted by last used. | **M** | Applying *College day* fills 08:00–17:00 correctly; templates list shows most-recently-used first. |
| **R23** | **Per-block reminders** with exact-time alarms and custom sound/vibration; a block can be marked *hard* (reminder + optional lock of everything else). | **M** | A 19:00 study block fires its reminder with the screen off and the phone on silent-but-vibrate. |
| **R24** | **"Shift the rest of my day by N minutes"** — the day always slips; one tap moves every remaining block. Undoable. | **S** | At 15:20, "shift +30" moves the remaining blocks and one undo restores them. |

### F · Earnings + learning tracker (R25–R28) — the personal edge no store app has

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R25** | **Earning log**: source (ProRewards / OfferPro / other), task type (app install, coin-earn game, survey), amount (default ₹12.50 per install, editable), and the app installed. Two taps to add. | **M** | Adding "installed X, ₹12.5" takes ≤ 2 taps and shows in today's total. |
| **R26** | **Auto-time join**: for logged installs, the app reads how long the installed app was actually in the foreground (from R1's data) and computes **real ₹ per hour** per source and per task — the number that decides where his time is best spent. | **M** | A ₹12.5 install that took 24 minutes of foreground time is reported as ₹31.25/h, and the weekly report ranks sources by ₹/hour. |
| **R27** | **Earning targets & insight**: today's ₹, this month's ₹, run-rate, best ₹/hour window of the day, and a gentle flag when a source's ₹/hour drops below his study value. **No income promise anywhere** — these are his own logged numbers, not forecasts. | **S** | The Earnings screen shows month-to-date ₹ and the best hour band; nothing in the UI claims he will earn any particular amount. |
| **R28** | **Daily learning tracker**: what he learned (free text or checklist), minutes, and a streak with a visible chain. Satisfies *"I have to learn something parday."* | **M** | A day with zero learning shows an unbroken-chain gap; logging one item keeps the streak and shows on the widget. |

### G · Review, data, platform (R29–R34)

| ID | Requirement | P | Acceptance test |
|---|---|---|---|
| **R29** | **Weekly report** (Sunday night): planned vs actual by category, top 3 time leaks, unlocks, earnings ₹ and ₹/hour, learning minutes, override count, best and worst day, next week's suggestion. | **S** | The report renders from stored data alone, offline, and every number is reproducible from the raw entries (unit-tested). |
| **R30** | **Check-in**: a 30-second evening prompt (3 questions, skip-safe) that feeds the weekly report. | **C** | Skippable without guilt; three taps to finish. |
| **R31** | **All data local**; on-device database. **Backup/restore to a file he chooses** (SAF) + CSV export. History is never deleted, pruned or paywalled. Backup is scheduled (weekly) and reminds him if it has not run. | **M** | Clear the app's data, restore from the file, lose nothing; CSV opens correctly in a spreadsheet. |
| **R32** | **No ads, no analytics, no crash SDK, no account, no network calls in the core path.** The only reason this app ever touches the internet is nothing — it does not need to. | **M** | A network-permission audit shows no `INTERNET` usage in the core path; Data Safety would read "no data collected" if it were ever published. |
| **R33** | **Battery discipline**: usage polling batched, WorkManager for periodic work, no foreground service unless blocking (R16) genuinely requires one, and the app must show its own battery cost honestly in Settings. | **M** | One full day of normal use adds no visible battery drain versus the day before (owner-observed, recorded). |
| **R34** | **English only** for v1 per the recorded decision (2026-10-15). All user-facing text — including notification templates — lives in resources so a Bengali locale stays a cheap later change, and notification wording is user-editable anyway (R9). | **M** | No hard-coded strings; `values-bn/` can be added later without touching logic. |

## 4. Insight model — what the app must tell him (the "edit insight" ask)

Insight is not a chart dump. Every insight is **configurable** (on/off, thresholds,
which apps/categories it watches) and each one answers a question he actually has:

| # | Question he has | Insight the app produces | Source |
|---|---|---|---|
| I1 | "How much phone did I use, and on what?" | Today/week total + per-app + per-category, with ▲▼ vs last week | R1, R2, R6 |
| I2 | "Is my waste time getting worse?" | Waste vs its ceiling, per day, with the leak hours highlighted | R3, R5, R11 |
| I3 | "Am I earning efficiently?" | ₹ today / this month, **₹ per hour by source**, best hour band, installs logged vs time actually spent in the installed apps | R25–R27 |
| I4 | "Did I learn something today?" | Learning minutes, streak chain, gap days | R28 |
| I5 | "Does my plan survive the day?" | Planned vs actual adherence %, blocks completed, shifts and overrides counted | R20, R24, R29 |
| I6 | "What is the one thing to fix tomorrow?" | Weekly report's single top suggestion, generated from the largest gap — and the Tomorrow plan pre-filled to act on it | R21, R29 |
| I7 | "Is the app itself costing me?" | Its own battery cost and notification count, shown plainly | R12, R33 |

## 5. Screens (sketches — never code)

1. **Onboarding / permissions** — 3 steps: grant usage access (system Settings
   page, explained honestly), grant notifications, pick the earning + waste apps
   to classify. Skip-safe; nothing else is asked.
2. **Today** (home) — big total, category donut, per-app list, budget bars, hourly
   heatmap strip, next block, today's ₹, learning tick. Footer: *device-reported
   foreground time, as of HH:MM*.
3. **Insights / Weekly** — week vs week deltas, top 3 leaks, ₹/hour ranking,
   adherence %, overrides, the one suggestion for next week.
4. **Plan** — 24-hour vertical timeline, proportional blocks, overlap flags,
   template picker, "shift rest of day by N", per-block reminder settings.
5. **Tomorrow** — evening-first: template pre-fill, three-tap adjust, save; also
   reachable from a notification action and a widget tap.
6. **Notifications** — list of every reminder/trigger with on/off, time, days,
   tone, wording editor with live preview, quiet hours, daily cap.
7. **Limits & blocking** — per-app/category daily limits, block-duration menu
   (editable), always-allowed list, strict-mode PIN, focus session launcher.
8. **Block screen** (shown over a locked app) — kind wording, what was used,
   "extend 10 min (once)", "skip to next block", "unlock anyway" (hold + PIN).
9. **Earnings** — add log (2 taps), today/month ₹, ₹/hour by source, best band,
   installed-app time attribution.
10. **Learn** — today's item, minutes, streak chain, history.
11. **Settings** — categories, budgets, widget fields, data (backup/restore/CSV),
    permissions status, battery cost, about + open-source notices.

## 6. Data model (sketch — never code)

`AppUsageSample(appId, date, foregroundMinutes, pickups, longestSessionMin, source="device")` ·
`AppInfo(appId, label, category, alwaysAllowed, limitMin, blockDurations[])` ·
`Category(id, name, kind=ceiling|floor, budgetMin, color)` ·
`DayPlan(dateKey, templateId?, note)` · `Block(id, dateKey, startMin, endMin, activityId, kind=soft|hard, reminder?, done)` ·
`Template(id, name, lastUsedAt)` + `TemplateItem(templateId, startMin, endMin, activityId)` ·
`NotificationRule(id, type=scheduled|threshold|digest, enabled, times[], days[], scope(appIds/categoryIds), tone, templateText, quietFrom, quietTo, dailyCap)` ·
`LimitEvent(dateKey, appId, limitMin, reachedAt, action=block|nudge, blockDurationMin, unlockedAt, override=none|extend|pin, pinned)` ·
`EarningEntry(id, dateKey, source, taskType, appId?, amountPaise, loggedAt, attributedMinutes?)` ·
`LearningEntry(id, dateKey, minutes, text, createdAt)` · `CheckIn(dateKey, q1, q2, q3, mood)` ·
`DayAggregate(dateKey, totalMin, perCategory{}, perApp{}, unlocks, earnedPaise, learnMin, adherencePct)` ·
`Setting(key, value)` — includes `strictPinHash`, `widgetFields[]`, `backupPath`.

Rules: integer minutes; paise for money (never floats); `dateKey` = local date;
overnight blocks belong to the day they start; history is append-only — deletes
are soft; every aggregate must be recomputable from raw rows (unit-tested).

## 7. Permissions and privacy (exact, and why)

| Permission | Needed for | Grant path | Cost / risk |
|---|---|---|---|
| `PACKAGE_USAGE_STATS` (special app access) | **R1** per-app time, R4 pickups, R26 attribution | Settings → Usage access, one tap-through; cannot be granted by a dialog | High-sensitivity **if published**; fine sideloaded. Data never leaves the device. |
| `QUERY_ALL_PACKAGES` | Show real app names/labels in the per-app list (Android 11+ package visibility) | Manifest | Restricted on Play (declaration form) — irrelevant while personal; recorded as a publish blocker. |
| `POST_NOTIFICATIONS` (API 33+) | R9–R12 | Runtime dialog | None. |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | R23 block reminders, R16 timed unlock | Runtime/settings on API 31+/33+ | Must survive OEM battery optimization; tested on the owner's device. |
| Accessibility Service | **R16/R17** detecting the foreground app to lock it | Settings → Accessibility, manual enable | The most sensitive capability in the app. **Used only to detect the foreground package and show the block screen** — never to read screen content. Declared plainly in-app. |
| `SYSTEM_ALERT_WINDOW` (overlay) | Drawing the block screen over a locked app | Settings | Same class as above; some OEMs need it, some do not. |
| `RECEIVE_BOOT_COMPLETED` | Reminders/limits survive a reboot | Manifest | None. |

**Never requested:** location, contacts, camera, microphone, storage-wide access,
`READ_PHONE_STATE`, SMS/call logs. **No `INTERNET` permission in the core path**
(R32) — this app has nothing to say to any server.

**Privacy in one line:** all usage, earnings and learning data stays in an
on-device database; the only export is a file the owner explicitly picks; there
is no account, no analytics, no crash SDK, no ads.

## 8. Hard limits — stated before building, not discovered later

1. **Usage access is a manual system grant.** No app can request it with a
   dialog; the owner must walk through Settings once. The onboarding does that
   walk with him (R1).
2. **Android reports foreground time, not attention.** It cannot know whether he
   was watching or dozing. Numbers are labelled device-reported (R7) — the
   verified complaint against the category leader is *"precise but not accurate…
   says 6 hours… really means half that"*, and we will not pretend otherwise.
3. **OEMs differ.** On some devices usage buckets and exact alarms are delayed or
   killed by battery optimization. Phase 1 ends with a verification pass on the
   owner's actual phone, and Settings shows what the OS actually granted.
4. **Widgets cannot refresh continuously.** Android's widget update floor is
   ~15–30 minutes; we refresh on screen-on, on relevant changes, and on a
   periodic worker, and we stamp the age of the number (R15). A widget claiming
   to be live to the second would be lying.
5. **Blocking is the fragile part.** It needs Accessibility + overlay, and
   determined users (and aggressive OEMs) can defeat it. That is why R17 makes
   overrides *logged* rather than pretending they are impossible, and why R8
   (in-app feature blocking) is out entirely.
6. **Notifications are dismissible by design.** Android lets him swipe them away.
   The answer is not louder notifications — it is the widget plus the anti-nag
   cap (R12, R13).
7. **No app can make him study.** This app measures, reminds, plans and reports.
   The 21-day personal kill test (§9) is how we find out honestly whether it
   changes anything.

## 9. Personal-use kill test (pre-written 2026-10-15, replaces the store test)

There is no store now, so the old install-based numbers are void. Written before
any code exists; **never rewritten after results are seen**:

- **P0 — buildability:** by the end of phase 2, the sideloaded APK shows real
  per-app usage for the current day on the owner's own phone, within ~10% of the
  system's own number. If that fails, the whole concept fails → stop and fix
  before anything else is built.
- **P1 — 21-day honest use:** the owner uses the app on **≥ 21 consecutive days**:
  reads the widget daily, ≥ 1 planned block on ≥ 18 days, ≥ 1 learning log on
  ≥ 15 days, ≥ 1 earnings entry on ≥ 10 days (or zero if he genuinely stopped
  offerwall work — recorded either way).
- **P2 — effect, measured from his own data:** comparing week 4 with week 1,
  **either** Waste minutes fall **or** Study minutes rise **or** ₹/hour rises —
  at least one, by ≥ 10%, **and** he still wants the app on his home screen on
  day 21.
- **P3 — cost:** no observed battery regression and ≤ the configured daily
  notification cap on every one of the 21 days.
- **Decision rule:** P0 fails → stop, fix the foundation, do not build on sand.
  P1 fails (usage lapses for ≥ 5 consecutive days) → the app is not serving him:
  one 1-week iteration on the *single* reason he stopped (logged), then re-run
  21 days once. P1 passes, P2 fails → keep it as a measuring tool, drop the
  ambition of behaviour change, and record that honestly. All pass → it is a
  tool worth maintaining; only then may publishing ever be reconsidered, as a
  fresh decision with a fresh plan.

## 10. Out of scope for v1 (and why)

In-app feature blocking (R8) · cloud sync, multi-device, web companion ·
accounts or any login · publishing, store listing, monetization, ads · Bengali
locale (kept cheap via resources, not built) · iOS/desktop · automatic earning
detection from ProRewards/OfferPro (no API, no scraping, no reading their
notifications without an explicit owner request) · gamification/coins of our own
(he already earns coins elsewhere; adding a second reward loop would compete with
his real goal) · any "guaranteed focus/productivity" claim.

## 11. Revised phase plan (replaces the publishing roadmap)

Full table with "Done means" cells in `docs/ROADMAP.md`. Every phase ends with a
downloadable, sideloadable `app-progress-apk`.

| # | Phase | Parts |
|---|---|---|
| 0 | Bootstrap + hello | frozen playbook snapshot + plan docs + MIT LICENSE + Gradle skeleton (targetSdk 36, minSdk 26) + CI with `app-progress-apk` |
| 1 | **Usage insight** (R1–R7) | permission onboarding · ingestion + local storage · Today screen · categories + budgets |
| 2 | **Sweet notifications** (R9–R12) | engine + gentle templates · full customization incl. quiet hours · threshold triggers · anti-nag cap |
| 3 | **Widget + planning** (R13–R15, R20–R23) | Glance widget with tap targets + honest refresh · 24-hour timeline · **Tomorrow plan** + templates · block reminders |
| 4 | **Limits & blocking** (R16–R19) | limits + customizable block durations · Accessibility/overlay block screen · logged overrides + PIN · always-allowed list + focus session |
| 5 | **Earnings + learning** (R25–R28) | earning log · ₹/hour auto-attribution · targets & insight · daily learning streak |
| 6 | **Review + durability** (R6, R24, R29–R31, R33) | weekly report · shift-my-day · backup/restore/CSV + weekly reminder · battery audit + polish |

Phase 4 is the riskiest and is deliberately after the value he asked for first
(usage → notifications → widget). If phase 4 proves unworkable on his device,
phases 1–3 and 5–6 still leave a complete, useful app — that is the fallback,
recorded now so it is not a mid-build surprise.

## 11.5 Candidate features — proposed 2026-10-15, awaiting the owner's pick

The owner asked *"find anything useful I can [put] in it"*. These are the
candidates the agent would add, each derived from his actual context (college
student · offerwall earner at ~₹12.50 per install · daily-learning rule · wants
the truth visible without opening the app). **None of them is in the roadmap
yet**: the four-week budget and kill test P0 come first, and the owner picks.
Each row says what it is, why it earns its place, its cost, and where it would
land.

| ID | Candidate | Why it is useful to *him* | Cost | Would land in |
|---|---|---|---|---|
| **C1** | **Deadlines & exam countdown** — enter exam dates, assignment/submission deadlines; a countdown chip on the widget and in notifications; the Tomorrow plan and the weekly report become deadline-aware ("exam in 3 days — your Study floor was missed twice this week") | A college student's time is not free-form; it is shaped by fixed dates. This is the single biggest gap in the current spec: it plans the day but knows nothing about the semester | ~½ day | phase 3 (with Tomorrow) + phase 6 (report) |
| **C2** | **Class-gap earning advisor** — from the *College day* template, detect free gaps ≥ 25 min between classes and suggest the best use per gap, ranked by his **own ₹/hour** data (e.g. "45-min gap → OfferPro install ≈ ₹12.50 / ~24 min = ₹31/h, your best rate") | Turns dead commute/gap time into the highest-rate earning window he personally has, using data the app already collects. No other app can do this because none of them know his rates | ~½ day | phase 5 (needs ₹/hour from R26) |
| **C3** | **"What that cost you" insight** — waste minutes over the ceiling expressed in his own opportunity cost: *"52 min of Reels yesterday ≈ ₹27 of foregone earning, or 52 min of learning"* — using his real ₹/hour, shown once a day at most, never as guilt-spam | Makes an abstract number concrete in the two currencies he actually cares about (money and learning). It is the insight most likely to change behaviour, and it costs nothing to compute once R26 exists | ~2 h | phase 5 |
| **C4** | **Payout tracker** — per source: pending vs received, cumulative ₹, and a note field for each offerwall's minimum-payout threshold and typical delay | Offerwall money is not instant; "earned" and "in hand" are different numbers, and mixing them makes the ₹/hour insight misleading | ~2 h | phase 5 |
| **C5** | **Source reliability notes** — per earning source/app: does the install actually credit, cooldown/daily-cap timers, and a "wasted" flag with the minutes lost, feeding a *do-not-bother* list | His scarcest resource is time; an offer that never credits costs him twice. This is the honest counterweight to C2 | ~2 h | phase 5 |
| **C6** | **Bedtime shutdown mode** — a chosen wind-down window: Waste apps lock, a "one more scroll costs you X minutes of sleep" line appears, the screen dims to a night theme, and the Tomorrow plan prompt fires at the start of the window | The verified leak band for students is late night (his own R5 heatmap will prove it). Combines R16 blocking + R21 Tomorrow into one habit | ~½ day | phase 4 |
| **C7** | **Morning protection** — for the first N minutes after the day's first unlock, Waste apps stay locked and the widget shows today's plan instead | He described reaching for the phone first thing; the first 30 minutes decide the day. Cheap: one time-window rule reusing the phase-4 lock path | ~2 h | phase 4 |
| **C8** | **Focus-session whitelist** — during a study/focus session, allow only chosen apps (notes, calculator, dictionary, college app) and lock everything else, not just the Waste category | R19 currently blocks a chosen set; a whitelist is the stronger, simpler-to-reason-about version and matches "learn something every day" | ~2 h | phase 4 |
| **C9** | **Learning continuity** — "yesterday you learned X — 2 minutes to revisit it?" plus a tiny spaced-repetition queue from his own learning log entries, and optional subject-wise goals (course/chapter, minutes toward it) | The daily rule sticks far better with one revisit nudge than with a blank box; still no content of ours, only his own entries | ~½ day | phase 5 |
| **C10** | **Auto-backup triggers** — back up automatically after any large entry (N earnings or learning rows) and before any schema migration, keeping the last 5 backups | His data is the app's whole value and there is no cloud; a scheduled weekly backup alone can lose a week | ~2 h | phase 6 |
| **C11** | **Semester/weekly time audit export** — one-tap PDF/image summary of a week or month (time by category, ₹ and ₹/hour, learning minutes, adherence) for his own records | He keeps no other record; a shareable audit also makes the weekly report reusable if he ever shows a friend | ~½ day | phase 6 |
| **C12** | **Battery + permission health screen** — shows exactly what the OS granted, whether usage access or the Accessibility service was revoked by an OEM battery killer, the app's own battery cost, and a one-tap re-grant walk-through | OEMs silently revoke these; the app would otherwise show empty numbers with no explanation. This is the difference between "broken app" and "fix in two taps" | ~2 h | phase 1 (small) + phase 6 (full) |

**Agent recommendation (not a decision):** if the budget only stretches to three,
take **C1 (deadlines/exam countdown)**, **C2 (class-gap earning advisor)** and
**C12 (permission/battery health)** — C1 because a student's day is shaped by
fixed dates, C2 because it is the only feature no existing app can copy without
his own rate data, and C12 because without it a silent OEM revoke looks exactly
like a broken app. **C3, C6 and C7** are the strongest behaviour-change additions
if he wants the app to actively protect his evenings and mornings.

**Deliberately not proposed** (and why): automatic reading of ProRewards/OfferPro
notifications to auto-log earnings — brittle, reads another app's content, and
R25's two taps are cheaper than the breakage; a pomodoro mode — R19 focus sessions
already cover it; GPS/attendance tracking — needs location, which the permission
set forbids; any social/leaderboard feature — one user, and a second reward loop
would compete with his real goal.

## 12. What only the owner can answer (not question batches — fill when convenient)

These do not block phase 0–1; they sharpen R2/R3/R25. Defaults are used until
answered, and every default is logged as a reversible agent decision.

1. Which exact apps are **earning** (ProRewards, OfferPro — any other offerwall or
   coin-earn game package names?), which are **waste**, and which must **never**
   be blocked (UPI, dialer, college apps)?
2. His real **daily ceilings and floors**: Waste ≤ ? min · Study ≥ ? min ·
   Earning ≥ ? min · total screen time target ? h.
3. Typical college timetable (days + hours) so the *College day* template is real
   rather than invented.
4. Notification **tone** he actually wants (one example sentence he would not hate
   reading at 21:00), the daily cap, and his quiet hours.
5. Device make/model + Android version (drives the OEM permission/battery
   verification pass in phase 1).
6. Is ₹12.50 per install the only rate, or does it vary by offer/task type?
