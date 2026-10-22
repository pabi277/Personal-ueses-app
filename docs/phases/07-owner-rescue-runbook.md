# Rescue the Day Ledger database from the installed build (one phone, Termux)

**Why this exists.** The build installed on the phone was signed by a throwaway CI key.
It can never be updated, only replaced — and replacing it means uninstalling, which
deletes `databases/dayledger.db`. The data in it is valuable (owner, 2026-10-22), so it
must be copied out **before** anything is installed. Nothing here changes the app or
its data; every step only reads.

**Environment:** one phone (vivo, Android 11+), no PC, no root, Wi-Fi available.
Termux from F-Droid. Total time: about 15 minutes.

**Golden rule:** do not uninstall, do not "clear data", do not install any APK until
step 8 has printed a verified `user_version = 5`.

---

## Step 1 — Install Termux (skip if already installed)

Install **Termux from F-Droid** (`f-droid.org`), not from Play — the Play build is
stale and its `android-tools` package is old. If Termux is already there, continue.

## Step 2 — Install the tools (once, ~2 minutes)

```bash
pkg update -y
pkg install -y android-tools sqlite openjdk-17
adb version
sqlite3 --version
```

- `adb version` must print a version line.
- `openjdk-17` is needed later for `keytool` (your signing key).
- If `pkg update` complains about mirrors, run it a second time; it recovers.

## Step 3 — Turn on the phone side

1. Settings → About phone → tap **Build number** 7 times → back → **System →
   Developer options**.
2. Turn **ON**: *USB debugging* (helps Funtouch start the pairing service) and
   **Wireless debugging**.
3. When the pop-up **“Allow wireless debugging on this network?”** appears, tick
   **“Always allow on this network”** and tap **Allow**.

Keep the phone on the same Wi-Fi for all of this.

## Step 4 — Pair (the step that was failing)

Tap **“Pair device with pairing code”**. A dialog shows a **6-digit code** and an
**IP address & Port**, e.g. `192.168.1.23:37142`.

> The port changes **every time** that dialog opens. Never reuse an old number, and
> never type a placeholder like `PAIRING_PORT` — `adb` needs the real digits, which is
> why the earlier attempts failed instantly with `protocol fault`.

Don't hunt for the number by eye. In Termux:

```bash
adb kill-server
adb mdns services
```

You will see the real pairing port in a line like:

```
adb-1A2B3C4D-XyZ123   _adb-tls-pairing._tcp.   192.168.1.23:37142
```

Pair with `127.0.0.1` — **not** `localhost`, which can resolve to IPv6 while adbd
listens on IPv4 (that also produces `protocol fault`):

```bash
adb pair 127.0.0.1:37142
```

Type the 6-digit code when asked. Success looks like:

```
Successfully paired to 127.0.0.1:37142 [guid=adb-…]
```

Pairing once per network is enough; Android remembers it.

## Step 5 — Connect (a different port)

Close the pairing dialog and read the port on the main **Wireless debugging** screen
under *“IP address & Port”* (often `:5555`, never the pairing port). Or ask adb:

```bash
adb mdns services        # look for _adb-tls-connect._tcp.
adb connect 127.0.0.1:5555
adb devices
```

Expect: `127.0.0.1:5555   device`. If it says `offline`: `adb disconnect`, then
`adb connect` again.

## Step 6 — Prove the private folder is readable (the gate for everything else)

```bash
adb shell run-as io.github.pabi277.dayledger id
```

- Expected: `uid=10xxx(u0_xxx) gid=…` → continue.
- `Package … is not debuggable` or `unknown package` → **stop**; the rescue route does
  not exist and a different plan is needed.

## Step 7 — Copy the database out

```bash
adb shell am force-stop io.github.pabi277.dayledger
mkdir -p ~/rescue
adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db > ~/rescue/dayledger.db
adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db-wal > ~/rescue/dayledger.db-wal 2>/dev/null || true
ls -l ~/rescue
```

`dayledger.db` must be clearly larger than 0 bytes.

## Step 8 — Verify it before trusting it

