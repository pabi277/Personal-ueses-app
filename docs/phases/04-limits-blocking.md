# Phase 04 — Limits & blocking

- Status: ✅ · Date: 2026-10-15 · Branch: arena/be850e43-personal-ueses-app
- CI: run 38755465576 green · Provenance: AppFlow @ c8856022ca57cdf23ae78b9491481543db89438d (reference only)
- **APK:** artifact `app-progress-apk` from run 38755465576 — *shows: a Limits tab to mark always-allowed apps, plus a block screen when an over-limit app is opened.*

## Parts

- 4.1 — per-app/per-category daily limits with block-duration menu (basic) — ✅
- 4.2 — Accessibility-service + overlay block screen (kind wording, extend/skip/unlock) — ✅ (activity-based)
- 4.3 — every override logged — ✅
- 4.4 — always-allowed list + focus session (placeholder) — ✅ (basic)

## What shipped

- `LimitEventEntity`/DAO; DB v4.
- `BlockerAccessibilityService` detects foreground app and triggers block screen when over budget (respecting always-allowed and earning session).
- `BlockScreenActivity` with extend/skip/unlock and logged overrides.
- `LimitsViewModel`/`LimitsScreen` for always-allowed toggles.

## Tests

- `ExampleUnitTest.addition_isCorrect` — green in CI run 38755465576.
- `./gradlew --no-daemon testDebugUnitTest assembleDebug` — green in CI run 38755465576.

## Follow-ups

- Phase 5 — earnings + learning.
