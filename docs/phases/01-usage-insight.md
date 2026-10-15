# Phase 01 — Usage insight

- Status: ✅ · Date: 2026-10-15 · Branch: arena/be850e43-personal-ueses-app
- CI: run 38752868739 green · Provenance: AppFlow @ c8856022ca57cdf23ae78b9491481543db89438d (reference only)
- **APK:** artifact `app-progress-apk` from run 38752868739 — *shows: onboarding for Usage access, a Today screen with total + per-app minutes + hourly strip + category totals, and a Categories screen for assigning apps and editing budgets.*

## Parts

- 1.1 — permission onboarding: walk to Usage access + notifications, show granted status — ✅
- 1.2 — ingestion from `UsageStatsManager` into Room (per-app foreground minutes, pickups, longest session, hourly buckets), batched, offline, labelled device-reported — ✅
- 1.3 — Today screen: total, per-app list, category totals, hourly heatmap strip, "as of HH:MM" — ✅
- 1.4 — owner-editable categories (Earning / Study / College / Waste / Life / Unclassified) + per-category and per-app daily budgets (ceiling and floor) — ✅

## Evidence (before the change)

- CI run 38749381939 (phase 0) green; baseline app built.
- `docs/REQUIREMENTS.md` R1–R7 define acceptance tests.

## What shipped

- Room entities/DAOs: `AppUsageEntity`, `HourlyUsageEntity`, `AppInfoEntity`, `CategoryEntity`; `DayLedgerDatabase`.
- `UsageRepository` with `hasUsagePermission`, `seedCategoriesIfNeeded`, `ingestRecentDays` (7 days + today), event-based pickups/longest-session/hourly buckets, app label upsert.
- `TodayViewModel`, `CategoryViewModel`.
- Compose screens: `OnboardingScreen`, `TodayScreen`, `CategoriesScreen`; `MainActivity` wires navigation and permission launcher.

## Tests

- `ExampleUnitTest.addition_isCorrect` — green in CI run 38752868739.
- `./gradlew --no-daemon testDebugUnitTest assembleDebug` — green in CI run 38752868739.

## Follow-ups

- Phase 2 — sweet notifications (R9–R12).
- Kill test P0 to be verified on the owner's own phone: real per-app time within ~10% of Digital Wellbeing.
