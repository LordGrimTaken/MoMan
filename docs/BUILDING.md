# Building MoMan

Written against the toolchain the project is developed with. Everything below was executed on Windows; the same
commands work on macOS/Linux with `./gradlew`.

## Toolchain

| Component | Version | Notes |
|---|---|---|
| JDK | **21** | `.github/.java-version` pins 21; Gradle 9.x requires it |
| Android SDK platform | **37.2** | `compileSdk` comes from `gradle/mihon.versions.toml` |
| Build-tools | **37.0.0** | `aapt2`, `apksigner` |
| Gradle | **9.8.2** | provided by the wrapper |
| AGP | **9.4.1** | from `gradle/libs.versions.toml` |
| Node | 18+ | only for `server/` and `cloudflare/` tooling |

`minSdk` is 26, `targetSdk` 36. The NDK version is declared project-wide but **no module has native sources**, so the
NDK is not required.

## First run

```bash
git clone https://github.com/LordGrimTaken/MoMan.git
cd MoMan
echo "sdk.dir=$HOME/Android/Sdk" > local.properties    # Windows: sdk.dir=C:/Users/you/Android/Sdk
./gradlew :app:assembleDebug
```

`local.properties` is untracked by design. On Windows use forward slashes: backslashes are escape characters in
`.properties` files and Gradle fails with a confusing path error.

Cold build: **~19 minutes**, ~1.7 GB of dependencies. Keep `GRADLE_USER_HOME` stable between builds or you pay that
again.

## Tasks

| Command | Result |
|---|---|
| `./gradlew :app:assembleDebug` | `app/build/outputs/apk/debug/` — one APK per ABI plus universal; package id `com.moman.reader.dev` |
| `./gradlew :app:assembleRelease` | per-ABI release APKs (requires signing, see below) |
| `./gradlew :app:bundleRelease -PabiSplits=false` | `app/build/outputs/bundle/release/app-release.aab` |
| `./gradlew :app:testDebugUnitTest` | JVM unit tests |
| `./gradlew :app:lintDebug` | Android lint |

### Why `-PabiSplits=false`

ABI splits produce one shrunk-resource set per ABI, and AGP refuses to build an app bundle from that:

```
Multiple shrunk-resources files found in …shrunk_resources_proto_format\release\minifyReleaseWithR8
Please disable building multiple APKs when building an Android app bundle.
```

The bundle flag turns splits off only for that build; APK builds keep them.

## Signing

Release builds need a keystore. Without one, AGP signs the release variant with the **local debug key** — fine for
smoke-testing, wrong for distribution:

- a debug-signed release cannot install over a published MoMan (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`);
- never generate a replacement keystore for an app that is already distributed — it permanently breaks updates.

Create `keystore.properties` in the repo root (untracked):

```properties
storeFile=/absolute/path/to/upload.jks
storePassword=…
keyAlias=…
keyPassword=…
```

For Play, Google holds the app-signing key and your keystore becomes the upload key.

## Verifying an artifact

Do not trust the Gradle file; read the APK:

```bash
$ANDROID_HOME/build-tools/37.0.0/aapt2 dump badging app/build/outputs/apk/debug/app-arm64-v8a-debug.apk | head -1
# package: name='com.moman.reader.dev' versionCode='…' versionName='…'
```

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `SDK location not found` | no `local.properties` / `ANDROID_HOME` | create it |
| Path syntax error in `local.properties` | backslashes | use `/` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | different signing key | install the debug `.dev` build, or sign with the real key |
| Kotlin compile OOM | heap | `gradle.properties` sets `-Xmx4g`; close other builds |
| Build cannot create files under `Documents` on Windows | Defender **Controlled Folder Access** | build outside protected folders |

## Release checks beyond a successful build

Run these against a **release** artifact before publishing it; none of them is exercised by `assembleDebug`.

**16 KB page sizes.** Android 15+ devices may use 16 KB pages, and a native library whose ELF `LOAD` segments are
4 KB-aligned fails to load there. Both halves must hold: the APK entries zip-aligned, and the ELF segments aligned.

```bash
# 1. zip alignment (build-tools 35+)
$ANDROID_HOME/build-tools/37.0.0/zipalign -c -P 16 -v 4 app-arm64-v8a-release.apk

# 2. ELF alignment of every bundled library: the last column must be 0x4000
unzip -o -q app-arm64-v8a-release.apk 'lib/*/*.so' -d /tmp/elfchk
for f in /tmp/elfchk/lib/*/*.so; do
  printf '%-40s %s\n' "$(basename "$f")" "$(readelf -lW "$f" | awk '$1=="LOAD"{print $NF}' | sort -u | tr '\n' ' ')"
done
```

Measured on 1.0.13: zipalign passes, and all ten libraries (`libarchive-jni`, `libsqliteJni`, `libquickjs`,
`libwebgpu_c_bundled`, …) report `0x4000`.

**Manifest class names.** `./gradlew checkManifestClasses` — a rename once left two receivers pointing at classes
that no longer existed, and the app crashed on launch.

**Exported components.** `./gradlew checkExportedComponents` — every exported component without a permission must be
on the reviewed allowlist.

**Database schema version.** `data/src/main/sqldelight/**/migrations/*.sqm` must stay intact and the generated
`DatabaseImpl.Schema.version` must match what the published build used, or the first update after installing will
migrate from a version the app no longer knows and lose or crash on the user's library.

```bash
ls data/src/main/sqldelight/com/moman/migrations/*.sqm | wc -l      # 19 files
grep -A2 "override val version" \
  data/build/generated/sqldelight/code/Database/debug/com/moman/data/data/DatabaseImpl.kt
# get() = 20   (db version = highest migration + 1; unchanged by the package rename)
```
