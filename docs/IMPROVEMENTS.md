# Improvement log

Continuous improvement work on MoMan, with the evidence for each cycle. Baseline measured on the Linux host
(`/srv/dsh/workspaces/moman-app`) on 2026-11-10: `:app:lintDebug` **3 errors / 127 warnings**,
`:domain:testDebugUnitTest` **6 tests**, cold-ish `:app:assembleDebug` **3m16s**, release arm64 APK **37.1 MB**.

| # | Area | Change | Evidence | State |
|---|---|---|---|---|
| 1 | Updater | Decide by `versionCode` instead of the version *name* | 6 tests pass (3 new); build 3m16s | shipped in `3bbe8b5` |
| 2 | Upcoming calendar | Read the locale observably so a language switch applies immediately | lint errors 3 → **0**; build 3m34s | shipped in `d0bd2fb` |
| 3 | Updater | Reject a downloaded APK that is not this app (package + signing cert) | 5 new tests pass; build 1m22s | shipped in `d509f8d` |
| 3b | Updater | Fix the API-26/27 crash lint found in cycle 3 | lint 4 errors → **0**; build 4m18s | shipped |
| 4 | Deep links | Only forward the actions the exported search activity declares | 3 new tests; build 1m57s | shipped |
| 5 | Updater download | Safe progress maths, internal cache file, no stale APK reuse | 5 new tests; lint still 0 errors | shipped |
| 6 | Updater | Enforce `minSupportedVersionCode`: required update, not dismissible | 9 tests in the interactor; lint 0 errors | shipped |
| 7 | Reader views | Accessibility click path for four touch-handling views | ClickableViewAccessibility 4 → 0; warnings 129 → 125 | shipped |
| 8 | ViewModels | Retire 8 StaticFieldLeak false positives with evidence | StaticFieldLeak 8 → 0; warnings 125 → 117 | shipped |
| 9 | Widgets | **Crash on launch**: manifest named the pre-rename receiver classes | emulator: 4 crash lines → 0, process starts | shipped |
| 10 | Tooling | `tools/check-manifest-classes.py` guards the bug class from cycle 9 | passes clean; fails when the bug is reintroduced | shipped |
| 11 | Release path | R8/release verified post-rename: every manifest class survives minification | release build 7m3s; 13/13 classes in dex | verified |
| 12 | Test + lint baseline | First whole-project run since the rename, debug and release | **141 tests, 0 failures**; release lint 0 errors | verified |
| 13 | Release runtime | Attempted again on a quiet host; interrupted by a DSH host restart | emulator booted, install not completed | **not verified** |
| 14 | Updater | ABI choice extracted into a pure, tested function | 4 new tests; build 1m54s | shipped |
| 15 | Updater | Progress throttle extracted and fixed: completion always emits | 5 new tests; build 28s | shipped |
| 16 | Release prep | versionCode 14 / 1.0.13 with a changelog for the twelve fixes | aapt2 reports 14 / 1.0.13 | staged |
| 17 | Tooling | The cycle-10 guard is now a Gradle task, not a script nobody runs | `gradlew checkManifestClasses`: exit 1 with the bug, 0 without | shipped |
| 18 | Updater | Skipping a version deletes the cached APK instead of leaving it in cache | full gate green: 150 tests, lint 0 errors | shipped |
| 19 | Translations | Placeholder guard + 3 locales that dropped their `%s` fixed | `gradlew checkTranslations`: exit 1 when broken, 0 when clean | shipped |
| 20 | Manifest | Exported-component allowlist guard; OAuth-state review recorded | `gradlew checkExportedComponents`: exit 1 on an injected export | shipped |
| 21 | Tooling | `scripts/verify.sh`: one command for tests, both lints and all three guards | full run: 3 stages, "all checks passed", exit 0 | shipped |
| 22 | Compatibility | Verified 16 KB page-size support on the release APK (Android 15+) | zipalign -P 16 passes; all 10 `.so` LOAD align 0x4000 | verified |
| 23 | Database | Verified migrations survived the `.sq` move; schema version unchanged | 19 `.sqm` files present; generated version 20 | verified |
| 24 | Backup | Backups carry a format version; a newer backup is refused, not partially restored | 4 new tests; build 2m30s | shipped |
| 25 | Branding | Renamed the last Mihon-named drawables and user-visible files; OAuth URIs documented | 154 tests, guards pass, build green | shipped |
| 26 | Release | Rebuilt the release variant at 1.0.13 after five commits; verified release-safety flags | R8 green 4m16s; code 14 / 1.0.13; not debuggable, no PreviewActivity, allowBackup=false | verified |
| 27 | Docs | `docs/RELEASING.md`: the release procedure, written from this cycle's real commands | linked from README; commands taken from the runs above | shipped |
| 28 | Export | CSV export reports failure instead of a false "exported" toast | build 1m54s; translation guard passes | shipped |

