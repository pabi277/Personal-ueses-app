# Research draft (pre-gate) — new Android app for Indian users

> PLAN.md stage-1 working paper. **Not** yet an app repo file: no repository,
> branch, commit, or app code was created. Owner direction pick is pending
> (batch A). Pinned playbook: AppFlow @
> `c8856022ca57cdf23ae78b9491481543db89438d` (latest `main` at session start;
> `<SHA>` arrived unreplaced in the launcher).
> All Play/chrome-stats evidence below was read on **2026-10-15 (UTC)**,
> country **India (gl=IN)** unless stated.

## Method note and honest limits

`knowledge/niche-method.md` steps 2–4 were run with live Play listing pages and
the chrome-stats index. Two limits are stated instead of hidden:

1. **Ten recent one-star reviews per leading option could not be fully
   audited.** Public listing pages expose only 2–3 "most relevant" reviews;
   chrome-stats exposes the first ~10 indexed reviews per app and paywalls the
   rest. What follows is therefore *every negative review I could actually
   read*, quoted, plus each app's rating distribution where published. The
   10×1-star audit per leading option stays an **owner verification task in
   Play (step 6 of the loop: install and observe)** before code.
2. **Installs are not geography.** Play shows download bands, not country
   splits. Several mess/meal apps are published from Bangladesh; whether their
   installs are Indian is unmeasured. Stated as uncertainty, not assumed.

## Seed (niche-method step 1)

Owner context arrived **blank** (`[MY CONTEXT]` / `[AREA, IF ANY]` unfilled), so
the seed is what is publicly known and is itself a question to the owner:
Indian indie developer, phone-first workflow, West Bengal (Bālurghāt), Bengali +
English, no backend, four-week ship-or-kill budget (AppFlow `PURPOSE.md`,
`RULES.md`). Nothing below assumes an advantage the owner has not confirmed.

---

## Option 1 — Mess / meal-rate hisab for WB hostels, messes and bachelor flats
**Offline-first, no-login, Bengali + English, editable history, month close → PDF/WhatsApp**

- **Target user & painful job:** the elected *mess manager* (a student or
  bachelor) in a shared mess/hostel/flat who must log everyone's daily meals
  (মিল), bazar cost, deposits and fixed bills, then survive the month-end
  argument over "who owes what". Today it is a notebook, a calculator, or an
  Excel-style app — and every existing app either needs accounts/internet or
  refuses to let you fix a wrong entry.
