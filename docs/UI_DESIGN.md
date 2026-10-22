# UI Design v2 — Daily Logger (approved direction, 2026-10-22)

> This is the target design for future phases, not a description of the current
> APK. The approved visible label **Daily Logger** ships with the Phase 8
> checklist front door. Until then, the current shipped label remains **Day
> Ledger**. Keep `applicationId` and Room database identity
> `io.github.pabi277.dayledger` unchanged.
>
> Requirements and acceptance tests live in [`REQUIREMENTS.md`](REQUIREMENTS.md),
> including DL1–DL28. This document defines presentation and interaction; it does
> not authorize implementation in Phase 7.

## 1. Approved product frame

The approved hybrid is **A+B+C**:

- **Today — Command Center Checklist:** the first screen answers “What now?” and
  “What is left today?” Usage evidence is supporting **Phone evidence**, not the
  Today hero.
- **Plan / Tomorrow — timeline:** blocks describe *when*; checklist items describe
  *what*. The two may link but are never conflated.
- **Focus — One Thing:** a focused, optional mode launched from Today, a widget or
  a notification. It is not a replacement home screen and does not force a timer.

Primary navigation, in this order: **Today · Plan · Habits · Review · More**.
Focus is a contextual action. More preserves access to **Usage, Earn, Learn,
Limits, Categories, Notifications and Settings**, plus existing supporting screens
such as About and data export. Do not silently remove or strand existing features.

## 2. Visual system and layout

- Material 3 with light and dark themes; use dynamic color on supported Android
  versions and a calm, high-contrast fallback palette elsewhere.
- Single-column, vertically scrollable layouts. Use consistent 16dp horizontal
  gutters, clear section spacing and cards only when they improve grouping.
- The primary action is visually distinct; secondary and destructive actions are
  clearly differentiated. Avoid dense dashboards, decorative charts above the
  checklist, streak pressure and color-only status.
- Use resource-backed copy. Use fictional neutral examples in previews, tests and
  screenshots. Never seed a personal timetable, routine, app list or real task.
- Respect system font scaling, display-size settings, dark theme and reduced-motion
  preference. Content must reflow rather than clip or require horizontal scrolling.

## 3. Today — Command Center Checklist

**Top region:** date/day selector, permission or reminder-health notice only when
needed, and a compact progress summary showing Core remaining and completed Full /
Minimum counts. Do not impose a maximum of three Core items; an optional Top 3 is a
planning aid, not a hard limit.

**Checklist order:**

1. **Now / Overdue:** the most relevant current item, with an honest due label and
   one-tap primary action.
2. **Next:** the next timed item and its time window.
3. **Due today / Time windows:** grouped, scannable rows; untimed items remain in
   an Anytime group rather than receiving invented times.
4. **Quick Capture:** always reachable from the app bar/FAB and other approved
   entry points.
5. **Phone evidence:** a lower, collapsible card showing device-reported usage,
   freshness (“as of …”) and a path to Usage/Review. It never marks an item done.

A short, skippable **Morning Brief** may confirm a day type/template, due items and
an editable Top 3. With an existing template it targets ≤60 seconds; generating it
more than once must not duplicate occurrences.

Each item row shows a short title, optional domain, due/window state and only the
smallest useful status cue. Primary **Done** is one tap and never opens a required
form. Secondary actions are **Minimum**, **Later**, **Reschedule**, **Skip** and
**Edit**. Provide **Undo** immediately after a state-changing action. Use plain
labels/icons with TalkBack descriptions; do not encode meaning only by color.

An overdue item is visible but not alarmist. “Nothing due” and “No Core items
left” are calm states, not celebratory streak screens. Completed rows may collapse
but remain available in history.

## 4. Quick Capture and item editor

### Quick Capture

Open from Today, the widget and other explicit capture entry points. The title is
the only required field. Save title-only items directly to Inbox/Today; date,
domain, recurrence, due time, priority, note and detail remain optional. Show a
brief confirmation and Undo rather than forcing classification before capture.

### Item / habit editor

Make the item type explicit: **Habit rule**, **One-off task/deadline**, or a dated
**Occurrence**. Never present one as another. Provide only the fields relevant to
the selected type:

- Title and optional domain.
- Habit cadence (daily, chosen weekdays, weekly or interval), preferred window,
  weekly target and optional Minimum Version.
- One-off date-only deadline or optional date/time.
- Reminder profile and optional note.
- Pause, archive and future-schedule actions; explain which future occurrences
  change. Editing a rule must not rewrite past occurrences or completion logs.

Use a confirmation step for destructive/archive actions, with a clear undo where
possible. Do not ask for proof or minutes to save a task or mark it Done.

### Optional-detail sheet

After Done or Minimum, offer (never require) minutes, quantity, topic or a short
note. Include an obvious **No detail / Skip** action. Dismissing the sheet keeps
the completion. Timer or usage data may prefill a suggestion only; the user
confirms it.

## 5. Plan, Tomorrow and carryover

Plan is a day timeline with proportional blocks, readable time labels, an Anytime
lane and visible overlap warnings. Provide accessible move/resize controls in
addition to drag gestures. Checklist items may link to zero or more blocks; moving
a block changes its time, not the item's completion state.

