# CI-failure issue triage (2026-10-23)

The repository's `Report failure` step files a GitHub issue titled "CI build failure"
whenever the build job fails. **21 such issues are open** (#1–#9, #12–#24; #10, #11 and
#22 do not exist as issues). None of them describes a failure that still reproduces at
`ffa6502`.

**None has been closed.** Closing an issue is an owner command in this project, so this
page is the triage record the close decision needs. Every issue below is an
auto-generated build report, not a bug report from a person, and none carries a reply or
a decision that closing would discard.

## How each issue was matched to a run

The filings carry **no run id, no commit and no timestamp** — the `Report failure` step
pipes a log tail straight into `gh issue create`. Matching is therefore by timestamp:
each issue was filed 15–45 s after a failing run finished. That inference is reliable
enough here because the failing signatures are distinctive, and it is labelled as an
inference rather than as a recorded fact wherever the run is still listed.

| # | Filed (UTC, 2026) | Run / commit it reports | Signature | State at `ffa6502` |
|---|---|---|---|---|
| 1 | 10-15 14:35 | ~`aec9d20` | `MainActivity.kt` — conflicting import: `Modifier` ambiguous | Compiles; `MainActivity` imports resolve |
| 2 | 10-15 14:52 | `e4feae0` `38755148025` | `BlockScreenActivity.kt:30` — unresolved `setContent` | Compiles |
| 3 | 10-15 15:01 | `3d3c3b7` `38756560063` | `MainActivity.kt:75` — `TRY_CALL`/`WHEN_CALL` type mismatch, unresolved `contentResolver` | Source is no longer the mangled text that produced this |
| 4 | 10-15 17:42 | no run remains | `MainActivity.kt:158` — expecting a top-level declaration | Compiles |
| 5 | 10-15 18:16 | no run remains | unresolved `Icon`, `padding`, `dp` | Compiles |
| 6 | 10-15 18:19 | no run remains | unresolved `padding` at `MainActivity.kt:201` | Compiles |
| 7 | 10-15 18:29 | `9c8216e` `38785871377` | unresolved `fillMaxWidth` | Compiles |
| 8 | 10-15 18:30 | `e1a2e7d` `38786006097` | unresolved `fillMaxWidth` | Compiles |
| 9 | 10-15 18:30 | `cbb9356` `38786095666` | unresolved `fillMaxWidth` | Compiles |
| 12 | 10-22 11:47 | `8d5efe1` `39773026332` | `app/build.gradle.kts:25` — unresolved reference `io` | Build script compiles; debug + release both build |
| 13 | 10-22 12:15 | `b650158` `39776380281` | `MigrationTest` v5 schema drift — autoincrement marker `1\|1` vs `0\|1` | Test renamed to *"migrated schema matches the v7 entity definitions exactly"* and passes |
| 14 | 10-22 12:20 | `de35af5` `39776972220` | same v5 schema drift | same |
| 15 | 10-22 16:09 | `c4d50c6` `39811357193` | `packageDebug` — `KeytoolException: Failed to read key dayledger-owner … null` | Owner keystore opens in both jobs; signer `533D76ED5C2E264779EE005C` |
| 16 | 10-22 16:11 | `db24580` `39811839022` | **empty body** — no log tail, no test names | Nothing to reproduce; an infra blip, kept only because nothing closes it |
| 17 | 10-22 16:14 | `b0f581e` `39812191114` | `ANDROID_KEYSTORE_PASSWORD` does not open the keystore | Fixed; keystore opens |
| 18 | 10-22 16:16 | `600e25b` `39812528295` | same | Fixed |
| 19 | 10-22 16:40 | `4735b3d` `39815873911` | same | Fixed |
| 20 | 10-22 16:44 | `3270a67` `39816488826` | same, after trimming whitespace | Fixed |
| 21 | 10-23 03:25 | `d773a12` `39894932989` | same, after trimming whitespace | Fixed |
| 23 | 10-23 05:55 | `77537b4` `39908341045` | `ChecklistGenerationTest` — weekly habit, weekday parsing (2 failures) | Both tests exist and pass |
| 24 | 10-23 06:36 | `11e18d3` `39912443671` | `HabitsScreen.kt:170` — expecting `"` / `->` | Compiles |

Two failing runs from that morning (#23's neighbours `873e0fd` and `b6d9bb2`) filed
**nothing**, so the filer is not one-issue-per-failure either. Together with the three
issues whose runs no longer exist, that is the case for replacing this filer rather than
trusting its count.

## Evidence that the failures are gone

- **`ffa6502`, run `39927123528` — success.** Job *Unit tests, debug build, progress APK*:
  every step green, including *Run unit tests and build debug APK*, *Prepare owner signing
  key on runner*, *Verify OS-backup exclusion in the merged manifest* and *Record Room
  schema export*. 113 unit tests. `app-progress-apk` 18,599,302 B, `room-schema` 2,406 B,
  signer `533D76ED5C2E264779EE005C`.
- **`66582f1` on `main`, run `39914193884` — success**, including the *Signed release AAB
  and APK* job: keystore restore, signed build, certificate recorded, artifacts uploaded
  (24,718,303 B). This is what retires #15 and #17–#21.
- The compile-error issues (#1–#9, #12, #24) cannot recur without the build job failing,
  and that job has not failed since 10-23 06:35.

## Closure status — approved, blocked on a permission

**Owner command 2026-10-23: close all 21, including the empty #16.** Not executed yet.

The sandbox's GitHub connection is an App installation with repository `contents: write`
(pushes work) but **no `issues: write`**. Every attempt returned HTTP 403
`Resource not accessible by integration`:

- `POST /repos/:owner/:repo/issues/:n/comments` — the closing note
- `PATCH /repos/:owner/:repo/issues/:n` — the state change
- `POST /repos/:owner/:repo/issues/:n/labels` — a "closed by triage" label instead

So nothing was half-done: **no issue was commented on, labelled or closed**, and all 21
are still open and unmodified. Two ways to finish it:

1. **Reconnect GitHub in Arena with issue permissions**, then one command per issue (or a
   loop) closes them with the note prepared in this page's mapping table.
2. **A one-shot workflow** using the Actions `GITHUB_TOKEN`, which does have
   `issues: write` — that is the token the `Report failure` step already uses to *create*
   issues. It would be a small `workflow_dispatch` job that comments the mapping and
   closes the 21 numbers, run once, then deleted. **This is a workflow change, so it needs
   an explicit gate answer** and has not been written.

Nothing else depends on the outcome: the failures are already retired by green runs
whether or not the reports stay open.

## What is still real

Nothing in the product. What remains is in the reporter itself, and each fix is a
workflow change that needs an owner gate:

1. **No run id or commit in the body.** Every filing is unlinkable to a run after the
   fact; this page had to reconstruct the links from timestamps. One `echo` of
   `$GITHUB_RUN_ID` / `$GITHUB_SHA` and the run URL would fix it.
2. **"Failing unit tests" lists passing tests.** `grep -o 'testcase name="[^"]*"'` prints
   *every* test case in the XML, not just the ones with `<failure>`. Issue #23's body
   therefore names 21 data-safety tests as failing when the two real failures are the
   `ChecklistGenerationTest` ones shown separately. This is actively misleading and is
   worth fixing before the filer is trusted again.
3. **No dedupe and no auto-close.** Identical signatures refile on every push, and a
   green run never closes what a red one opened — hence 21 open reports for failures
   that were fixed hours later.

A replacement needs: run id + commit + a signature line in the body, only genuinely
failed test cases listed, one open issue per signature, and a close-on-green step keyed
to that signature.