## Cycle 1 — updater compared names, not codes

**Hypothesis.** `latest.json` carries `versionCode` and the app parses it into `MomanRelease`, but the decision
used `versionName`. Names are display strings: debug builds append a commit count (`1.0.12-36`), and the comparison
regex strips punctuation, turning that into the components `[1, 0, 1236]`.

**Change.** `Release.versionCode` (defaulted, last position so positional callers keep compiling);
`ReleaseServiceImpl` forwards it; `Arguments.versionCode`; `AppUpdateChecker` passes `BuildConfig.VERSION_CODE`.
`versionCode` decides when the feed has one; feeds without it keep the old name comparison. The name comparison was
also rewritten to compare component-wise with missing parts as 0, so `1.1 == 1.1.0` and a shorter incoming version
can no longer throw.

**Next from here.** `minSupportedVersionCode` is still parsed and ignored.

## Cycle 2 — calendar labels ignored language changes

**Hypothesis.** Lint's three *errors* were all `NonObservableLocale`: `Locale.getDefault()` read inside composables.
Changing the app language would leave weekday names, the first day of the week and the `MMMM yyyy` title in the
previous language until the screen was recreated.

**Change.** Both composables read `LocalConfiguration.current.locales[0]`; the weekday list is `remember`ed keyed on
that locale.

## Cycle 3 — an unverified APK could be offered as an update

**Hypothesis.** The updater streamed the APK to cache and handed it to the installer with no identity check, even
though the README says it verifies the APK. A swapped or truncated file fails later, inside PackageInstaller, with a
message the user cannot act on.

**Change.** `ApkIdentity.isTrustedUpdate()` (pure, unit-tested) requires the same package id and identical SHA-256
certificate fingerprints, rejecting empty sets. `NewUpdateScreenModel` runs it inside the download coroutine, before
the state becomes `Downloaded`; failure deletes the file and reports `Failed`.

## Open candidates (not yet done)

- `minSupportedVersionCode` is sent by the feed and ignored by the app: no "this version is no longer supported" path.
- 127 lint warnings remain: 75 `PrivateResource` (fork-internal resources), 8 `StaticFieldLeak` (mostly
  application-context false positives, one Compose screen worth checking), 4 `ClickableViewAccessibility`,
  1 `UnsafeIntentLaunch` in `DeepLinkActivity`, dependency-update notices.
- The app cannot be exercised on a device from this host yet; Game Lab (`gamelab`) is the only emulator and belongs
  to the Game Lab session — UI-level verification is therefore not part of these cycles, and each change above is
  verified by compile + unit test only.

## Cycle 3b — lint caught a crash the build could not

**What happened.** Cycle 3 used `PackageInfo.signingInfo`, which is API 28, while `minSdk` is 26. `assembleDebug`
compiled it happily; `:app:lintDebug` then reported four `NewApi` **errors**, i.e. a `NoSuchMethodError` waiting on
Android 8.0/8.1. This is exactly why the lint task is part of every cycle here, not just the build.

