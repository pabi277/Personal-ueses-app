# Phase 07 — Data safety and migration foundation

- Status: 🚧 **IN PROGRESS — parts 7.1–7.4 implemented and CI-verified; 7.5 and
  the Done gate wait on the owner's device** · Date: 2026-10-22 ·
  Branch: `arena/0276c412-personal-ueses-app`
- Base: `main` @ `8070713` (merge commit of PR #11)
- Round 1 (evidence only) wrote no code. Round 2 wires the owner-approved signing
  identity (§8) — the one change the gate authorised — and **still writes no Phase 7
  feature code**, because the owner's answer made the installed data *valuable* and
  the first install on the new identity would destroy it (§6a).

## 1. Owner inputs for this gate — answered 2026-10-22

| Gate input | Owner's answer |
|---|---|
| State of the data in the currently installed build | **Valuable — do not lose it** |
| Approval of one owner-controlled signing identity | **Approved — wire it** (four secret names, values only the owner's) |
| Owner-device signing/update test with both run IDs | not provided (the gate did not need it once the design was proven unstable) |

The two answers are consistent only if the identity change is **staged behind a
data rescue** — see §6a. That is why implementation code still has not started.


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

## 5a. Measured proof of instability (2026-10-22, two runs, same tree)

The earlier sections argued instability from configuration. It is now measured. CI
publishes the APK's signer fingerprint as an artifact **name**
(`signer-<first 24 hex of the certificate SHA-256>`, falling back to `signer-unknown`),
which is readable through the API without downloading anything:

| Run | Commit | Signer artifact | Certificate SHA-256 (first 24 hex) |
|---|---|---|---|
| `39804639332` | `1beb4d5` | `signer-DC13157ECCF809111C72F888` | `DC13157ECCF809111C72F888…` |
| `39805057123` | `342db50` (same tree, empty commit) | `signer-B988DBD3452E29EAD4C1CBFE` | `B988DBD3452E29EAD4C1CBFE…` |

**Different certificates for the same code.** That is the defect in one line: each
runner generates its own debug key, so no APK can install over the previous one. It also
gives the gate its comparison method — after the owner adds the four secrets, two runs
must produce the **same** `signer-…` name, and that is the stability evidence.

Getting this to work took four attempts, each recorded for the next round: `::notice::`
annotations are not retrievable through this API (`signer-unknown`), apksigner is absent
on the runner image, `keytool -printcert -jarfile` had nothing to read because minSdk 26
made AGP sign v2-only, and only Gradle's `signingReport` produced a real fingerprint.

## 6. Gate step 7 — data conflict, stated explicitly (⚠️ STOP condition met)

The owner has now confirmed the installed data is **valuable — do not lose it**.
That turns the assumption below into the recorded fact, and the conflict stands:

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

One possible rescue route exists and must be tested **before** any uninstall,
never after: the installed CI artifact is a **debug** build, so a PC with
platform-tools can copy the database out of the app's private directory with
`adb shell run-as io.github.pabi277.dayledger` (no root, no repository secret).
It is **not** a substitute for Phase 7's versioned SAF backup.

### 6a. The staged order this record now follows

1. Signing plumbing is wired (done in round 2, §8b). It changes nothing on the
   phone until an APK built with the owner key is installed.
2. The owner creates the keystore outside the repository and adds the four secret
   names (§8).
3. **Data rescue (§6b) — before any install of an owner-key APK.**
4. Only then: one uninstall, install the owner-key `app-progress-apk`, and the
   update-over-install proof becomes possible (Phase 7 part 7.5).
5. Phase 7 parts 7.1–7.4 (schema export, migrations, SAF backup/restore, OS-backup
   exclusion) are implemented and tested, and the owner's smoke check is recorded
   in §10 with CI evidence kept separate from device evidence.

Installing an owner-key APK before step 3 is the single action that loses the
owner's data; it is not taken or recommended by this record.

### 6b. Rescue procedure (owner-run, PC, ~10 minutes)

The build currently installed is a CI **debug** artifact, which is what makes this
possible. It must be done **while the old build is still installed**.

1. On the phone: Settings → About phone → tap *Build number* seven times → back →
   System → Developer options → enable **USB debugging**. Connect USB, choose
   *File transfer*.
2. On the PC, install Android platform-tools, then confirm the device is visible:
   `adb devices` (accept the *Allow USB debugging?* prompt on the phone).
3. Confirm the private directory is reachable (this proves the build is debug):
   `adb shell run-as io.github.pabi277.dayledger ls -l databases`
4. Copy the database out, to the PC, and keep the file somewhere private (not in
   this repository):
   `adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db > dayledger-rescue.db`
   then the write-ahead log if it exists:
   `adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db-wal > dayledger-rescue.db-wal` (an error here only means the file is absent).
5. Verify before trusting it: the file should be several KB and start with
   `SQLite format 3`. Locally, `sqlite3 dayledger-rescue.db ".tables"` should list
   `app_usage`, `categories`, `blocks`, `earnings`, `learning` and the rest.
   Optionally past a hash so the copy can be proven later.
6. Report the verified result in chat. **Do not uninstall yet** — the install step
   comes after I have confirmed the Phase 7 restore path can read that file.

The rescued `.db` is a raw Room database, not the versioned backup Phase 7 will
produce; it will be re-imported through the new restore path (or copied back with
the same `run-as` route on a debug build) after the reinstall.

### 6c. Termux variant (owner's environment: phone only, no PC)

> The full, phone-friendly walkthrough now lives in
> [`07-owner-rescue-runbook.md`](07-owner-rescue-runbook.md) — start there; this
> section keeps only the shape of the route.

Same idea, different transport: **wireless debugging to the same phone**, so the
`adb` client and the device are one handset. Needs **Android 11 or newer** (its
`Wireless debugging` menu) and Termux installed from **F-Droid** (the Play build is
outdated). On Android 10 or older this variant cannot work — a PC with a USB cable
is then the only rescue route.

```
pkg update
pkg install android-tools sqlite -y
# Developer options > Wireless debugging > ON
# tap "Pair device with pairing code" -> note the PAIRING port + code
adb pair localhost:PAIRPORT          # type the 6-digit code when asked
adb connect localhost:CONNECTPORT    # the port shown on the Wireless debugging screen
adb devices                          # must list localhost:CONNECTPORT  device
adb shell run-as io.github.pabi277.dayledger id     # must print uid=...  (proves debug build)
adb shell am force-stop io.github.pabi277.dayledger # close the DB so the WAL is flushed
mkdir -p ~/rescue
adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db > ~/rescue/dayledger.db
adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db-wal > ~/rescue/dayledger.db-wal 2>/dev/null || true
sqlite3 ~/rescue/dayledger.db "PRAGMA user_version; .tables"
```

Expected: `user_version` = `5`, and the table list contains `app_usage`,
`hourly_usage`, `app_info`, `categories`, `settings`, `notification_rules`,
`notification_log`, `blocks`, `day_plans`, `templates`, `template_items`,
`limit_events`, `earnings`, `learning`. Then keep a second copy and a hash
(`sha256sum ~/rescue/dayledger.db`) as evidence. `run-as` printing
`Package … is not debuggable` means the installed build is **not** a CI debug
artifact — stop and report, because the rescue route then does not exist.

Because this route uses the same `run-as` channel, the rescued file can also be
written back after the reinstall (app stopped first, stale `-wal`/`-shm` removed),
which avoids losing the history between the rescue and Phase 7's own restore path.

## 7. Gate status and the owner's next actions

Gate answered: data **valuable**, signing identity **approved**. What is left is
mechanical, and only the owner can do it:

1. Create the keystore outside the repository (§8a Step A).
2. Add the four repository **secrets by name** in GitHub (§8a Step B) and say only
   *"secrets added"* in chat — never the values.
3. Run the §6b rescue and report the verified result.
4. Say *go* for the uninstall/reinstall once the rescue is verified.

Phase 7 feature code (7.1–7.4) starts after step 3's rescue is confirmed, so the
first owner-key APK the owner installs already carries the migration and backup
work rather than a build he has to install twice.

## 8. Owner setup plan — one stable, owner-controlled personal sideload identity

Phone-friendly. Secret **names** only; values never appear here, in a commit, or
in chat.

**What was wrong before round 2, in one line:** every CI run invented a throwaway
signing key, so each APK was a different app to Android — that is why an update
fails.

### 8a. What the owner does (steps A–D unchanged by round 2)

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

Tell me only that the four names exist — never the values. The wiring that reads
them already landed in round 2 (§8b), so the next push after you add them will
sign the progress APK and the release with your key and print the certificate
fingerprint in the log and in the artifact.

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

### 8b. What round 2 wired (signing plumbing only)

Gradle (`app/build.gradle.kts`):

- A `signingConfigs` entry named `owner` is created **only** when
  `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
  `ANDROID_KEY_PASSWORD` are all present and the keystore file exists. It is
  applied to **both** `debug` and `release`, which is what makes the sideloaded
  progress APK and the release share one certificate.
- When the secrets are absent the file falls back to AGP's default debug key, so
  CI still produces a progress APK; that state is now loudly labelled instead of
  silent (§8b workflow).

Workflow (`.github/workflows/ci-build.yml`):

- The `test` job gains a `Prepare owner signing key on runner` step that decodes
  the base64 secret to `$RUNNER_TEMP/owner-sideload.jks` (mode 600) and exports
  `ANDROID_KEYSTORE_PATH`. With no secret it emits an explicit
  `::warning::` that this APK is throwaway-key signed and **cannot** be installed
  over an owner-key build; with a partial set it fails with the missing **name**.
- Both jobs gain `Record APK signing certificate`, which runs
  `apksigner verify --print-certs` and writes the signer DN and **SHA-256
  fingerprint** to the log and to `signing-certificate.txt` inside the uploaded
  artifact. A fingerprint is public information; a keystore value is not.
- The `signed-release` job keeps its hard failure when any of the four names is
  missing, so a release can never be produced with a throwaway key.
- Header comment rewritten to describe the one-identity rule and the names only.

**What this cannot prove yet:** with no secrets in the repository, the current run
still signs with a throwaway key and prints *that* fingerprint. Stability is proven
only by two runs **after** the owner adds the secrets showing the same SHA-256
fingerprint — that is the evidence this gate needs, and it is recorded here when it
exists.

### 8c. Round-2 CI record

| Item | Value |
|---|---|
| Green run | `39773260641` (`Android CI`, push, branch `arena/0276c412-personal-ueses-app`) |
| Tip commit | `3b72eb7` |
| Steps | `Prepare owner signing key on runner` ✅ (warns: secrets absent), `Run unit tests and build debug APK` ✅, `Record APK signing certificate` ✅, `Upload progress APK` ✅ |
| Artifact | `app-progress-apk`, id `12423721661`, 18,234,553 bytes, `sha256:c5902be1…7cf47a`, expires 2026-11-21 |
| Signed release job | skipped (branch build — main only) |
| Expected annotation | *"Owner signing secrets are absent: this app-progress-apk is signed by a throwaway debug key…"* |

Two earlier runs of this round are worth keeping: `39772365133` (docs-only commit
`92b4c96`, green) and `39773026332` (`8d5efe1`, failed at `configure :app` because a
`.kts` script resolves `java` to Gradle's `java` extension, so `java.io.File` was an
unresolved reference; fixed in `3b72eb7`). The failure was reported automatically as
issue **#12**, which is left open — closing issues needs the owner's command.

**Consequence to keep in mind until the owner's key exists:** no APK CI can produce
right now is installable over the build already on the phone, because every one of
them is signed by a *different* throwaway key. The next install the owner makes —
whoever signs it — is the one that needs the §6b rescue first.

## 9. Round 3 — parts 7.1–7.4 implemented and verified in CI

The owner chose option **A**: build everything that CI can prove, install nothing.
No APK was installed and nothing on the phone was touched. The installed build and
its data are exactly as they were.

### 9.1 What shipped, part by part

**7.1 — Schema baseline.** `exportSchema = true` with
`room.schemaLocation = app/schemas` in `app/build.gradle.kts`; CI now fails if Room
does not export `5.json`, prints its `identityHash`, and uploads it as the
`room-schema` artifact. Fictional fixtures for v1–v5 live in the migration tests
(`MigrationTest.seedV1/…/seedV5`) with representative keys, unicode labels, nulls
and both `INTEGER`/`TEXT` shapes; no owner data is involved. Database version is
still **5** and no checklist table exists.

**7.2 — Versioned SAF backup/restore.** `backup/Json.kt` (in-tree writer/parser),
`backup/BackupModel.kt`, `backup/BackupCodec.kt` (envelope: `formatVersion`,
`schemaVersion`, app id, app version, timestamp, tables → columns → rows),
`backup/RestorePlanner.kt` (pure accept/refuse logic), `backup/BackupService.kt`
(enumerates `sqlite_master`, so every table — including ones added in later phases —
is covered without a hand-maintained list; restore deletes and re-inserts inside one
transaction). Settings now has **Back up everything** (create document) and
**Restore from backup** (open document) with a visible result line, and the Phase 6
placeholder CSV is replaced by the real stored usage rows.

**7.3 — Non-destructive migrations.** `data/migrations/DatabaseMigrations.kt` holds
the four explicit steps; `DayLedgerDatabase` registers them and **no longer calls**
`fallbackToDestructiveMigration()`, so an unknown schema throws instead of wiping
history. Every step creates tables only — the full git history proves no column was
ever renamed, retyped or dropped, so no row is rewritten.

**7.4 — Android OS backup exclusion.** `android:allowBackup="false"`,
`android:fullBackupContent="@xml/backup_rules"` (legacy, API 23–30) and
`android:dataExtractionRules="@xml/data_extraction_rules"` (API 31+, both
`cloud-backup` and `device-transfer`), each excluding `root`, `file`, `database`,
`sharedpref` and `external`. CI verifies the merged manifest and the packaged APK
flags on every run.

### 9.2 CI evidence (this is CI evidence, not device evidence)

| Item | Value |
|---|---|
| Green run | `39777555069` · `Android CI` · branch `arena/0276c412-personal-ueses-app` |
| Tip commit | `30cee09` |
| Unit tests | 26 tests, all green: 7 migration, 9 backup codec, 9 restore planner, 1 scaffold |
| Steps | build ✅ · signing certificate record ✅ · **OS-backup exclusion in merged manifest ✅** · **Room schema export ✅** · `app-progress-apk` upload ✅ · `room-schema` upload ✅ |
| `app-progress-apk` | id `12425322526`, 18,280,337 bytes, `sha256:2f3d3793…39fd475` |
| `room-schema` | id `12425352488`, 1,805 bytes, `sha256:aab9e1e6…48fd6d` (`5.json`) |
| Signed release job | skipped (branch build; main only) |
| Expected annotation | owner signing secrets still absent — the APK is throwaway-key signed |
| Signer evidence | `signer-DC13157EC…` (run `39804639332`) vs `signer-B988DBD3…` (run `39805057123`) — **different**, see §5a |

Two failed runs preceded it and are kept as evidence: `39776380281` (first attempt,
schema-matrix assertion) and `39776972220` (same test after diagnostics were added).
Both failed for the same reason: the **test expectation** said `notnull=0` for
`INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL`, while SQLite correctly reports `1` —
i.e. the migration DDL matched Room exactly and the expectation was wrong. Fixed in
`30cee09`. Their auto-filed issues are **#12**, **#13** and **#14**; they stay open
because closing issues needs the owner's command.

### 9.3 What is proven, and what is not

Proven by CI: every supported upgrade path (v1, v2, v3, v4 and v5 starts) reaches
v5 with every source row intact; the migrated schema matches the v5 entity matrix
column for column; no migration statement contains `DROP`/`DELETE`/`TRUNCATE`/
`RENAME`; backups round-trip exactly through the codec (unicode, escaping, nulls,
reals, empty database); malformed, foreign and newer files are refused with
readable messages; the restore planner refuses before any delete.

**Not proven, and deliberately not claimed:** that the app launches with the new
Room setup on a real phone; that update-over-install works on the owner's device;
that an exported file restores into a real install; that a newer-signature APK
installs over the old one. Those are the owner's smoke check (§10).

### 9.4 Known gap carried into the next round

The generated `5.json` is uploaded by CI but its **checked-in copy is still
missing** from the repository, because artifact downloads are blocked in this
environment (see §4). Fetch the `room-schema` artifact from run `39777555069` and it
can be committed as-is under
`app/schemas/io.github.pabi277.dayledger.data.DayLedgerDatabase/5.json`; the
schema-matrix test already pins the same column facts, so nothing is unverified
meanwhile — only the canonical file is absent.

## 10. How this phase becomes Done

Two evidence lines, kept separate so CI evidence and owner evidence can never be
confused.

**CI/test evidence — ✅ collected (run `39777555069`):** every supported v1–v4 → v5
upgrade non-destructive, schema matrix match, backup/restore round trip, refusal
paths, merged-manifest OS-backup exclusion, `app-progress-apk` uploaded.

**Owner-device evidence — ⬜ still outstanding.** All of it needs one install, which
needs the one-time uninstall, which needs the database rescue first (§6b/§6c):

1. A `dayledger-backup-*.json` file exported from Settings → *Back up everything*.
2. That same file restored after a clear-data, with row counts unchanged.
3. The app launching normally on the new Room configuration (no crash dialog).
4. `app-progress-apk` installing **over** the installed build without uninstalling.
5. P0 usage numbers still correct, with any exact error text if they are not.
6. The certificate SHA-256 fingerprint printed by two separate runs being identical.

Phase 7 is **not Done** while any of those is missing, and it is not Done while the
signing secrets are absent. When the owner reports items 1–6, this record gains a
"Round 4 — owner device evidence" section quoting his words, and only then does the
roadmap row move to ✅.
