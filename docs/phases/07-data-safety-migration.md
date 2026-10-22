# Phase 07 — Data safety and migration foundation

- Status: 🚧 **BLOCKED at the owner signing gate** · Date: 2026-10-22 ·
  Branch: `arena/0276c412-personal-ueses-app`
- Base: `main` @ `8070713` (merge commit of PR #11)
- **No implementation code was written in this round.** The signing gate in
  `docs/ROADMAP.md` ("Signing-continuity audit status") is unresolved, so the
  phase stops here and waits for the owner's answer. Nothing in
  `.github/workflows/ci-build.yml`, `app/build.gradle.kts`, the manifest, the
  Room classes or the source tree has been changed.

## 1. Owner inputs this gate needs (not supplied yet)

The authorization message left both owner-evidence fields as unfilled
placeholders:

| Gate input | What arrived | Consequence |
|---|---|---|
| Owner-device signing/update test (two run IDs + result) | placeholder text, no run IDs, no result | Device-side update-over-install evidence is **absent** |
| State of the data in the currently installed build | placeholder (disposable / valuable / not installed) | Must be assumed **valuable**, per `rule.md` §5 "History is sacred" |

Neither can be inferred from the repository, and `rule.md` §3 forbids treating a
guess as a gate answer. The owner answers below (§7) are the unblocking step.

## 2. Gate step 1 — main contains the merged PR #11 documentation — ✅ verified

- `gh pr view 11` → `state: MERGED`, `mergedAt: 2026-10-22T11:34:32Z`,
  `mergeCommit 8070713636f27f0f692420984499568595cb23fe`, files changed:
  `README.md`, `docs/DECISIONS.md`, `docs/REQUIREMENTS.md`, `docs/ROADMAP.md`,
  `docs/UI_DESIGN.md`, `docs/reference/Daily-Logger-Handoff-for-New-Chat.md`,
  `prompt.md`.
- Local `HEAD` == `origin/main` == `8070713`, working tree clean.
- The merged documentation is present in this checkout: `docs/ROADMAP.md`
  "Phase 7 — Data safety and migration foundation" (historical Room inventory,
  signing audit, parts 7.1–7.5), `docs/DECISIONS.md` "Owner update — gate answers
  (2026-10-22)" (label timing, UI hybrid, OS-backup exclusion, public repo), and
  `docs/UI_DESIGN.md`.
- Historical Room inventory re-verified against the now-complete local history
  (the clone was depth-1 and was unshallowed to check this): `fe58d9f` → version
  1, `ad0948a` → 2, `8974737` → 3, `5970aed` → 4, `1ff9a87` → 5, `3aec2e0`
  (Phase 6) still 5. The roadmap inventory is accurate.

## 3. Gate step 2 — workflow, Gradle signing and the failed main job — inspected

Findings are facts about **configuration and secret names only**. No secret
value, keystore or credential was requested, read, printed, copied or committed.

**a. The workflow's release job fails on a missing secret name.**
Run `39771615912` (main, push, tip `8070713`, 2026-10-22 — the current `main`
tip) and the earlier run `38845505942` (main, 2026-10-16) both fail the same way:

- Job `Signed release AAB and APK` → step `Prepare signing key on runner` fails
  after 0s of work; later steps `Build signed release AAB and APK` and
  `Upload signed release artifacts` are **skipped**.
- Failure annotation: **`Missing required Actions secret: ANDROID_KEYSTORE_BASE64`**.
- Therefore, and only this much: `ANDROID_KEYSTORE_BASE64` is **not set** on this
  repository. The check loop (`.github/workflows/ci-build.yml:95–100`) exits at
  the first empty name, so `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
  `ANDROID_KEY_PASSWORD` are **unproven** — they may be set or unset; the log
  cannot distinguish them.
- The secret list API is not readable with the sandbox token
  (`403 Resource not accessible by integration`), so no independent inventory of
  names exists.

**b. A second, independent defect: Gradle has no signing configuration at all.**
`app/build.gradle.kts:8–39` contains no `signingConfigs` block and no
`signingConfig` on either build type; the `release` block sets only
`isMinifyEnabled`/`proguardFiles` (`:21–29`). Consequences proven by
configuration, not by inference:

- Nothing in Gradle reads `ANDROID_KEYSTORE_PATH` or the four secret environment
  variables that the workflow exports
  (`.github/workflows/ci-build.yml:73–76, 105`), despite the workflow's own
  header comment claiming it does (`:8–12`).
- Even with all four secrets present, `assembleRelease` would emit
  `app-release-unsigned.apk`; the upload glob
  `app/build/outputs/apk/release/*.apk` (`:114–117`) would still match it, so CI
  would go green and ship an APK that cannot be installed. The signed-release
  path is therefore broken in two places: missing secret **and** missing Gradle
  signing config.

**c. The progress APK path (`test` job) never touches a keystore.**
`.github/workflows/ci-build.yml:29–66` has no keystore restore/cache step. The
only cache is `actions/setup-java`'s `cache: gradle` (`:42`), which caches
`~/.gradle` — not `~/.android`, where the debug keystore lives. So the progress
artifact is signed by whatever AGP generates on that fresh runner (§5).

## 4. Gate step 3 — fingerprint comparison — ❌ not possible in this environment

Both available routes were attempted and both failed:

1. `gh run download 39766581703 -n app-progress-apk` → error body is a signed
   `https://productionresultssa18.blob.core.windows.net/...zip?...` URL, and the
   request dies with `EOF`.
2. `curl` of the artifact's `archive_download_url` → `HTTP 302` immediately
   followed by `OpenSSL SSL_connect: SSL_ERROR_SYSCALL` to
   `productionresultssa18.blob.core.windows.net`.

GitHub redirects artifact archives to `*.blob.core.windows.net`, which is outside
this sandbox's outbound allowlist (github.com, codeload.github.com, api.github.com,
registry.npmjs.org, pypi.org, files.pythonhosted.org). A local re-build is equally
impossible: the sandbox has no JDK (`java: command not found`), no
`ANDROID_HOME`, no Android SDK and no Gradle cache, and Google's Maven repository
is not reachable. **No certificate fingerprint has been inspected; nothing is
known about the signer of any artifact.** Matching artifact byte sizes
(18,234,183 vs 18,234,185 for the two runs) are not signing evidence.

With no fingerprints and no owner-device result, the owner's update-over-install
observation would be the primary evidence — and it was not supplied (§1).

## 5. Gate step 4 — is the current design stable? — ❌ proven unstable by construction

The design was treated as suspect, and it does not survive inspection:

1. No debug `signingConfig` exists (`app/build.gradle.kts:8–39`), so the progress
   APK uses AGP's default debug signing, which materialises an auto-generated
   `$HOME/.android/debug.keystore`.
2. Each CI job runs on a **fresh** `ubuntu-latest` VM, and
   `.github/workflows/ci-build.yml` neither restores nor caches that file
   (`:29–66`). GitHub-hosted runners do not ship a pre-created debug keystore;
   AGP generates a brand-new RSA key pair on first use.
3. Therefore **every progress run signs with a different certificate**. Android
   refuses to install an APK over one signed by a different key
   (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`); the only way in is uninstall, which
   deletes app-private data including `dayledger.db`.
4. The private key of a given runner is destroyed with that runner, so a
   previously installed CI build **cannot** be updated, and the currently
   installed build's certificate cannot be reproduced even if its fingerprint
   were known.

This is the documented, commonly-hit behaviour of unsigned-config AGP builds on
GitHub runners: "GitHub Actions runners don't have `~/.android/debug.keystore`
pre-created" ([jakes1345/xat-client#6](https://github.com/jakes1345/xat-client/pull/6));
"Each run of Android CI was signing its debug APK with a new random key. GitHub
runners start empty, so Android Gradle generated a fresh debug.keystore every
time." ([mllagostera/commander-companion#139](https://github.com/mllagostera/commander-companion/pull/139));
AGP "gera uma debug.keystore nova a cada runner limpo"
([jadiel054/plutao-os#118](https://github.com/jadiel054/plutao-os/pull/118), where
the no-`signingConfigs` case is confirmed against real installations); and the
resulting data-wiping mismatch between CI and local debug builds
([TheRealAshik/Jules#86](https://github.com/TheRealAshik/Jules/issues/86)).

**Verdict: progress signing is not stable, and signed-release continuity cannot be
established.** The exhaustive-loop `for name in ...` check means the only proven
fact about the release secrets is that `ANDROID_KEYSTORE_BASE64` is absent.
`docs/ROADMAP.md` requires a stop at the owner gate in exactly this situation.

## 6. Gate step 7 — data conflict, stated explicitly

Because the owner's data-value answer was a placeholder, this record assumes the
installed data is **valuable** (`rule.md` §5). On that assumption there is a real
conflict:

- The one-time uninstall needed to adopt a stable key **deletes**
  `/data/data/io.github.pabi277.dayledger/`, including
  `databases/dayledger.db`.
- **The installed build cannot export its own data.** The CSV exporter writes a
  header plus the literal row `today,placeholder,0`
  (`MainActivity.kt:90–100`, `:96`), and the Backup button is wired to an empty
  lambda (`MainActivity.kt:180`, `SettingsScreen.kt:17–31`). Phase 7's SAF
  backup does not exist yet, so a working backup cannot be produced **before** the
  signing switch.
- Therefore: **valuable installed data + uninstall-first signing switch = data
  loss unless it is rescued out of the device first.** That rescue has not been
  attempted, agreed or proven, so it is not assumed to work.

One possible rescue route exists and must be agreed and tested before any
uninstall, never after: the installed CI artifact is a **debug** build, so a PC
with platform-tools can copy the database out of the app's private directory with
`adb shell run-as io.github.pabi277.dayledger` (no root, no repo secret). That
route needs USB debugging, a PC, and a verification step that the copied
`dayledger.db` actually opens. It is **not** a substitute for Phase 7's versioned
SAF backup, and it is not authorised by this record.

## 7. What the owner must answer (gate) — nothing else blocks the phase

1. The two missing owner inputs (device update-over-install result with both run
   IDs; and whether the installed data is disposable, valuable or absent).
2. Approval, or rejection, of the single owner-controlled signing identity in §8.

Phase 7 implementation does not start until those answers arrive.

## 8. Owner setup plan — one stable, owner-controlled personal sideload identity

Phone-friendly. Secret **names** only; values never appear here, in a commit, or
in chat. This plan is **not executed yet** and changes no workflow or Gradle file
in this round.

**What is wrong today, in one line:** every CI run invents a throwaway signing key,
so each APK is a different app to Android — that is why an update fails.

**Step A — make the key on your own PC once (never on GitHub, never in the repo).**

- Install a JDK 17 (Android Studio's bundled one is fine; `keytool` ships with it).
- In a folder **outside** this repository — e.g. `D:\keys\` or `~/keys/` — run
  `keytool -genkeypair -v -keystore dayledger-owner.jks -alias dayledger-owner
  -keyalg RSA -keysize 4096 -validity 10000`. Choose the store and key passwords
  yourself; they are the owner's, not the agent's.
- Store two copies: the `.jks` in that private folder **plus** one offline copy
  (password manager attachment or encrypted USB). Losing this file means a
  future update can only be installed by uninstalling again — the same pain you
  are fixing now.
- Never commit it, never paste it, never paste its passwords, and never put it in
  the repository, an issue or this chat. `.gitignore` already has no keystore
  exceptions and must stay that way.

**Step B — put it into GitHub yourself, as four repository secrets.**

GitHub → repo → Settings → Secrets and variables → Actions → New repository
secret, exactly these four **names** (the workflow already reads these names, so
no new name has to be invented):

| Secret name | Value the owner pastes |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | output of `base64 -w0 dayledger-owner.jks` (one long line) |
| `ANDROID_KEYSTORE_PASSWORD` | the store password chosen in Step A |
| `ANDROID_KEY_ALIAS` | `dayledger-owner` |
| `ANDROID_KEY_PASSWORD` | the key password chosen in Step A |

Tell me only that the four names exist — never the values. I will then (after
your go-ahead) wire a real `signingConfigs` block in `app/build.gradle.kts`, add a
keystore-restore step to **both** the progress job and the release job, and add a
CI step that prints the APK's SHA-256 certificate fingerprint with
`apksigner verify --print-certs`. A certificate fingerprint is public information
and is the evidence that later runs really are signed by the same key.

**Step C — one final uninstall/reinstall: yes, exactly once.**
Needed only if a build of this app is currently installed from an older CI run,
because its key was thrown away with that runner. If nothing is installed ("app
was not installed"), skip it. After the first APK signed with your key is
installed, every later `app-progress-apk` updates straight over it — no further
uninstalls.

**Step D — what happens to the data already in the installed build.**
Everything in the installed build is deleted by that uninstall: usage history,
categories, budgets, plans, limits, earnings, learning streak, settings. There is
no export inside that build (see §6). Options, in order of safety:

1. **Rescue first (recommended if the data is valuable).** On a PC with
   platform-tools, while the old build is still installed and USB debugging is on,
   copy `databases/dayledger.db` out through `adb shell run-as`. I would give the
   exact, small command set and verify the copy opens before you uninstall
   anything. Doing this without verification is not a backup.
2. **Accept the loss** if the data is disposable — it is a fresh-ish install with
   a few days of usage.
3. **Do nothing until Phase 7's SAF backup exists** — not possible as a workaround
   here: installing the build that has the backup already requires the uninstall.
   This is exactly the conflict in §6.

**Explicitly not happening:** no public/`android` debug keystore is committed, no
secret value is requested or stored, and the signed-release path is **not** retired
— it stays in the workflow and simply starts working once the same four secrets
exist and Gradle reads them.

## 9. Phase 7 work held back (nothing started)

Room v5 schema export and fictional v1–v5 fixtures · explicit non-destructive
v1/v2/v3/v4 → v5 migrations and removal of `fallbackToDestructiveMigration()`
(`DayLedgerDatabase.kt:25–26, 53`) · complete versioned SAF backup/restore
(replacing `MainActivity.kt:90–100, 180`) · `allowBackup="false"` plus legacy
full-backup rules and API 31+ data-extraction rules (absent from
`AndroidManifest.xml` today) · update-over-install proof on the stable signing
path · the README AppFlow provenance line and the
`docs/REQUIREMENTS.md` §7 link as a documentation ride-along.

No checklist UI, no Daily Logger label change, no new permissions, no database
version bump, no personal seed data.

## 10. How this phase becomes Done

Two evidence lines, kept separate so CI evidence and owner evidence can never be
confused:

- **CI/test evidence (agent):** migration tests for v1–v4 → v5 and the 1→2→3→4→5
  steps, fictional fixtures for every legacy table, backup/restore round-trip
  tests, merged-manifest check of the backup attributes, green `Android CI` run
  with `app-progress-apk`, and a certificate fingerprint printed for that APK.
- **Owner-device evidence (owner, reported by him):** the Phase 7 APK installing
  over the previously installed build without uninstalling, the app launching,
  the data surviving the update, an exported SAF backup file restored in an
  isolated install, and P0 observations with any exact error text.

Phase 7 is not Done while either line is missing or while this gate is open.
