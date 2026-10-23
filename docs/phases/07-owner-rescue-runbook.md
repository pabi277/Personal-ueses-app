# Phase 7 owner runbook — everything from Termux, nothing in Chrome

One phone, no PC, no root. Every step below runs in Termux; Chrome is only ever
used for the one GitHub login code (step 2), never for configuring secrets,
downloads or installs.

**Why this exists.** The installed build is signed by a throwaway CI key, so it can
never be updated — only replaced. Replacing means uninstalling, which deletes
`databases/dayledger.db`. That data is valuable (owner, 2026-10-22), so it is copied
out **first**. After step 12 the app has one stable, owner-controlled identity and
every later `app-progress-apk` installs over the previous one.

**Golden rule:** no uninstall, no "clear data", no installing an APK until step 7 has
printed `user_version = 5` **and** a second copy exists outside Termux.

---

## PART A — Tools (steps 1–2)

### 1. Packages

```bash
pkg update -y
pkg install -y android-tools sqlite openjdk-17 gh termux-api
adb version && sqlite3 --version && gh --version | head -n 1
```

If `pkg update` complains about mirrors, run it a second time; it recovers.
Termux must be the **F-Droid** build, not the Play one.

### 2. GitHub login, once (the only Chrome moment)

```bash
gh auth login
```

Choose: **GitHub.com** → **HTTPS** → **Login with a web browser**.
Termux shows a one-time code; open it in Chrome, paste the code, approve.
Back in Termux it prints `✓ Logged in to github.com as pabi277`.

```bash
gh auth status
gh api user -q .login          # must print pabi277
```

Nothing else needs a browser afterwards.

---

## PART B — Rescue the database (steps 3–7)

### 3. Phone switches

Settings → About phone → tap **Build number** 7× → **System → Developer options**:

- **USB debugging**: ON (vivo/Funtouch starts the wireless pairing service more
  reliably with it on)
- **Wireless debugging**: ON
- When **"Allow wireless debugging on this network?"** appears: tick **Always
  allow**, tap **Allow**

### 4. Pair and connect

Open **"Pair device with pairing code"** in Termux's split screen so the code and
port stay visible, then:

```bash
adb kill-server
adb mdns services                # shows real ports; the pairing one appears only while that dialog is open
```

Take the port from the `_adb-tls-pairing._tcp.` line and use **127.0.0.1**, not
`localhost`:

```bash
adb pair 127.0.0.1:<pairing-port>
```

Type the 6-digit code → `Successfully paired to 127.0.0.1:<port>`.
Close the pairing dialog, then connect with the **connect** port shown on the main
Wireless debugging screen (often `:5555`):

```bash
adb connect 127.0.0.1:<connect-port>
adb devices
```

### 5. Prove the private folder is readable

```bash
adb shell run-as io.github.pabi277.dayledger id
```

`uid=10xxx(u0_xxx)` → continue. `not debuggable` / `unknown package` → stop.

### 6. Copy the database

```bash
adb shell am force-stop io.github.pabi277.dayledger
mkdir -p ~/rescue
adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db > ~/rescue/dayledger.db
adb exec-out run-as io.github.pabi277.dayledger cat databases/dayledger.db-wal > ~/rescue/dayledger.db-wal 2>/dev/null || true
ls -l ~/rescue
```

### 7. Verify, then keep two copies

```bash
sqlite3 ~/rescue/dayledger.db "PRAGMA user_version;"      # must print 5
sqlite3 ~/rescue/dayledger.db ".tables"                    # 14 tables
sha256sum ~/rescue/dayledger.db
termux-setup-storage                                        # allow when asked
mkdir -p ~/storage/shared/Documents/dayledger-rescue
cp ~/rescue/dayledger.db ~/rescue/dayledger.db-wal ~/storage/shared/Documents/dayledger-rescue/ 2>/dev/null || true
```

---

## PART C — One stable signing identity (steps 8–12)

### 8. Create the key (typed twice, verified before anything leaves the phone)

```bash
set -u
R=pabi277/Personal-ueses-app
BR=arena/0276c412-personal-ueses-app

rm -f ~/dayledger-owner.jks
while :; do
  read -s -p "new password (letters+digits, 12+ chars): " P; echo
  read -s -p "type it again: " P2; echo
  [ "$P" = "$P2" ] && [ ${#P} -ge 12 ] && break
  echo "  no match or too short - retry"
done
keytool -genkeypair -v -keystore ~/dayledger-owner.jks -alias dayledger-owner \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Day Ledger,O=Personal,C=IN" -storepass "$P" -keypass "$P"
```