Tomorrow reuses the timeline language but adds an editable **Top 3** and a review
of due tasks and explicit carryovers. Template blocks, due tasks and carryovers
must be distinguishable and merge without duplicates. Applying a template must
not silently complete or overwrite checklist history.

At Evening Close, every unfinished item receives an explicit decision: **Move**,
**Schedule**, **Shrink to Minimum** or **Drop/archive**. Show repeated carryover
without shame. Never roll an item indefinitely or silently discard it.

## 6. Habits, Review and Focus

### Habits

Show Active, Paused and Archived sections, recurrence, next due date, weekly
Full/Minimum progress and pause controls. Make target denominators explainable;
paused and unscheduled days are not missed days. Domain and template choices are
optional and editable.

### Review

Provide Day and Week views, searchable history (date, domain, text), source links
for completion records, pause-aware consistency and Full-versus-Minimum totals.
Show one primary suggestion rather than a wall of scores. Usage charts may be
reached here or in More → Usage; label them device-reported foreground time and
show freshness. Do not infer learning or completion from usage.

### Focus — One Thing

A distraction-reduced view for one selected item, entered from Today, a deep link,
widget or notification. Offer optional timer controls and explicit Done/Minimum,
Later and Exit actions. A timer ending only offers a minutes suggestion. Focus
works when Accessibility-based app limits are disabled; it never blocks essential
or always-allowed apps.

## 7. Reminder surfaces and action behavior

Notifications use the persisted per-occurrence state and approved Core, Standard
and Flexible profiles in `REQUIREMENTS.md`. Keep copy short, calm and actionable.
Where Android permits, actions are **Done**, **Minimum**, **Later** and **Open**;
otherwise deep-link to the same occurrence. Repeated/stale action taps are
idempotent. After Core's audible cap, unresolved work remains silent and visible
in the ongoing notification/widget until action or day close. Quiet hours are a
no-fire window: no new alert, sound or vibration; due state persists for the next
eligible time without a catch-up burst. Global Pause, Less Often and independent
daily Core/general caps must be reachable from the reminder flow; never hide why a
reminder is paused or delayed.

## 8. Widgets — compact and wide

Both sizes show a freshness stamp and open a safe, date-aware deep link. Avoid
claiming continuously live data.

- **Compact:** current/Next item (whichever fits), Core remaining count and a
  single accessible open/action target. Truncate titles safely; do not show a
  misleading blank card when there is no item.
- **Wide:** Now and Next with due/window labels, Core progress/count, optional next
  time block, Quick Capture and a freshness stamp. Keep checklist state readable at
  large font sizes and on dark/light launchers.

Widget actions carry the exact occurrence identity and are idempotent. If the
widget is stale or its permission/state is unavailable, open Today and explain
rather than applying an action to a different item.

## 9. Navigation and deep links

Preserve the current app's supporting destinations under More and retain a clear
back path. Notification, widget and Quick Capture links must carry an explicit
route and stable item/occurrence identity; opening Tomorrow must retain its date.
Returning from a child screen restores the originating tab and scroll position
where practical. An invalid, archived or stale link opens Today with a brief
explanation, not a blank screen. Do not use `localhost` or external services for
any in-app route; the product is local-first.

## 10. Empty, error and permission states

- **No checklist items:** invite Quick Capture or a generic editable template;
  never populate a personal schedule automatically.
- **No history:** explain that history starts with the first explicit action.
- **Usage access denied/revoked:** show a non-blocking explanation and Settings
  route. Checklist capture and completion remain usable.
- **Notifications/exact alarm unavailable:** state which reminder behavior is
  approximate or unavailable and offer a path to system settings; do not claim a
  fire occurred.
- **Storage/export/restore error:** preserve existing data, explain the failed
  step, offer retry and never suggest clearing app data as a fix.
- **Quiet hours/Pause/cap active:** show the reason and next eligible time when
  known. A paused day is not counted as a failure.
- **Offline:** all core screens remain usable; do not show fake sync states.

## 11. Accessibility and copy

- Minimum 48dp touch targets for interactive controls; do not make row gestures the
  only way to complete, edit, reorder or reschedule an item.
- TalkBack order follows the visual hierarchy. Every icon-only action has a
  specific label, state and purpose; announce completion/Undo and timer state
  changes without repeated noise.
- Support large text and display scaling without clipped controls, hidden actions
  or horizontal scrolling. Use contrast-compliant text in light/dark themes and
  shape/icon/text as well as color for status.
- Keep motion optional and brief. Respect reduced-motion settings and avoid
  flashing, vibrating or animated streak pressure.
- Copy is kind, direct and non-shaming. Prefer “Not done yet—choose what happens
  next” to failure language. Never promise behavior change, exact usage accuracy,
  income or alarm delivery beyond Android's guarantees.

## 12. Phase boundary

This target design is for the approved Daily Logger direction. Phase 7 remains
migration, backup/restore and privacy foundation only; it does not ship this
checklist front door or rename the current app. Phase 8 introduces the checklist
front door and visible label **Daily Logger** together, while preserving package
and database identity. The full device matrix remains Phase 15; every individual
phase still requires an owner install and short smoke check.