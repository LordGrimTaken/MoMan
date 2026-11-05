# Architecture

A short tour of how the pieces fit. MoMan is a fork of Mihon, so the shape is inherited; the parts marked
**MoMan** are this project's own work.

## Module graph

```
app ──────────────┬── domain ── (models, use cases, no Android)
 │                ├── data ──── (SQLDelight DB, repositories, network, trackers, update client)
 │                ├── core/common, core/archive, core/metro
 │                ├── core-metadata
 │                ├── presentation-core, presentation-widget
 │                └── source-api, source-local
 └── i18n, icons
```

`app` holds the Compose UI and the Android entry points; `domain` and `data` are plain Kotlin/JVM where possible,
which is why most of the interesting logic is unit-testable without a device.

## Reading path

1. A **source** (an extension implementing `source-api`) returns `SManga` / `SChapter` / `Page` models.
2. `data` persists library state in SQLDelight and caches pages on disk.
3. `app`'s reader renders pages (Compose paged, long-strip, or the experimental high-quality renderer) and reports
   progress back through `domain` use cases.

## Extensions

`source-api` defines the contract; extensions are separate APKs loaded at runtime through a dedicated class loader.
The package names `eu.kanade.tachiyomi.*` and `tachiyomi.*` are part of that ABI and **must not be renamed** — doing
so breaks every existing extension. MoMan ships with no extensions and hosts no sources.

## Updates (MoMan)

`AppUpdateChecker` → `GetApplicationRelease` → `ReleaseServiceImpl` fetches
`https://momanreader.com/latest.json`, decodes it into `MomanRelease` (version, versionCode, changelog, per-ABI APK
URLs), picks the URL matching the device ABI, downloads it, and hands the file to Android's `PackageInstaller`.

The feed is produced by `server/` and published to Cloudflare R2 by `cloudflare/publish.sh`; `cloudflare/worker.js`
serves the site, `/latest.json` and `/apk/*`.

## Site and release pipeline (MoMan)

```
server/releases.json ─┐
server/public/*       ├─ publish.sh ─→ Cloudflare Worker ─→ R2 (moman-site)
release APK ──────────┘                        │
                                               └─→ https://momanreader.com/{latest.json,apk/*}
```

`server/server.js` is a dependency-free Node server that serves the same files locally for testing.

## Conventions worth knowing

- **Versioning** lives in `app/build.gradle.kts` (`versionCode`, `versionName`) and is mirrored into
  `server/releases.json` at release time. Bump both together; `versionCode` must never decrease.
- **Telemetry** is compiled out (`include-telemetry=false`); the Firebase/Crashlytics plugins are not applied.
- **Build variants**: `debug` (`.dev`), `release`, `foss` (`.foss`), `nightly` (`.debug`), `benchmark`.
  `Config.enableUpdater` is false for local builds and true for distributed ones.
- **i18n** is moko-resources; strings live in `i18n/src/commonMain/moko-resources/base/strings.xml` with ~70 locale
  overrides.
