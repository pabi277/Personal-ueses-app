# Day Ledger UI Design

## Principles
- Material 3 (Material You) with dynamic color on Android 12+ and a calm teal fallback palette otherwise.
- Bottom navigation with 3-5 top-level destinations; secondary screens live under "More".
- Single-column layouts, 16dp horizontal screen padding, 12-16dp vertical spacing between sections.
- Outlined text fields in forms; primary buttons for the main action, outlined buttons for secondary actions.
- Cards use large (16dp) rounded corners and low tonal elevation for grouping.

## Theme
- `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`
- `DayLedgerTheme(darkTheme, dynamicColor)` wraps `MaterialTheme`.

## Reusable components
- `ui/components/Components.kt`: `ScreenTitle`, `SectionTitle`, `AppCard`, `EmptyState`, `KeyValueRow`.

## Navigation
- Today, Plan, Limits, Earn, More.
- More opens Categories, Notifications, Learning, Report, Settings, Tomorrow.