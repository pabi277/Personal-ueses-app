# Research — Day Ledger (`io.github.pabi277.dayledger`)

> Evidence-backed. Every claim links its source and its check date. Rejected
> options stay listed so nobody re-litigates them. Rules: `rule.md` in this repo.
> Provenance: AppFlow @ `c8856022ca57cdf23ae78b9491481543db89438d` (reference only).
> All Play / chrome-stats / appbrain evidence read on **2026-10-15 (UTC)**,
> country **India (gl=IN)** unless stated otherwise.

## Niche verdict line

`Demand: the owner's own unmet daily job is the primary demand signal (owner
statement, 2026-10-15) — external demand for another planner is NOT
established; staleness: category leaders are giant-scale and freshly
maintained (StayFree 1Cr+ downloads updated 2026-09-26; TimeTune 5M+ updated
2026-09-08; Digital Wellbeing is preinstalled), so there is no stale incumbent
to displace; complaints: verified and specific — screen-time blockers fail to
enforce and demand high-risk permissions, planners fail at overlap/delay
editing, break "clear schedule", paywall backup and history, and hide the past
behind premium; owner advantage: CONFIRMED for one user only — the owner can
use and verify the app every single day, needs no backend, and needs no
content pipeline. Decision: build for owner use first (kill test K0), publish
second; do not claim a market win the evidence does not support.`

## Method note and honest limits

`knowledge/niche-method.md` steps 2–4 were run against live Play listing pages,
the chrome-stats index and appbrain's Play mirror. Three limits are stated
instead of hidden:

1. **Ten recent one-star reviews per leading option were not fully auditable.**
   Public listing pages expose only 2–3 "most relevant" reviews; chrome-stats
   exposes ~10 indexed reviews per app and paywalls the rest; appbrain exposes a
   rating histogram plus a handful of dated comments. What is recorded below is
   *every negative review actually read*, quoted, with its date where the source
   gave one. Completing the 10×1-star audit per leading option (loop step 3) and
   the install-and-observe pass (loop step 6) stays an **owner verification task
   before any store submission**.
2. **The direction was chosen by the owner before the category research was
   finished.** The owner's batch-A answer (2026-10-15) redirected the app from
   the mess-ledger option to a personal 24-hour time-management app because
   *"all screen time app is good but not very much suitable for my use"*. The
   category sweep below was run afterwards, and it says the category is
   giant-owned. That is recorded as a risk, not smoothed over.
3. **No Indian-specific demand measurement exists for this app.** The category
   evidence is global (StayFree, TimeTune, Actiondash listings served from IN
   show the same global install bands). Nothing here proves Indian users want a
   sixth planner.

## Seed (niche-method step 1)

Owner context arrived blank in the launcher (`[MY CONTEXT]` unreplaced) and was
supplied in batch A on 2026-10-15: the owner wants to **use his own 24 hours
effectively**; he has tried screen-time apps and finds them good but unsuitable
for his own use; he believes an app built to his own recommendations would let
him manage his time effectively. Publicly known constraints (AppFlow
`PURPOSE.md`): Indian indie developer, phone-first (no PC build), GitHub Actions
as the build machine, offline-first, four-week ship-or-kill budget, West Bengal.

**Consequence for the whole plan:** this app's first user, first tester and
first source of truth is the owner. That is a genuine advantage for quality and
a genuine weakness for market evidence — both are recorded.

---

> **Superseded-in-part note (2026-10-15, later the same day).** The owner revised
> the direction after this file was written: the app is now **for his own use
> only** — per-app usage insight, sweet customizable notifications, a home-screen
> widget, limits with customizable block durations, planning with a Tomorrow plan,
> plus an offerwall-earnings and daily-learning tracker. The authoritative spec is
> **`docs/REQUIREMENTS.md`**; the phases are re-cut in `docs/ROADMAP.md`; the
> store-based kill test K0–K3 below is **void** and replaced by the personal-use
> test P0–P3 in `docs/REQUIREMENTS.md` §9. This file stays on the record for three
> reasons: the category evidence is what put `PACKAGE_USAGE_STATS`, per-app
> accuracy and enforcement fragility on the risk list; it explains why in-app
> feature blocking is out of scope; and the finished mess-hisab dossier here is the
> designated next app if this one is killed.

