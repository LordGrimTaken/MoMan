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