**Fix.** `certFingerprints(PackageInfo)` uses `signingInfo` on API 28+ and the deprecated `signatures` field on
26–27. Lint is back to 0 errors.

## Cycle 4 — an exported component forwarded anything it was given

**Hypothesis.** `DeepLinkActivity` is exported with SEARCH/SEND/APPLICATION_PREFERENCES filters, yet it passed the
incoming intent straight to `MainActivity`. `MainActivity` acts on `ACTION_VIEW`: it installs an extension
repository from a `?url=` query parameter and opens `.tachibk` backup restores. Any installed app could reach those
flows through a component that never declared them.

**Change.** `DeepLinkActions.isForwardable()` (unit-tested) allowlists exactly the declared actions; anything else
finishes without forwarding, with `ACTION_VIEW` asserted false in the test.

**Lesson recorded in the work, not just the log:** the lint severities moved 0 → 4 *after* a "green" cycle — a
passing build is not evidence of API-safe code.

## Cycle 5 — three defects in one download path

**Hypotheses.** (a) Progress was `100 * bytesRead / contentLength`; a chunked response reports `-1`, so the
percentage went negative and the dialog could sit at a nonsense value. (b) The APK was written to
`externalCacheDir`, which is absent when external storage is unmounted and evictable at any time. (c) A stale
`update.apk` from a previous failed attempt could survive and be offered for install if the next attempt failed
before writing.

**Change.** `TransferProgress.percent()` (pure, unit-tested) returns `null` for an unknown total and clamps
overshoot; the listener skips updating on `null`. The file lives in the internal `cacheDir` and is deleted at the
start of every attempt.

**Measured after:** 5 new tests, lint 0 errors / 129 warnings, `:app:assembleDebug` 4m5s.

## Cycle 6 — the feed's minimum supported version was parsed and ignored

**Hypothesis.** `releases.json` sends `minSupportedVersionCode`; `MomanRelease` parses it; nothing reads it. A client
the server no longer supports saw the same optional prompt as any other update, and the skipped-version preference
could suppress it entirely.

**Change.** `Release.minSupportedVersionCode`; `NewUpdate(release, required)`; `required` is true when the running
`versionCode` is above 0 and below the feed's minimum, and it also forces an offer when the versions otherwise look
current. `MainActivity` ignores the skipped-version preference for a required update, and the screen hides "Not now"
and the skip action — the only exit is installing.

**Measured after:** interactor 9 tests, 0 failures; `:app:assembleDebug` + `:app:lintDebug` 3m42s, 0 lint errors.

## Cycle 7 — four touch-handling views could not be activated by an accessibility service

**Hypothesis.** `ClickableViewAccessibility` flagged `Pager`, `ReaderButton`, `WebtoonRecyclerView` and
`WebtoonStripImageView`: all override `onTouchEvent`, none expose `performClick()`, so a screen reader or switch
device has no way to activate them even though a finger can.

**Change.** The three gesture surfaces override `performClick()` and call it on `ACTION_UP`; they have no click
listener, so no double-fire. `ReaderButton` is a genuine button whose click already dispatches through
`super.onTouchEvent`, so it keeps the override but suppresses the "should call" half with that reason.

**Result.** `ClickableViewAccessibility` 4 → 0; warnings 129 → 125. One `UnsafeIntentLaunch` warning remains on
`DeepLinkActivity` even with a class-level suppression — the allowlist from cycle 4 makes it a false positive, and it
is recorded as such rather than claimed fixed.

## Cycle 8 — eight leak reports that were not leaks

**Hypothesis.** `StaticFieldLeak` flagged eight ViewModels holding a `Context`. `App.kt:84` builds the DI graph with
`create(context = this, ...)` where `this` is the `Application`, so the injected `Context` is the application context
and cannot retain an Activity. Lint cannot follow the generated graph.

