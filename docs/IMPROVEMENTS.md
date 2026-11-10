# Improvement log

Continuous improvement work on MoMan, with the evidence for each cycle. Baseline measured on the Linux host
(`/srv/dsh/workspaces/moman-app`) on 2026-11-10: `:app:lintDebug` **3 errors / 127 warnings**,
`:domain:testDebugUnitTest` **6 tests**, cold-ish `:app:assembleDebug` **3m16s**, release arm64 APK **37.1 MB**.

| # | Area | Change | Evidence | State |
|---|---|---|---|---|
| 1 | Updater | Decide by `versionCode` instead of the version *name* | 6 tests pass (3 new); build 3m16s | shipped in `3bbe8b5` |
| 2 | Upcoming calendar | Read the locale observably so a language switch applies immediately | lint errors 3 → **0**; build 3m34s | shipped in `d0bd2fb` |
| 3 | Updater | Reject a downloaded APK that is not this app (package + signing cert) | 5 new tests pass; build 1m22s | shipped in `d509f8d` |

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
