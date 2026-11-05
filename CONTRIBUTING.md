# Contributing to MoMan

Thanks for wanting to help. This is a small fork; the rules are short.

## Reporting a problem

Open an [issue](https://github.com/LordGrimTaken/MoMan/issues) and include:

- what you did, what you expected, what happened;
- the app version (**You → About**) and your Android version and device;
- a log if you can get one (`adb logcat`), trimmed to the failure.

Please **do not** open issues asking for content, sources, or extensions. MoMan ships with none and does not
distribute them.

## Pull requests

1. Fork, branch from `main`, keep the change focused.
2. Build it: `./gradlew :app:assembleDebug` — see [docs/BUILDING.md](docs/BUILDING.md) for the toolchain.
3. Run the checks: `./gradlew :app:testDebugUnitTest` and, if you touched UI or platform code,
   `./gradlew :app:lintDebug`.
4. Formatting is enforced with Spotless: `./gradlew spotlessApply` before committing.
5. Describe the change and how you verified it. A screenshot or a test result beats a promise.

Do not rename the `eu.kanade.tachiyomi.*` or `tachiyomi.*` packages — they are the extension ABI, and renaming them
breaks every installed extension.

## Translations

Strings live in `i18n/src/commonMain/moko-resources/`. Add or fix a locale there and open a PR; `base/strings.xml`
is the English source of truth.

## Forks

Forks are welcome under the terms in [LICENSE](LICENSE) and [NOTICE](NOTICE). If you publish one, change the app
name, the icon, the `applicationId` and — importantly — the update URL in
`app/src/main/java/eu/kanade/tachiyomi/data/updater/AppUpdateChecker.kt`, so your builds do not try to update over
someone else's.

## Conduct

By participating you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).