**Change.** Each site carries `@SuppressLint("StaticFieldLeak")` and a comment naming the binding, so the
suppression is evidence-backed rather than a mute button.

**Measured after:** `StaticFieldLeak` 8 → 0, warnings 125 → 117, 0 errors, build 4m37s.

## Cycle 9 — the first defect found by running the app, not compiling it

**How it was found.** A dedicated headless AVD (`moman28`, Android 28, `-accel off`) was created from the
already-installed system image for this session (slot 6), because this host has no KVM. The app installed and
crashed immediately:

```
java.lang.ClassNotFoundException: tachiyomi.presentation.widget.UpdatesGridGlanceReceiver
```

`presentation-widget/src/main/AndroidManifest.xml` still named the pre-rename classes for both Glance receivers; the
batch-A namespace move put them under `com.moman.presentation.widget`. Build, unit tests and lint were all green —
none of them resolves manifest class names.

**Fix.** Both `android:name` values point at `com.moman.presentation.widget.*`.

**Verified on the device:** before, 4 crash lines and no process; after, the app starts and produces 0 crash lines.

**Limit reached:** the emulator's own `systemui` ANRs under software rendering with five emulators on twelve cores,
so the UI never painted. Process-level verification yes; screenshots no.

## Cycle 10 — automating the guard for cycle 9's bug class

**Reasoning.** The crash was a *string* in a manifest; no compiler, merger, test or lint rule resolves those. The
same mistake could be reintroduced by any future rename, so the fix belongs in tooling rather than a reminder.

**Change.** `tools/check-manifest-classes.py` builds an index of every class/object/interface in the repo, resolves
relative `android:name` values against each module's `namespace`, and exits non-zero on an app-owned reference that
does not exist. Intent actions and third-party names are skipped.

**Proved both directions:** clean tree → 13 references checked, exit 0; reintroducing the two old lines → both
reported, exit 1; restoring → clean again.

## Cycle 11 — the release path after the rename

**Why.** Cycles 1–10 were verified on the debug variant. 1.0.13 would ship the **release** variant, where R8 renames
and removes classes by rule; a manifest naming a stripped class is exactly the cycle-9 bug with a delay fuse.

**Measured.** `:app:assembleRelease` → BUILD SUCCESSFUL in 7m3s, five APKs (arm64 38 MB … universal 107 MB). Every
class named in the release manifest was then looked up in the R8 output dex:

```
checked 15 manifest names; 13 real classes all present; 2 "missing" are
com.moman.reader.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION (a permission string) and
eu.kanade.tachiyomi.SEARCH (an intent action) — not classes.
```

**Runtime attempt.** I tried to install and launch the release APK on the emulator. The emulated `system_server`
died again (`cmd: Can't find service: activity/package`), the same failure as the first attempt under software
rendering with other emulators running. The emulator was stopped and slot 6 removed. So the release variant is
verified **statically and by R8**, not by running it.

**Limitation now recorded twice:** this host (no KVM, shared CPU) can sustain a debug launch occasionally but cannot
be relied on for UI or release runtime checks.

## Cycle 12 — the whole suite, both variants

Earlier cycles ran `:domain:testDebugUnitTest` and `:app:testDebugUnitTest` with filters. The rename touched every
module, so this measured the whole project for the first time.

**Measured.** `./gradlew test :app:lintRelease` → BUILD SUCCESSFUL in 6m19s.

- **141 tests, 0 failures, 0 errors, 0 skipped** across all modules.
- release lint: **0 errors**, 117 warnings — the same profile as debug (75 `PrivateResource`, 20
  `NewerVersionAvailable`, 6 `AndroidGradlePluginVersion`, 4 `GradleDependency`, 3 `UnusedAttribute`).

**Conclusion.** No defect surfaced. That is the result, not a failure to find one: the rename did not break a test,
and the release variant does not add lint errors the debug one hides. The baseline is now on record so a future
regression has something to fail against.