### 9. Local proof — hard stop

```bash
keytool -list -keystore ~/dayledger-owner.jks -storepass "$P" >/dev/null 2>&1 \
  && echo "keystore+password OK" \
  || { echo "BROKEN - nothing uploaded, stop here"; exit 1; }
```

Do not continue unless it prints **keystore+password OK**.

### 10. Upload the four secrets from Termux (`printf`, never `echo`)

```bash
printf '%s' "$P" | gh secret set ANDROID_KEYSTORE_PASSWORD  --repo "$R"
printf '%s' "$P" | gh secret set ANDROID_KEY_PASSWORD      --repo "$R"
printf '%s' 'dayledger-owner' | gh secret set ANDROID_KEY_ALIAS --repo "$R"
base64 -w0 ~/dayledger-owner.jks | gh secret set ANDROID_KEYSTORE_BASE64 --repo "$R"
gh secret list --repo "$R"      # four names, no values
```

### 11. Back up the key, keep the password somewhere safe

```bash
cp ~/dayledger-owner.jks ~/storage/shared/Documents/dayledger-rescue/
sha256sum ~/dayledger-owner.jks
unset P P2
```

Losing this file costs one more uninstall later, so keep a second copy elsewhere.

### 12. Run CI twice and compare the fingerprints

```bash
gh workflow run "Android CI" --repo "$R" --ref "$BR"
sleep 20
gh run list --repo "$R" --branch "$BR" --limit 2
```

After each run finishes:

```bash
gh run list --repo "$R" --branch "$BR" --limit 1 --json databaseId -q '.[0].databaseId' \
  | xargs -I{} gh api repos/$R/actions/runs/{}/artifacts -q '.artifacts[].name'
```

Expected: a `signer-<24 hex>` name (not `signer-unknown`), and the **same** name for
both runs. Same name twice = one stable certificate = the signing gate is satisfied.
Report the two names.

---

## PART D — Switch over (steps 13–15, only after both runs match)

### 13. Download the APK from Termux

```bash
cd ~
gh run download --repo "$R" --branch "$BR" --name app-progress-apk -D ~/apk
ls ~/apk
```

### 14. One uninstall, then install

Uninstall **Day Ledger** once (Settings → Apps). The data is already in
`~/rescue` and Documents.

```bash
termux-open ~/apk/app-debug.apk     # opens the system installer; allow "install unknown apps" for Termux
```

Open the app once so its database is created.

### 15. Put the history back

```bash
adb shell am force-stop io.github.pabi277.dayledger
adb shell run-as io.github.pabi277.dayledger rm -f databases/dayledger.db-wal databases/dayledger.db-shm
adb shell run-as io.github.pabi277.dayledger sh -c 'cat > databases/dayledger.db' < ~/rescue/dayledger.db
adb shell run-as io.github.pabi277.dayledger ls -l databases
```

Open the app; the numbers should be back. From now on every `app-progress-apk`
installs **over** the previous build — no more uninstalls.

### 16. Owner smoke check to report

1. APK installed over the previous build without uninstalling.
2. App launches; Today shows your history (not empty).
3. Settings → **Back up everything** → save `dayledger-backup-*.json`; note the
   "Backup written: 14 tables, N rows" line.
4. Settings → **Restore from backup** on that file → "Restore complete: N rows".
5. Restore a `.txt` file → must refuse with "Not a valid backup file", data unchanged.
6. Any exact error text, and whether P0 usage numbers look right.

---

## Failure table

| Symptom | Cause | Fix |
|---|---|---|
| `protocol fault (couldn't read status message)` | placeholder/stale port, or IPv6 `localhost` | reopen pairing dialog, re-read port, `adb pair 127.0.0.1:<digits>` |
| `adb mdns services` empty | pairing dialog closed; old android-tools | reopen dialog; `pkg upgrade -y android-tools`; `adb kill-server` |
| `unknown command pair` | old platform-tools | `pkg upgrade -y android-tools` |
| `device offline` | stale session | `adb disconnect` → `adb connect` |
| `run-as: not debuggable` | installed build is not a debug APK | stop; report exact text |
| `keystore+password OK` does not print | password typed differently the two times, or keytool failed earlier | rerun step 8; check step 8 output for errors |
| CI: `ANDROID_KEYSTORE_PASSWORD does not open the keystore` | secret ≠ store password | redo steps 8–10 without skipping step 9 |
| CI: `signer-unknown` | fingerprint tooling mismatch | report the run id; CI falls back to Gradle `signingReport` |
| `gh: not a git repository` | `gh workflow run` without `--repo` | always pass `--repo "$R"` |