- **Exact search phrases used:** `mess meal rate hisab`, `mess manager meal
  tracker` — Play, country IN, 2026-10-15.
  ([search page](https://play.google.com/store/search?q=mess+meal+rate+hisab&c=apps&hl=en&gl=IN))
- **Observed facts — top listings (read 2026-10-15):**

| # | Listing | Rating | Downloads | Updated | Model observed | Developer |
|---|---|---|---|---|---|---|
| 1 | [Smart Mess Manager](https://play.google.com/store/apps/details?id=com.official.smart_mess_manager&hl=en&gl=IN) | 3.5★ / 49 reviews in the search index; chrome-stats total **3.86** over 71 ratings, 1★ = 6% (3), 2★ = 4% (2), 3★ = 4% (2) | 10K+ | **2026-10-06** | cloud sync, real-time member sync, premium IAP | RonyCode Studio (Bangladesh) |
| 2 | [MessXpense](https://play.google.com/store/apps/details?id=com.expensecount.messExpense&hl=en&gl=IN) | 4.2★ / 243 reviews | 10K+ | **2026-10-08** | invite link/code → accounts | ExpenseCount Inc (Canada) |
| 3 | [Mess Mate](https://play.google.com/store/apps/details?id=com.eritlab.messmate&hl=en&gl=IN) | no rating block rendered | 1K+ | 2026-09-19 | cloud + web app, join code, email invoices, "no ads" | ERIT LAB (Bangladesh), listing targets Dhaka/Chattogram/… |
| 4 | [Mess Hisab – মেস হিসাব](https://play.google.com/store/apps/details?id=com.messhisab&hl=en&gl=IN) | no rating block rendered | 1K+ | 2026-08-30 | "first person who signs up becomes manager" | MicroAppValley (Bangladesh) |
| 5 | [My Mess](https://play.google.com/store/apps/details?id=com.xpart.mymeal&hl=en&gl=IN) | no rating block rendered | 500+ | 2026-05-21 | contains ads, account + manager role; **"opening a new Mess calculation will delete all existing Mess statements"** (its own description) | Xpart IT (Bangladesh) |
| 5b | [My Mess Pro: Tracker Free](https://play.google.com/store/apps/details?id=com.maminu.mymesspro&hl=en&gl=IN) | no rating block rendered | **100+** | **2025-08-13** | the only offline/no-ads/no-login one found: SQLite on device | All dream BNG (Bangladesh) |
| 5c | [Meal Calculation](https://play.google.com/store/apps/details?id=com.mealcalculation&hl=en_US&gl=US) | — | — | 2024-08-22 | web + Android, Excel-mirroring | Redwan Hossain |

  Also visible in the same IN search: Meal Khata, Meal Rate – Mess & Meal
  Manager, Meal Calculator (DevTripleS), Meal Manager (Bari Labs), Khabar
  Manager, Mess Manager – Meal Tracker (QualitySoft BD); chrome-stats lists more
  sub-1K siblings (Mess Manager: Meal Tracker, MessMate – The Mess Manager, Meal
  Sheet, Mess Bhai, Bachelors' Meal Manager).
- **Verified review signals (read, not inferred):**
  - Smart Mess Manager (chrome-stats review summary, read 2026-10-15): *"The
    most repeated requests are for editing past meals and market entries. A few
    users also report glitches or incorrect meal counts."* A quoted Bengali
    review: *"অপ app … আরও আপডেটের দরকার অনেক গ্লীচ/বাগ আছে। আর মিল on off করার
    ওখানে ফন্ট একদম ছোট সূক্ষ্ম ভাবে টাচ না করলে মিল প্লাস বা মাইনাস হয়না"* — the
    meal +/− touch target is so small you mis-tap. Another quoted review asks
    for independent editing of each month's start/end date.
  - MessXpense (Play page reviews): asks for purchase-bill upload, ration stock
    carry-forward from month to month, and a manager role; and a bug report:
    *"The list of participant's names under fields like 'who paid' is just
    blank."*
  - My Mess: month rollover **deletes** prior statements (developer's own text).
- **Crowding classification:** *fragmented-and-weak*, not empty and not
  giant-owned. Long tail of sub-1K apps, two leaders at 10K+ (one at 3.5★ in
  the IN search index), **every** leader account/cloud-based, and the one
  offline/no-login competitor at 100+ installs and stale since 2025-08-13.
- **Free product spec handed to us by competitors' users:** edit/delete any past
  meal + bazar + deposit entry; big touch targets for meal +/−; editable month
  start/end; carry-forward balance and ration stock; no sign-up; month close
  that never destroys history; PDF/WhatsApp share of the monthly hisab.
- **Owner advantage (only if confirmed):** lives in the mess/hostel culture he
  would serve; can have 3–10 real mess managers verify the calculation model and
  the Bengali wording; no backend needed — which fits a phone-only developer
  (law 13).
- **Distinct offline-first v1 (≈4 weeks):** one phone, one mess, no internet, no
  login. Members + daily meal grid (big ±), bazar/expense entries, deposits,
  fixed costs, month with editable dates, auto meal rate = total cost ÷ total
  meals, per-member due/refund, month close that archives (never deletes),
  local encrypted backup/restore, and a shareable monthly summary (PDF/image or
  plain text for WhatsApp). Bengali-first UI with English fallback.
- **Privacy / licensing / policy risk:** low. No SDKs that collect data if no
  ads at first; if AdMob is added, Data Safety must declare it and the ad-free
  IAP must be offered (RULES 8–9). No copyrighted content, no government
  affiliation claims, no finance-product claims (it is a calculator/ledger, not
  a lending or banking app — mirror the disclaimer style used by KharchaBook).
- **Plausible monetization (no revenue promise):** one primary model — free core
  with an optional **rewarded** ad to export/unlock extra months or a second
  mess, plus the required one-time **ad-free purchase** (₹49–99 experiment).
  Or, if the owner prefers: paid-upfront-free-of-ads with no ad SDK at all.
- **Uncertainty:** Indian-side demand is inferred from a shared-language market
  and from the fact that these listings rank in an IN search — not measured.
  10K+ bands may be mostly Bangladeshi installs.

## Option 2 — WB recruitment-exam MCQ practice (WBP constable / WBSSC Group C-D), offline, ad-respectful

- **Target user & painful job:** a Bengali-medium aspirant in a small WB town
  preparing for one *specific* recruitment exam who wants syllabus-aligned MCQ
  practice on a cheap phone with bad connectivity, and is currently served by
  ad-heavy apps with thin question banks.
- **Exact search phrases:** `WBP constable exam preparation`, `WBSSC group C and
  D` — Play, IN, 2026-10-15.
- **Observed facts:**
  - [WBP Constable Exam Preparation](https://play.google.com/store/apps/details?id=com.app.wbpconstablepreparation&hl=en&gl=IN)
    — 4.1★ / **16** reviews, 5K+ band; chrome-stats: **8,458** downloads,
    **+376 in one week**, created 2025-06-22, updated 2026-08-13, publisher IN.
    Its two visible negative reviews: *"Very bad app gives huge advertise
    😡😡😡😡"* (4 helpful) and *"bad app,,,,, don't download,,few questions are
    here,,,,,"* (3 helpful). chrome-stats indexes only **6** reviews total.
  - Same developer ("Mild Owner") ships a family of near-identical exam apps
    (WBPSC Clerkship, GK in Bengali 3.8★, মাধ্যমিক সাজেশন 3.9★, SSC GD 4.4★,
    ICDS 3.9★, GNM/ANM 3.9★) — a repetitive-content pattern to *not* copy
    (RULES 5).
  - Category neighbours: [Wb Police Pril+Main Exam guide](https://chrome-stats.com/d/wbp.preparation)
    62K downloads, [Mock Test WBPSC](https://chrome-stats.com/d/com.blogspot.anumondal78.wbcsquestionpaper)
    72K, WBCS Exam Preparation 2026 4K, WBP/KP Math & GK Guide 2K.
- **Verified complaint themes:** excessive ads, too few questions, no offline
  guarantee (several competitors describe themselves as online-first: *"It is an
  online app, so we can easily add a question"*).
- **Inference (clearly marked):** demand exists (tens of thousands of installs
  across the family), quality is low, and the differentiator would be a small,
  **owner-verified** question bank with zero mid-task ads. That inference rests
  on 6–16 readable reviews — thin.
- **Owner advantage:** only real if the owner can source-check every question
  against official material (PRB/WBPSC notifications, previous-year papers).
  RULES 14 makes content accuracy the owner's job; an agent-written bank is a
  defect, not a shortcut.
- **Risks:** copyright on copied question banks; implied government affiliation
  (must carry a disclaimer like the incumbents'); exam-cycle dependency
  (downloads spike with notifications); Play repetitive-content policy if it
  becomes one of many near-identical listings.
- **Kill-test friendliness:** weaker — content production, not engineering, is
  the bottleneck, so a four-week v1 realistically ships one exam × one subject
  set.

## Option 3 — WBBSE Madhyamik / HS practice and suggestions (Bengali + English)

- **Exact phrase:** `madhyamik suggestion` / `madhyamik mcq` — Play, IN,
  2026-10-15.
- **Observed facts (crowding is the finding):**
  - [Madhyamik Bondhu 1700 MCQ Quiz](https://play.google.com/store/apps/details?id=com.madhyamik.exam.app&hl=en&gl=IN)
    — 4.4★ / 616 reviews, contains ads + IAP; chrome-stats: **45,053** downloads,
    +219/week, version 23, updated 2026-09-02, created 2020-03-20, publisher IN,
    and it requests `READ_PHONE_STATE` ("very high risk" per chrome-stats).
  - [West Bengal : Exam Prep QList](https://chrome-stats.com/d/com.isbawb) —
    **202K** downloads; [WB Class 10: Exam Preparation](https://chrome-stats.com/d/com.wbc10)
    — 54K; [Madhyamik Suggestion : WB 10](https://chrome-stats.com/d/com.madhyamiksuggestion)
    — 55K; plus Safollo, ছাত্র-ছাত্রী (updated 2026-04-02), EduTips, Alpha MCQ,
    Happu MCQ, Madhyamik MCQ (9K), Madhyamik Master.
- **Verified review signals for the leader** (chrome-stats index, 69 reviews,
  first ~10 readable): *"this is one of the worst app I have tried"*;
  *"it is not in english and we are forced to give a star to this app"*;
  *"notes nai"*; *"language cannot be changed"*; a Bengali teacher's list ending
  *"❌ দু তিনটে প্রশ্নের উত্তর ভুল মনে হচ্ছে 👀 বিজ্ঞাপন আসছে 📕 সাজেশান এখনো আসেনি"*
  (2–3 answers seem wrong, ads arrive, suggestions not out yet); a long Bengali
  review asking for 2/3/5-mark long questions, not only MCQs; a developer-site
  testimonial admitting *"ads were very distrupe me and any big questions didn't
  avalible here"*; Safollo review: *"some questions are incorrect answer"*.
- **Reading:** crowded-but-bad *and* crowded-with-scale — 45K–202K-download
  incumbents that update monthly. Entering means out-contenting them, which a
  four-week phone-built v1 cannot do. **Rejected unless the owner brings a
  verified content pipeline.**

## Option 4 — Offline personal "daily uses" tracker (kharcha / medicine / reminders), Bengali-first

- **Exact phrases:** `kharcha tracker offline`, `personal expense offline` —
  Play, IN, 2026-10-15.
- **Observed facts:** the differentiation we would claim is already the
  incumbents' headline claim: [Kharcha Book](https://play.google.com/store/apps/details?id=com.yumik.kharchabook&hl=en_IN)
  ("Offline, Secure & Private… no sign-ups… encrypted backup"),
  [Kharcha](https://play.google.com/store/apps/details?id=com.eklak.kharcha&hl=en)
  ("100% offline-first… no subscriptions, no ads, no tracking", updated
  2026-08-20), [Kharche](https://play.google.com/store/apps/details?id=com.kharche.app&hl=en_IN)
  (Hinglish, local Hive storage), [Personal Expense-(Offline)](https://play.google.com/store/apps/details?id=com.indianappnovation.personalexpenseapp&hl=en_IN).
- **Reading:** no honest offline-first gap left in generic expense tracking; a
  new entry needs a *different job* (e.g. mess hisab = option 1, or a
  Bengali-first combined household ledger). **Rejected as a standalone.**

## Option 5 — Private-tuition teacher's offline register (batches, attendance, fees, dues), Bengali

- **Exact phrase:** `tuition teacher fees attendance app` — Play, IN,
  2026-10-15.
- **Observed facts:** results are dominated by backend SaaS:
  [Tuition Manager](https://play.google.com/store/apps/details?id=com.excelon.tuitionmanager.in&hl=en_IN)
  (web sign-up, SMS gateway, GST, biometric attendance, staff roles) and
  [Vedmarg](https://play.google.com/store/apps/details?id=com.vedmarg.app&hl=en_US)
  (payment gateway, QR attendance, school ERP). No simple offline
  one-teacher register surfaced in the top results.
- **Reading:** a genuine gap, but the buyer (a single private tutor) is hard to
  reach from a Play listing, the revenue base is small, and the job overlaps
  generic ledger apps. **Held as a later candidate, not this app.**

---

## Comparison

| | Option 1 · Mess hisab | Option 2 · WB recruitment MCQ | Option 3 · Madhyamik prep | Option 4 · Offline expense | Option 5 · Tuition register |
|---|---|---|---|---|---|
| Verified demand | 10K+ ×2 leaders, long tail | 8.5K (+376/wk) · 62K · 72K | 45K · 54K · 55K · 202K | present but saturated | unclear |
| Leader quality signal | **3.5★ in IN index / 3.86 total** | 4.1★ on 16 reviews | 4.4★ on 616 | 4.5+ typical | SaaS, not comparable |
| Complaints we can fix in code | edit past entries, tiny ± touch target, forced login/cloud, month rollover deletes data | ad volume, thin bank | wrong answers, ads, language lock, no long questions | none left | offline mode |
| Content the owner must verify | **almost none** (it is a calculator) | every question | every question/note | none | none |
| 4-week offline-first v1 | **realistic** | risky (content) | unrealistic | realistic but pointless | realistic but hard to sell |
| Backend needed | **no** | no | no | no | no |
| Policy/licensing risk | low | medium (copyright, affiliation) | medium-high | low | low |
| Monetization fit (rewarded + ad-free IAP) | strong (export/extra mess as reward) | medium | medium | weak | weak |
| Repeat use | **daily, month-cyclical** | exam-cyclical | exam-cyclical | daily | daily |

**Recommendation (not a decision — the owner picks, combines or refines):**
**Option 1**, with Option 2 as the fallback if the owner can personally commit to
source-checking a question bank. Option 1 is the only one where a phone-built,
four-week, no-backend v1 can be *better in ways users already wrote down*, and
where almost nothing depends on unverifiable content.

## Niche verdict line (draft, per niche-method step 7)

`Demand: mess/meal-rate category shows two 10K+ leaders ranking in an IN search
plus a long sub-1K tail — category demand observed, Indian share unmeasured;
staleness: leaders fresh (2026-10-06, 2026-10-08) but the only offline/no-login
competitor is stale (2025-08-13, 100+ installs); complaints: edit-past-entries,
tiny meal ± touch target, glitches/wrong meal counts, forced sign-up + cloud,
month rollover deleting statements — all verified quotes; owner advantage:
UNCONFIRMED — pending the owner's own mess/hostel access and Bengali quality
control. Decision: validate further (10×1-star audit in Play + 5 real mess
managers interviewed) before code.`

## Kill test (pre-written, before any results — never rewritten afterwards)

Measured from the first day the build is live in production (Play Console
statistics + one local, privacy-safe in-app counter; no analytics SDK):

- **K1 (adoption):** ≥ **500** installs within **28 days** of production go-live.
- **K2 (real use):** ≥ **25%** of installs create a mess with ≥ 3 members **and**
  ≥ 10 logged meal-days; ≥ **10%** close a month and open the next one
  (proves the recurring job, not a curiosity install).
- **K3 (quality):** rating ≥ **4.0** on ≥ 20 ratings, **zero** crash-affected
  sessions above 1.09% (Play vitals), and **zero** "cannot edit / lost my data"
  complaints unanswered for more than 7 days.
- **Decision rule:** K1–K3 all met → continue to the next phase (ads + ad-free
  IAP). K2 or K3 missed → one iterate cycle (max 2 weeks, one fix list).
  K1 missed **or** K2 still missed after the iterate cycle → **kill**: no further
  investment, next app; record the outcome in `docs/RESEARCH.md`.
- **Pre-registration:** these numbers are written on 2026-10-15, before any build
  exists, and may only be changed by an explicit owner decision logged in
  `docs/DECISIONS.md` — never after results are seen.

## Sources checked (2026-10-15 UTC)

- Play search `mess meal rate hisab`, gl=IN —
  https://play.google.com/store/search?q=mess+meal+rate+hisab&c=apps&hl=en&gl=IN
- Play listings: `com.official.smart_mess_manager`, `com.expensecount.messExpense`,
  `com.eritlab.messmate`, `com.messhisab`, `com.xpart.mymeal`,
  `com.maminu.mymesspro`, `com.mealcalculation`, `com.madhyamik.exam.app`,
  `com.app.wbpconstablepreparation`, `com.rtoexam.bangla`,
  `com.yumik.kharchabook`, `com.eklak.kharcha`, `com.kharche.app`,
  `com.indianappnovation.personalexpenseapp`, `com.excelon.tuitionmanager.in`,
  `com.vedmarg.app`
- chrome-stats indexes (data ingested 2026-10-15): `/d/com.official.smart_mess_manager/reviews`,
  `/d/com.madhyamik.exam.app` + `/reviews`, `/d/com.app.wbpconstablepreparation` + `/reviews`,
  `/d/com.isbawb`, `/d/com.wbc10`, `/d/com.madhyamiksuggestion`, `/d/wbp.preparation`,
  `/d/com.blogspot.anumondal78.wbcsquestionpaper`
- AppFlow playbook @ `c8856022ca57cdf23ae78b9491481543db89438d`: PLAN.md,
  START.md, RULES.md, PROMPTS.md (P0), knowledge/niche-method.md,
  knowledge/monetization.md, knowledge/rejection-causes.md, templates/*