## Cycle 13 — release runtime verification, second attempt

**Context.** Zero emulators were running and load was 4.3/12, the quietest window seen, so the release runtime check
from cycle 11 was retried: `moman28` booted in 108 s on 2.5 GB, then the release APK install was started.

**What happened.** The DSH host process restarted at 06:34:21 UTC mid-install. The emulator was gone afterwards,
`adb devices` was empty, and the install could not be confirmed. Slot 6 was removed.

**State of the claim.** The release variant is still verified **statically only** — R8 output contains all 13 classes
the manifest names, and release lint reports 0 errors. It has never been observed running.

**Two attempts, two different causes:** the first failed because the emulated `system_server` died under load; this
one because the host itself restarted. Neither was the app's fault, and neither says anything about the release
build's correctness — but the gap is real and stays recorded as a gap.

## Cycle 14 — the untested decision that gates every update

**Hypothesis.** `ReleaseServiceImpl.pickApk` walked `Build.SUPPORTED_ABIS` inline to choose the download. It decides
whether an update *can install* — a wrong pick downloads happily and then fails in PackageInstaller with
`INSTALL_FAILED_NO_MATCHING_ABIS` — and it had no test, because it depended on an Android type.

**Change.** `ReleaseApkSelector.pick(available, supportedAbis)` is pure and lives in `domain`: first device ABI the
feed offers, else `universal`, else null. `ReleaseServiceImpl` delegates; behaviour is unchanged, the decision is now
provable.

**Measured.** `ReleaseApkSelectorTest` 4 tests (primary ABI, second ABI, universal fallback, nothing installable), 0
failures; `:app:assembleDebug` 1m54s.

## Cycle 15 — the final progress tick was silently dropped

**Hypothesis.** The listener admitted a tick when `progress > saved && now - lastTick > 200`. The tick that reaches
100% often lands inside that window, so it was discarded and the bar sat one percent short until the stage flipped to
Downloaded.

**Change.** `TransferProgress.shouldEmit(...)` is pure: `done` always emits, otherwise real progress at most once per
interval. The listener delegates.

**Measured.** 10 tests in `TransferProgressTest`, 0 failures; `:app:assembleDebug` 28s.

**Two self-inflicted delays, recorded:** the function was appended outside the object literal (syntax error), then the
new tests used toy millisecond values instead of wall-clock ones, so "first tick emits" failed against a correct rule.

## Cycle 16 — release preparation

**Hypothesis.** The tree said 1.0.12 / versionCode 13 — the release already published — while containing twelve
fixes, one of which is a launch crash. Any release built from it would advertise itself as the previous version.

**Change.** `versionCode 14`, `versionName 1.0.13`, and a CHANGELOG section naming each fix, including the honest note
that `eu.kanade.tachiyomi.*` stays as the extension ABI.

**Measured.** Assembles; `aapt2 dump badging` reports `versionCode='14' versionName='1.0.13-65'`.

**Still blocked:** signing and publishing need `moman-release.jks`, which is not on this host. The tree is now one
signing step away from shipping.

## Cycle 17 — making the guard part of the build

**Reasoning.** Cycle 10 wrote `tools/check-manifest-classes.py`, but a script that has to be remembered protects
nothing; the bug it targets already shipped once.

**Change.** `./gradlew checkManifestClasses` (root build file, group `verification`) runs it.

**Proved through Gradle itself:** reintroducing the two old manifest lines → task fails, `exit 1`, output lists both
dangling references; restoring them → `exit 0`. My first attempt at this measurement piped the task through `grep`
and read *its* exit code, which reported success — the same pipeline mistake recorded in LESSONS.md, caught the same
round it was made.

## Cycle 18 — skipping an update left the download behind

**Hypothesis.** `skipUpdate` cancelled the job and recorded the skipped version, but a downloaded APK — tens of
megabytes — stayed in the internal cache until the system got round to evicting it.

