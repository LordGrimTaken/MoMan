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