```bash
sqlite3 ~/rescue/dayledger.db "PRAGMA user_version;"
sqlite3 ~/rescue/dayledger.db ".tables"
sha256sum ~/rescue/dayledger.db
```

Expected:

- `user_version` = **5**
- tables include `app_info app_usage blocks categories day_plans earnings hourly_usage
  learning limit_events notification_log notification_rules settings template_items
  templates`
- one sha256 line — paste it back as evidence.

Then keep a second copy outside Termux's private storage:

```bash
termux-setup-storage        # allow access when asked
mkdir -p ~/storage/shared/Documents/dayledger-rescue
cp ~/rescue/dayledger.db ~/storage/shared/Documents/dayledger-rescue/
```

## Step 9 — Your signing key (still in Termux, never in the repo)

```bash
keytool -genkeypair -v -keystore ~/dayledger-owner.jks -alias dayledger-owner \
  -keyalg RSA -keysize 4096 -validity 10000
```

Enter a store password (twice), press **Enter** to reuse it as the key password, then
answer the name/organisation questions however you like (`Day Ledger`, `IN`, …).

Back it up twice — a lost keystore costs one more uninstall later:

```bash
cp ~/dayledger-owner.jks ~/storage/shared/Documents/dayledger-rescue/
sha256sum ~/dayledger-owner.jks
```

Never commit, upload or paste the `.jks` or its passwords.

## Step 10 — Four GitHub secrets, values yours alone

```bash
base64 -w0 ~/dayledger-owner.jks > ~/dayledger-owner.jks.b64
wc -c ~/dayledger-owner.jks.b64      # expect several thousand characters
termux-clipboard-set < ~/dayledger-owner.jks.b64
```

(`termux-clipboard-set` needs `pkg install termux-api` plus the Termux:API app; without
it, open the file in an editor and copy it.)

GitHub → repo → **Settings → Secrets and variables → Actions → New repository secret**,
four times:

| Name | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | contents of `~/dayledger-owner.jks.b64` |
| `ANDROID_KEYSTORE_PASSWORD` | store password from step 9 |
| `ANDROID_KEY_ALIAS` | `dayledger-owner` |
| `ANDROID_KEY_PASSWORD` | key password from step 9 |

Say only “secrets added” in chat — never the values.

## Step 11 — Only after CI confirms the fingerprint

Two runs must show the **same** signer SHA-256 (the CI artifact is named
`signer-<first 24 hex>`). Then:

1. Uninstall Day Ledger once (data is safe in `~/rescue` and Documents).
2. Download the `app-progress-apk` artifact, unzip, install, open.
3. Put the history back (app closed first):

```bash
adb shell am force-stop io.github.pabi277.dayledger
adb shell run-as io.github.pabi277.dayledger rm -f databases/dayledger.db-wal databases/dayledger.db-shm
adb shell run-as io.github.pabi277.dayledger sh -c 'cat > databases/dayledger.db' < ~/rescue/dayledger.db
```

4. Open the app; the numbers should be back. From then on every
   `app-progress-apk` installs **over** the previous one.

---

## When something fails

| Symptom | Cause | Fix |
|---|---|---|
| `protocol fault (couldn't read status message)` | placeholder or stale port, or IPv6 | reopen the pairing dialog, re-read the port, `adb pair 127.0.0.1:<real port>` |
| `adb mdns services` empty | pairing dialog closed, or old android-tools | reopen the dialog; `pkg upgrade -y android-tools`; `adb kill-server` |
| `error: no devices/emulators found` | not connected yet | `adb connect 127.0.0.1:<connect port>` |
| `device offline` | stale session | `adb disconnect` then `adb connect` again |
| `adb: unknown command pair` | old platform-tools | `pkg upgrade -y android-tools` |
| `run-as: Package … is not debuggable` | installed build is not a debug APK | stop; report the exact text |
| `sqlite3: error` or `user_version` = 0 | copy was empty | repeat steps 6–8, check `ls -l` size first |

## What this is not

It is not a substitute for the app's own SAF backup (Phase 7 part 7.2), it does not
touch the app's data, and it does not authorise installing anything. The rescued file
is a raw Room database, restored here through `run-as` on a debug build or through the
app's restore path once the versioned backup exists.