**Change.** The file is deleted with the preference write; skipping is final for that version anyway.

**Measured with the whole gate rather than a rebuild:** `./gradlew test checkManifestClasses :app:lintDebug
:app:lintRelease` → BUILD SUCCESSFUL in 6m48s, **150 tests 0 failures**, lint **0 errors** on debug and release, the
manifest guard passing. (150 rather than 151 because the gate runs `test`, which does not include the app module's
instrumentation-free variant tests that the earlier per-module count did.)

## Standing limitation, restated each cycle

No release runtime observation: the keystore is absent and the emulator is unreliable on this host. Everything above
is compile, unit test, lint and static verification of the R8 output.

## Cycle 19 — translations missing their variables

**Hypothesis.** A locale that drops `%s` from a sentence loses the value the code passes. Nothing in the build checks
it; the result is a sentence with a hole, visible only to speakers of that language.

**Found 4, of which 3 are real:** `my/delete_category_confirmation`, `my/onboarding_guides_returning_user`,
`pl/download_queue_size_warning` had dropped their placeholder; `es/onboarding_storage_info` only reuses `%1$s`
twice, which is legal. The three entries were removed, so those strings fall back to English rather than rendering
incomplete.

**Guard.** `tools/check-translations.py` + `./gradlew checkTranslations`. Positional arguments compare as a set,
unnumbered ones by count. Proved both ways: clean → 0; re-breaking the Polish string → 1 with a precise message.

**Self-caught:** the guard crashed on its first run formatting the mismatch (`dict()` on a tuple) — a bug in the
checker, not in the translations, found because it was exercised both ways instead of only on a clean tree.

## Cycle 20 — exported components, and an OAuth finding I deliberately did not patch

**Audit.** Three app components are exported without `android:permission`: `MainActivity` (launcher, deep links),
`DeepLinkActivity` (SEARCH/SEND, actions allowlisted since cycle 4) and `TrackLoginActivity` (OAuth redirect). All
three are necessary; the risk is a fourth arriving unnoticed.

**Guard.** `tools/check-exported-components.py` + `./gradlew checkExportedComponents`: every exported component without
a permission must be named in an allowlist with a justification. Proved by injecting an exported
`.SneakyReceiver` → exit 1 naming it; clean tree → exit 0.

**Finding, not fixed.** `TrackLoginActivity` verifies OAuth `state` for Comick and MangaBaka, and MyAnimeList is
protected by PKCE; **AniList, Bangumi, Shikimori and Hikka accept a code with no state check**, so an attacker app
could in principle inject its own code (login CSRF — the user's progress would sync to the attacker's account).
Fixing that means changing flows for four services, each with its own OAuth behaviour, which I cannot test against
the live services from here. Patching them blind would risk breaking working logins; it is recorded for a decision
instead.

**Self-caught again:** my first attempt to prove the guard injected nothing (the replace target did not match) and
the guard "passed" on an unchanged file — a demonstration that proves nothing unless you check the setup landed.

## Cycle 21 — one verification command instead of five

**Reasoning.** By cycle 20 the checks were spread across five Gradle invocations (tests, two lint variants, three
guards). Individually each is easy to skip; collectively they are what makes a green tree trustworthy.

**Change.** `scripts/verify.sh` runs them in order and stops at the first failure; `--fast` runs only the guards. It
locates the workspace-local JDK/SDK under `.toolchain/` when the environment does not provide them, which matters on
this host.

**Measured.** Run with the environment deliberately stripped:

```
env -u JAVA_HOME -u ANDROID_HOME -u ANDROID_SDK_ROOT ./scripts/verify.sh
== checkManifestClasses checkTranslations checkExportedComponents
== test
== :app:lintDebug :app:lintRelease
all checks passed        EXIT=0
```