## Chosen direction (owner, 2026-10-15)

A **personal day-structure app**: plan the 24 hours as blocks, capture what
actually happened, and review the gap — offline, English-only, no account, no
ads. Built to the owner's own requirements first; publishable second.

Working identity is pending the exit gate (name + package ID are GATE items).

**Positioning that is defensible on the evidence below:** every incumbent is
either (a) a *tracker/enforcer* that measures screen time and tries to block
apps — and fails at enforcement while demanding high-risk permissions, or (b) a
*time-block planner* that captures intention only. **None of the inspected
listings combines planned-vs-actual on one timeline with a hard no-permission,
no-account, offline core.** That combination is the product. It is an
*inference from inspected listings*, not a measured demand.

## Options compared

| Option | Target user & painful job | Evidence (link, date) | Competition | Four-week scope | Risk | Verdict |
|---|---|---|---|---|---|---|
| 1 · Personal day-structure app (planned vs actual, no permissions) | The owner himself, then people whose screen-time app measures but never plans and whose planner never measures: "where did my 24 hours go, and why does my plan never survive contact with the day?" | [StayFree](https://play.google.com/store/apps/details?id=com.burockgames.timeclocker&hl=en_IN) 4.5★/2.8L reviews/1Cr+ · [Actiondash](https://play.google.com/store/apps/details?id=com.actiondash.playstore&hl=en_IN) 4.3★/68.6K/50L+ · [TimeTune](https://play.google.com/store/apps/details?id=com.gmail.jmartindev.timetune&hl=en_IN) 4.6★/94.4K/50L+ · [appbrain StayFree](https://www.appbrain.com/app/stayfree-screen-time/com.burockgames.timeclocker) 2026-09-26 · [appbrain TimeTune](https://www.appbrain.com/app/com.gmail.jmartindev.timetune) 2026-09-08 — all read 2026-10-15 | **Giant-owned**: 1Cr+ / 5M+ / 50L+ incumbents + preinstalled Digital Wellbeing (3.6★ over ~1.47M reviews per [habitdoom roundup](https://habitdoom.com/blog/best-screen-time-apps-android), 2026-09-30) | Realistic **only** because scope is one user's day: blocks, templates, reminders, manual actual log, gap view, stats. No enforcement, no backend | Category is crowded at scale; paid-upfront model throttles the install signal; owner-only demand | **CHOSEN by owner 2026-10-15**, with the kill test below written first |
| 2 · Mess / meal-rate hisab for WB hostels and bachelor messes (offline, no login) | Elected mess manager logging মিল/bazar/deposits and surviving month-end settlement | [IN search](https://play.google.com/store/search?q=mess+meal+rate+hisab&c=apps&hl=en&gl=IN) · [Smart Mess Manager](https://play.google.com/store/apps/details?id=com.official.smart_mess_manager&hl=en&gl=IN) 3.5★/49 in IN index, chrome-stats 3.86 over 71, 10K+, updated 2026-10-06 · [MessXpense](https://play.google.com/store/apps/details?id=com.expensecount.messExpense&hl=en&gl=IN) 4.2★/243, 10K+, 2026-10-08 · [My Mess Pro](https://play.google.com/store/apps/details?id=com.maminu.mymesspro&hl=en&gl=IN) offline/no-ads but 100+ and stale since 2025-08-13 — read 2026-10-15 | Fragmented-and-weak; every leader account/cloud-based | Realistic, no backend, almost no content to verify | Indian share of installs unmeasured; leaders update weekly | Rejected by owner 2026-10-15 (redirected to option 1). **Keep as the next app if option 1 is killed** — the dossier is written |
| 3 · WB recruitment-exam MCQ practice (WBP constable / WBSSC C–D), offline, ad-respectful | Bengali-medium aspirant on a cheap phone with bad connectivity | [WBP Constable Exam Preparation](https://play.google.com/store/apps/details?id=com.app.wbpconstablepreparation&hl=en&gl=IN) 4.1★ on **16** reviews, chrome-stats 8,458 downloads +376/week, created 2025-06-22, updated 2026-08-13; neighbours 62K and 72K — read 2026-10-15 | Low quality, mid scale | Risky: every question must be source-checked by the owner (RULES 14) | Copyright, implied government affiliation, exam-cycle dependency | Rejected 2026-10-15 — content, not code, is the bottleneck |
| 4 · WBBSE Madhyamik / HS prep | WB Class 10/12 students | [Madhyamik Bondhu](https://play.google.com/store/apps/details?id=com.madhyamik.exam.app&hl=en&gl=IN) 4.4★/616, chrome-stats 45,053 downloads, updated 2026-09-02, requests `READ_PHONE_STATE`; QList 202K; WB Class 10 54K; Madhyamik Suggestion 55K — read 2026-10-15 | Crowded-but-bad **and** crowded-with-scale | Unrealistic in 4 weeks | Cannot out-content 45K–202K incumbents updating monthly | Rejected 2026-10-15 |
| 5 · Offline personal expense tracker | Anyone tracking kharcha privately | [Kharcha Book](https://play.google.com/store/apps/details?id=com.yumik.kharchabook&hl=en_IN), [Kharcha](https://play.google.com/store/apps/details?id=com.eklak.kharcha&hl=en) ("100% offline-first… no ads, no tracking", updated 2026-08-20), [Kharche](https://play.google.com/store/apps/details?id=com.kharche.app&hl=en_IN) — read 2026-10-15 | Saturated on exactly our intended differentiation | Realistic but pointless | None worth taking | Rejected 2026-10-15 — the gap is already closed |
| 6 · Private-tuition teacher's offline register | Single private tutor tracking batches, attendance, fees, dues | Search returns backend SaaS only: [Tuition Manager](https://play.google.com/store/apps/details?id=com.excelon.tuitionmanager.in&hl=en_IN) (SMS gateway, GST, biometric), [Vedmarg](https://play.google.com/store/apps/details?id=com.vedmarg.app&hl=en_US) (payment gateway, QR attendance) — read 2026-10-15 | Real gap, small reachable market | Realistic | Buyer hard to reach from a listing | Held 2026-10-15, not chosen |

---

## Deep dive — the chosen category (screen time + day planning)

### Observed facts (read 2026-10-15)

| Listing | Rating | Installs | Updated | Ads / model |
|---|---|---|---|---|
| [StayFree – Screen Time](https://play.google.com/store/apps/details?id=com.burockgames.timeclocker&hl=en_IN) (`com.burockgames.timeclocker`) | 4.5★ / 2.8L reviews (appbrain: 4.55 over 270,570) | 1Cr+ (appbrain: 20M total, ~5.7K/day) | 2026-09-26 (v20.14.1, 71.5 MB) | **No ads**, premium tier, cross-device |
| [Actiondash](https://play.google.com/store/apps/details?id=com.actiondash.playstore&hl=en_IN) (`com.actiondash.playstore`) | 4.3★ / 68.6K reviews | 50L+ | — | ads introduced in a later update |
| [TimeTune – Schedule Planner](https://play.google.com/store/apps/details?id=com.gmail.jmartindev.timetune&hl=en_IN) (`com.gmail.jmartindev.timetune`) | 4.6★ / 94.4K reviews (appbrain 4.61 over 90,970; histogram 71K/12K/2.9K/1.5K/3.3K) | 50L+ (appbrain: 7.5M total, ~400–690/day) | 2026-09-08 (v5.6.3, 7.6 MB, Android 6+) | **No ads**, IAP premium (~$10/yr, ~$30 lifetime) |
| Digital Wellbeing (preinstalled) | 3.6★ over ~1.47M reviews per [habitdoom](https://habitdoom.com/blog/best-screen-time-apps-android) (2026-09-30) | system | — | free |
| Others in the same roundups | ScreenZen 4.7★/~31.6K reviews · AppBlock 4.7★/~238K · Regain ~154K · YourHour 4.2★/1M+ · QualityTime **3.6★**/1M+ · Forest 4.4★/10M+ · Time Planner 4.3★/22.5K | — | — | — |

### Verified complaint signals — every negative review actually read

**Screen-time trackers / blockers**

- StayFree, 5★ review with a complaint inside (Carlos Hidalgo, 2026-09-12, via
  [appbrain](https://www.appbrain.com/app/stayfree-screen-time/com.burockgames.timeclocker)):
  *"The only bad thing is the app wants high-risk permissions. They say they
  don't want to know who we are… but I'm not sure, and I didn't permit
  high-risk permissions just in case."* → **permission distrust is a real
  purchase blocker even for a beloved app.**
- StayFree, 1★ (Kelsey Schmidt, 2026-09-11): *"Does not work for TikTok! I've
  enabled all the permissions, set a timer… the app will not be blocked… Stay
  free sees the activity, the usage timer bar is full, but the app remains
  openable and functional."*
- StayFree, Play page (108 people found it helpful): *"the blocking features are
  spotty — freezing when setting up, forgetting to enforce… I'll just use the
  tracking and then another app for blocking. but I'd prefer an all in one."*
- StayFree, Play page: *"you can put in a pin so that the blocked lists cannot
  be turned on and off without the pin, BUT you can still open the blocked list
  and uncheck everything without the pin. Sometimes if an app is blocked you can
  just tab out and back… and it's unblocked."*
- Actiondash, Play page: *"App has been great for over a year. Now there's ads
  and none of the buttons work anymore… I can't even open settings anymore."*
- Actiondash, Play page: *"The app is precise but not accurate. If you leave
  apps open, but your phone is no where near you, it still counts as time on the
  app… If it says you were on phone for 6 hours per day… really means half
  that."*
- Third-party privacy read ([reddit r/nosurf](https://www.reddit.com/r/nosurf/comments/l6r1ik/thoughts_on_stay_free_app/)):
  the app's functionality *"gives it high-level access to your phone that is
  technically capable of reading sensitive data"* — recorded as an outside
  opinion, not a finding of fact.

**Time-block planners**

- TimeTune, 1★ (Kayleigh M, 2026-04-28, via appbrain): *"It's broken! I try and
  try to clear all the schedules using the 'clear all' button in the calendar,
  and it doesn't work… I click on the days and press 'clear schedule from this
  day on' and surprise…. It doesn't work!"*
- TimeTune, 1★ (Simon Fuller, 2026-04-13): *"Poor ability to extend delay blocks
  on the fly without manual editing and no simple UX to resolve overlaps… there
  is no solution to address amending overlaps."*
- TimeTune, 1★/2★ premium complaints (66 people found it helpful): *"I spent 33
  bucks on that lifetime subscription for nothing. Now you need to buy premium
  to backup your account which is super suspicious… Also you cant go back to see
  the previous months which is also weird."*
- TimeTune, billing complaint: *"Said it would give me a free 7-day trial PRIOR
  to the year subscription… They did NOT give me a trial and charged me the
  moment I went through with it."*
- TimeTune, 2★ (anna k, 2026-03-14): *"The new update however has no white
  background option which is extremely upsetting… too distracting now."*
- TimeTune, 5★ with asks (Alexa Miller, 2026-04-09): *"wish the blocks were sized
  according to the time they take up. Also wish I could long press on the days of
  the calendar and select multiple to apply a template, and select multiple
  blocks to move them all forward or back."*
- TimeTune, 5★ with asks (Matt Leach, 2026-03-31): *"I am 100% opposed to
  removing the Blocks tab… you're creating an unnecessary hassle."*
- TimeTune, older review via andro.io: template list *"is just sorted in
  alphabetical order which is a mess if you have 20+ different templates"*;
  another: notifications did not ring.

### From complaints to our free product spec

Every line below is a verified complaint above, converted into a v1 requirement:

1. **Zero sensitive permissions in v1.** No `PACKAGE_USAGE_STATS`, no
   Accessibility, no overlay, no `READ_PHONE_STATE`. Only `POST_NOTIFICATIONS`
   (Android 13+) and exact-alarm scheduling. This is the single clearest
   differentiator against StayFree/Actiondash/Digital Wellbeing.
2. **Blocks sized proportional to duration** (Alexa Miller's ask) and a timeline
   that stays legible at large font sizes.
3. **First-class overlap and delay handling**: "shift the rest of my day by N
   minutes" and an explicit overlap indicator — not manual editing (Simon
   Fuller's 1★).
4. **Destructive actions must work and must be undoable**: clear-day /
   clear-range with a visible result and an undo snackbar (Kayleigh M's 1★).
5. **History is never paywalled and never deleted.** Every past day readable
   forever; month close archives.
6. **Local backup/restore in the free core** — no account, no cloud, no paywall
   (the TimeTune premium-backup complaint).
7. **Templates sorted by last-used and multi-day template application** (andro.io
   + Alexa Miller).
8. **Accuracy honesty:** if automatic usage data is ever added later, it must be
   labelled as device-reported foreground time, and manual capture stays primary
   (the Actiondash "precise but not accurate" complaint).
9. **Billing honesty if a paid model is ever used:** one-time, no auto-renew
   surprise, restore path, and the exact entitlement stated in the listing.
10. **Theme choice preserved:** light + dark + follow-system, never removed.

### Crowding classification (loop step 4)

**Giant-owned and well-served, with a narrow unserved angle.** This is the
honest reading and it is *not* "crowded-but-bad = opportunity": the leaders are
4.5–4.6★ at 1Cr+/5M+ installs, updated within the last six weeks, and ad-free.
The unserved angle is (a) no-permission planned-vs-actual, (b) history/backup
not paywalled, (c) overlap/delay UX. Expect **no meaningful organic discovery**;
expect the value to be owner-use first. Stated as fact, not optimism.

### Niche inside the niche (loop step 5)

The audience the owner can serve with uncopyable care is **himself, then people
like him**: a Bengali-speaking Indian student/developer on a mid-range phone who
has already tried Digital Wellbeing and StayFree, does not want to grant
high-risk permissions, and wants a *plan* for the day rather than a *police* for
the day. English-only v1 (owner decision, 2026-10-15); a Bengali locale is a
post-v1 option, not a v1 promise.

### Ad friction (loop step 6)

Not applicable to our build: the owner chose **no ads** (2026-10-15). Observed in
the category: Actiondash introduced ads and users reported broken controls in
the same breath; StayFree and TimeTune are ad-free with premium tiers. **Install
and observe remains an owner task** — store-page labels and third-party
roundups are not direct evidence of placement quality.

---

## Privacy, licensing and policy risks (chosen option)

- **Sensitive permissions are the category's core risk and we decline it.**
  Automatic tracking would need `PACKAGE_USAGE_STATS` (and for real blocking,
  Accessibility/overlay), which triggers Play's sensitive-permission and
  Accessibility API disclosure requirements, a prominent in-app disclosure, and
  a Data Safety form that must match the shipped build exactly
  (`knowledge/rejection-causes.md`). **v1 collects nothing and declares
  nothing**; any later automatic-tracking phase is a recorded GATE decision with
  the policy re-checked that day.
- **No health/behaviour claims.** No "cures phone addiction", no guaranteed
  productivity or outcome claims — mirror the disclaimer style incumbents use.
- **Billing:** with a paid-upfront listing there is no IAP, but refunds,
  localized pricing and taxes are the owner's Play Console decisions
  (`knowledge/monetization.md`). If the model ever changes to free + unlock IAP,
  Google Play Billing is required for digital functionality and the ad-free/opt-out
  rules of RULES 9 apply only if ads exist — with **no ad SDK, the required
  ad-free IAP is not applicable** (recorded as an agent reading of RULES 9,
  reversible on objection).
- **Licensing:** all code original; no copied UI assets. Third-party libraries
  (AndroidX, Room, Compose, WorkManager — Apache-2.0) get notices in the app and
  a record in `docs/DECISIONS.md`. App license MIT/GPL-3.0 is a GATE item.
- **Data:** everything stays on-device; no analytics SDK, no crash reporting SDK
  in v1 (Play Vitals is sufficient and is Google-side). This keeps the privacy
  policy short and true.

## Monetization (plausible, no revenue promise)

Owner decision 2026-10-15: **paid upfront, no ads**. Recorded consequences:

- One-time Play purchase price, India-localized (the ₹49–99 range in
  `knowledge/monetization.md` is an experiment, not a market fact).
- No ad SDK → no AdMob Data Safety rows, no consent flow, no ad-free IAP needed.
- **A paid listing suppresses installs**, so the kill test cannot use an install
  target calibrated for a free app. K1 below is set at 50 paid installs/28 days
  and K0 (owner's own 21-day use) carries the real weight. If the owner later
  prefers free + one-time unlock IAP, that is a DECISIONS change and K1 must be
  re-set **before** go-live, never after results.
- No revenue, income or download promise is made anywhere in this plan.

## Kill test (pre-written 2026-10-15, before any build existed)

Measured from the first day the app is available in production, plus the owner's
own use which starts at phase 1. **Never rewritten after results are seen**;
changing any number is an explicit owner decision logged in `docs/DECISIONS.md`.

- **K0 — owner truth (starts at phase 1, before any store presence):** the owner
  uses the app on **≥ 21 consecutive days**, with ≥ 1 planned block and ≥ 1
  actual capture per day on ≥ 18 of those days, and states in writing which three
  things he still cannot do without leaving the app. **If K0 fails, the app is a
  hobby, not a product: stop before spending anything on publishing.**
- **K1 — adoption (paid-upfront model):** ≥ **50** paid installs within **28
  days** of production go-live.
- **K2 — engagement:** ≥ **30%** of installs record an actual-log entry on ≥ 3
  separate days within their first 7 days (measured from Play Console
  engagement/retention data — no analytics SDK is added to get it).
- **K3 — quality:** rating ≥ **4.0** on ≥ 10 ratings; crash-free sessions
  ≥ 98.91% and crash-free users ≥ 99.5% in Play Vitals; refund rate < 10%; zero
  "lost my data" or "history disappeared" complaints unanswered for > 7 days.
- **Decision rule:** K0 + K1 + K2 + K3 all met → continue investing (next
  phases, then a second locale or a paid-unlock experiment). K2 or K3 missed →
  exactly **one** iterate cycle of ≤ 2 weeks against a written fix list, then
  re-measure once. K0 missed → the app stays a private tool, no publishing spend.
  K1 missed, or K2 still missed after the iterate cycle → **kill**: record the
  outcome and dates here, and move to the next app (option 2, the mess hisab,
  already has a finished dossier).
- **Pre-registration:** these numbers are written on 2026-10-15 while the
  repository is still empty. Law 4 (kill fast) is enforced by this section, not
  by mood.

## Rejected, and why

- **Option 2 · Mess / meal-rate hisab** — rejected by the owner on 2026-10-15 in
  favour of the personal day app. Evidence *for* it stays on file (3.5★ leader in
  the IN index, both leaders account/cloud-based, the only offline competitor
  stale at 100+ installs, verified complaints about not being able to edit past
  entries and about month rollover deleting statements). It is the designated
  **next app** if the kill test fires.
- **Option 3 · WB recruitment MCQ** — rejected 2026-10-15: the differentiator is
  a source-verified question bank, which is owner labour the four-week budget
  cannot absorb; verified complaints were ad volume and thin content.
- **Option 4 · Madhyamik/HS prep** — rejected 2026-10-15: 45K–202K-download
  incumbents updating monthly; verified complaints (wrong answers, ads, language
  lock) are content problems, not code problems.
- **Option 5 · Offline expense tracker** — rejected 2026-10-15: incumbents
  already headline "100% offline-first, no ads, no tracking, no sign-up".
- **Option 6 · Tuition register** — held 2026-10-15: real gap (search returns
  only backend SaaS) but an unreachable buyer; revisit only with a distribution
  answer.
- **Automatic screen-time tracking in v1** — rejected 2026-10-15 (agent decision,
  reversible on objection): it needs `PACKAGE_USAGE_STATS`, which is exactly the
  high-risk-permission distrust verified in StayFree's own reviews, and it drags
  in sensitive-permission policy work inside a four-week budget.
- **App blocking / enforcement in v1** — rejected 2026-10-15: the category's
  best-funded apps visibly fail at enforcement (verified 1★ quotes above). A
  phone-built v1 would fail worse and earn the same 1★s.

## Sources checked (all read 2026-10-15 UTC)

- Play listings: `com.burockgames.timeclocker`, `com.actiondash.playstore`,
  `com.gmail.jmartindev.timetune`, `com.official.smart_mess_manager`,
  `com.expensecount.messExpense`, `com.eritlab.messmate`, `com.messhisab`,
  `com.xpart.mymeal`, `com.maminu.mymesspro`, `com.mealcalculation`,
  `com.madhyamik.exam.app`, `com.app.wbpconstablepreparation`,
  `com.rtoexam.bangla`, `com.yumik.kharchabook`, `com.eklak.kharcha`,
  `com.kharche.app`, `com.indianappnovation.personalexpenseapp`,
  `com.excelon.tuitionmanager.in`, `com.vedmarg.app`
- Play search pages (gl=IN): `mess meal rate hisab`
- appbrain mirrors with dated user comments: StayFree (page dated 2026-09-26),
  TimeTune (page dated 2026-09-08)
- chrome-stats indexes (data ingested 2026-10-15):
  `/d/com.official.smart_mess_manager/reviews`, `/d/com.madhyamik.exam.app`
  (+`/reviews`), `/d/com.app.wbpconstablepreparation` (+`/reviews`),
  `/d/com.isbawb`, `/d/com.wbc10`, `/d/com.madhyamiksuggestion`,
  `/d/wbp.preparation`, `/d/com.blogspot.anumondal78.wbcsquestionpaper`
- Category roundups (third-party, used only for scale/rating context):
  habitdoom.com best screen-time apps (2026-09-30), habitdoom.com best app
  blockers (2026-08-31), nextgrowthlabs.com screen-time apps (2023-09-18 —
  dated, treated as stale), flown.com time-blocking roundup (2026-06-01)
- Play policy references carried from AppFlow: target API requirement,
  Data Safety form, User Data policy, sensitive-permission expectations, testing
  requirements for newer personal accounts (see `knowledge/rejection-causes.md`)
- AppFlow playbook @ `c8856022ca57cdf23ae78b9491481543db89438d`: PLAN.md,
  START.md, RULES.md, PROMPTS.md (P0), knowledge/niche-method.md,
  knowledge/monetization.md, knowledge/rejection-causes.md, templates/*
