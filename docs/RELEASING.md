# Releasing MoMan

The steps that turn the tree into a release. Written from the 1.0.12/1.0.13 work; every command below was run in
that cycle. Nothing here needs the Harness — it needs the keystore.

## 0. Prerequisites

- `moman-release.jks` and its passwords. **This is the only irreplaceable input.** Certificate SHA-256 should be
  `1D:85:BC:09:BD:1F:51:8B:4F:F7:34:16:0C:DD:1F:EF:9E:6E:12:0C:9D:AF:BB:41:3B:FF:48:1D:22:29:82:27`
  (the key created 2026-11-05). If it differs, stop: a different key cannot install over an existing MoMan.
- A Cloudflare API token with **Workers R2 Storage: Edit**, at `${MOMAN_KEYS}/cf-api-token` or
  `CLOUDFLARE_API_TOKEN`.

## 1. Bump the version

`app/build.gradle.kts`: `versionCode` and `versionName`. `versionCode` must strictly increase — the updater compares it,
and Android refuses a lower one.

```bash
grep -n "versionCode\|versionName" app/build.gradle.kts
```

Add the `CHANGELOG.md` section for the version while you are there.

## 2. Prove the tree

```bash
./scripts/verify.sh          # guards + all unit tests + lint debug and release
```

## 3. Build and sign

```bash
echo "storeFile=/path/to/moman-release.jks
storePassword=…
keyAlias=…
keyPassword=…" > keystore.properties        # gitignored

./gradlew :app:assembleRelease               # per-ABI APKs
./gradlew :app:bundleRelease -PabiSplits=false   # if you also want an AAB
```

## 4. Verify the artefact before publishing

```bash
AAPT=$ANDROID_HOME/build-tools/37.0.0/aapt2
APK=app/build/outputs/apk/release/app-arm64-v8a-release.apk

$AAPT dump badging "$APK" | head -1          # package, versionCode, versionName
$ANDROID_HOME/build-tools/37.0.0/apksigner verify --print-certs "$APK"
#   -> certificate SHA-256 must equal the fingerprint in step 0.
#      If it does not, you are signing with the wrong key: stop, or every installed app will refuse the update.
sha256sum "$APK"
```

Then the release-only checks in [BUILDING.md](BUILDING.md#release-checks-beyond-a-successful-build): 16 KB alignment,
manifest class names, exported components, database schema version.

## 5. Publish to the site

```bash
cd cloudflare
MOMAN_KEYS=/path/to/keys ./publish.sh release ../app/build/outputs/apk/release/app-arm64-v8a-release.apk \
  ../server/releases.json
./publish.sh verify                          # prints latest.json and HEADs the APK
```

`server/releases.json` must name the new APK file, its version/versionCode, and the changelog shown in-app.

## 6. GitHub

```bash
git tag -a v1.0.13 -m "MoMan 1.0.13"
git push origin main v1.0.13
```

Create the release from the tag and attach the **signed** APK. Never attach a debug-signed build: it cannot install
over a published MoMan, and users see "app not installed" with no explanation.

## 7. Confirm from the outside

```bash
curl -s https://momanreader.com/latest.json | head -c 300
curl -sI https://momanreader.com/apk/<the-apk> | head -3
```

If `latest.json` advertises a version whose APK is missing or has the wrong hash, every installed client will offer an
update that cannot install. That is worse than not shipping.

## What has never been done

The release variant has never been *run* — only built, R8-verified and statically inspected. The first device that
installs a release build should be treated as the real test of the update path, the required-update dialog and the
restore flow.