**Attempt note:** the first full run was interrupted by the harness and left no log, so it was re-run rather than
assumed — an unknown outcome is not a pass.

## Cycle 22 — 16 KB page sizes, checked rather than assumed

**Why.** Android 15+ devices can use 16 KB memory pages. A native library whose ELF `LOAD` segments are only 4 KB
aligned fails to load there, and the app dies at startup on exactly the newest hardware. Nothing in `assembleDebug`
checks it.

**Measured on the release APK.**

- `zipalign -c -P 16 -v 4 app-arm64-v8a-release.apk` → all entries `(OK)`, exit 0.
- ELF `LOAD` alignment, read from the binaries themselves:

```
libandroidx.graphics.path.so 0x4000   libarchive-jni.so 0x4000   libconscrypt_jni.so 0x4000
libimagedecoder.so 0x4000            libquickjs.so 0x4000       libresize.so 0x4000
libsqliteJni.so 0x4000               libssiv_crop.so 0x4000     libwebgpu_c_bundled.so 0x4000
libzstd-kmp.so 0x4000
```

**Result: no defect.** All ten are 16 KB aligned. The commands are now documented in `docs/BUILDING.md` under "Release
checks beyond a successful build" so the property is verified rather than assumed on the next release.

**My own measurement error, caught:** the first pass printed the wrong `readelf` column (file size instead of align)
and would have been meaningless; re-reading with `-lW` gave the real values.

## Cycle 23 — the migration set after the `.sq` move

**Why.** Batch A moved `src/main/sqldelight/tachiyomi/**` to `com/moman/**`. SQLDelight derives both the table packages
and the schema version from that tree. If the migration files had been dropped or ignored, the generated version would
fall back to 1, and the first update over an installed 1.0.12 (version 20) would either crash or wipe the user's
library.

**Measured.**

- `data/src/main/sqldelight/com/moman/migrations/` contains **1.sqm … 19.sqm**, all present.
- Generated `DatabaseImpl.Schema.version` → **20** (highest migration + 1), identical to the value the published
  1.0.12 was built with, so the upgrade path is unchanged.

**Result: no defect.** The check and the numbers are now in `docs/BUILDING.md` under "Release checks beyond a
successful build".

## Two rounds without a finding

Cycles 22 and 23 both looked for a real defect and found the code already correct. That is a legitimate outcome, and
it is also the clearest signal available: the static surface of this app has been audited to the point where further
rounds mostly re-verify what is already known.

## Cycle 24 — a newer backup restored "successfully" with data dropped

**Hypothesis.** `Backup` is protobuf with no format version. An unknown field is ignored, so restoring a backup written
by a newer build reported success while discarding every field this build cannot represent — silent data loss, shown
to the user as a clean restore.

**Change.** `@ProtoNumber(107) backupSchemaVersion` on the message; `BackupCreator` stamps
`BackupCompatibility.CURRENT`; `BackupFileValidator` refuses anything newer with a message naming both versions.
`BackupCompatibility.isRestorable(stored, current)` is pure and unit-tested.

**Compatibility kept:** pre-version backups (and Mihon backups) read as `0` and restore exactly as before; a newer
reader ignores the added field. Only "newer than this build" is refused.

**Measured.** 4 tests, 0 failures; `:app:assembleDebug` 2m30s.

**Not verified:** no restore was exercised on a device — this is compile, unit tests and code-path reasoning. The
refusal message reaches the user through an existing error path I did not observe running.

## Cycle 25 — Mihon names still visible to the user

**Found while auditing restore.** `createFileInCacheDir("mihon_restore_error.txt")` produced a file named after Mihon,
shown in the restore-complete notification. A sweep found four such files and two drawables:

| Kind | Was | Now |
|---|---|---|
| drawable | `ic_mihon`, `ic_mihon_splash` | `ic_moman`, `ic_moman_splash` (8 references, including the splash theme) |
| export | `mihon_library.csv` | `moman_library.csv` |
| logs | `mihon_crash_logs.txt` | `moman_crash_logs.txt` |
| logs | `mihon_restore_error.txt` | `moman_restore_error.txt` |
| logs | `mihon_update_errors.txt` | `moman_update_errors.txt` |

The drawable turned out **not** to be Mihon artwork — its path data is identical to `ic_launcher_foreground`, the MoMan
enso — so this is a naming fix with no visual change.

**Deliberately unchanged:** the OAuth redirect URIs (`mihon://bangumi-auth`, `app.mihon://comick-auth`, …) are
registered with each provider; renaming the scheme would break login until the provider is updated. Comments now say so
at all four call sites.

**Measured.** 154 tests, 0 failures; both guards pass; `:app:assembleDebug` green. One attempt failed with
"AAPT2 daemon startup failed" and passed on retry — environment, not code.

## Cycle 26 — the release artefact was stale, and now it is not

**Finding.** `app/build/outputs/apk/release/` still held the **1.0.12** build (versionCode 13, mtime 06:17) after five
commits had landed — including the backup format field and the resource renames. Anyone signing that file would ship
the previous code under the new version's expectations.

**Change.** Rebuilt `:app:assembleRelease`: BUILD SUCCESSFUL in 4m16s, R8 completing over the new code.

**Verified on the rebuilt artefact:**

```
package: name='com.moman.reader' versionCode='14' versionName='1.0.13'
debuggable: 0 occurrences        (a debuggable release would be a security hole)
PreviewActivity: absent          (no compose-tooling component in the shipping manifest)
allowBackup=false                (unchanged, deliberate)
size: 38 MB (arm64-v8a)
```

**Interrupted once:** the host restarted mid-build; the artefact was checked (still 1.0.12), the build was re-run rather
than assumed, and only the completed run is recorded.

## Cycle 27 — writing down the release procedure

**Reasoning.** Every round for the last twenty has ended at the same blocker: the keystore is not on this host. The
useful thing left to produce is the procedure that turns the staged tree into a release, so the person who *does* hold
the key does not have to rediscover it — including the two traps this session hit: a debug-signed APK cannot install
over a published one, and a `latest.json` pointing at a missing or wrong APK is worse than shipping nothing.

**Delivered.** `docs/RELEASING.md`: version bump, `scripts/verify.sh`, signing, artefact verification (certificate
fingerprint, 16 KB alignment, manifest classes, exported components, schema version), R2 publish via `publish.sh`,
GitHub tag and release, and an outside-in check that the feed and its APK agree. Commands are the ones actually run in
cycles 11, 22–26.

**Honest section included:** the release variant has been built and statically verified but **never run on a device**,
so the first install should be treated as the real test of the update path.

**Self-caught:** one line in the draft said the fingerprint "must be … NOT 1d85bc09" — self-contradictory at exactly the
moment it matters. Corrected to "must equal the fingerprint in step 0, if not you are signing with the wrong key".

## Cycle 28 — an export that never happened, reported as success

**Found while checking CSV escaping** (which is correct: quotes doubled, `\r`, `\n`, `"` and `,` all trigger quoting).

**Defect.** `LibraryExporter` invoked `onExportComplete()` *outside* the `use` block, unconditionally. A document provider
that refuses to open the URI makes `openOutputStream` return null, writes nothing — and the screen still toasted
"library exported". On a screen whose entire purpose is handing the user a copy of their library, that is the kind of
false confirmation that precedes data loss.

**Change.** `exportToCsv` returns `Boolean` (true only when the stream opened and the write finished); the caller toasts
`library_exported` or a new `library_export_failed`. Other locales fall back to English until translated.

**Measured.** `:app:assembleDebug` BUILD SUCCESSFUL 1m54s, `checkTranslations` passing with the new key.

**Not verified:** no export was performed on a device; the null-stream path is reasoned from `ContentResolver`'s
contract and not observed.
