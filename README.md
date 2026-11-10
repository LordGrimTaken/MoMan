# 墨漫 MoMan

**墨 (mò) = ink · 漫 (màn) = manga/manhua.** A calm, fast Android reader for manhua, manga and webtoons — built
around long-strip reading, a library you own, and no ads or trackers.

**[Download from momanreader.com](https://momanreader.com)** · Android 8.0+ · arm64-v8a

MoMan is an independent Android reader, built and maintained by its own contributors. It ships with **no content of
its own**: sources are supplied by third-party extensions that you install yourself. It stands on the open-source
Mihon/Tachiyomi lineage — that history and its licence are recorded in [NOTICE](NOTICE) and
[LICENSE-APACHE](LICENSE-APACHE).

> **MoMan hosts nothing.** It is a reader. What you read through it comes from extensions you choose to add.

---

## If you just want to use it

1. Open **[momanreader.com](https://momanreader.com)** on your phone and download the APK.
2. Allow installation from your browser when Android asks.
3. Open MoMan, add an extension repository in **You → Sources**, and install the sources you want.

Updates arrive through the app: it checks `momanreader.com/latest.json`, verifies the APK, and hands it to Android's
installer. No account, no Play Store, no analytics.

---

## Features

- **Gapless long-strip reader** — vertical continuous scrolling by default, the way webtoons and manhua are drawn.
- **Library you own** — categories, read state, download queue, backups; everything stays on the device.
- **Extension sources** — install third-party sources; the app itself ships with none.
- **Tracking** — AniList, MyAnimeList, Kitsu, Shikimori, Bangumi, MangaBaka, MangaUpdates, Hikka, Comick, Kavita,
  Komga and Suwayomi.
- **Downloads** — per-chapter downloads with a queue, storage picker and automatic cleanup.
- **Reader options** — paged and long-strip modes, page transitions, dual-page spreads, colour filters, brightness
  and orientation controls.
- **Ink & Seal theme** — ink-dark surfaces, a vermilion seal accent and a paper-light mode, plus Material You and a
  dozen community palettes.
- **No ads, no analytics, no telemetry.** Crash reporting is compiled out (`include-telemetry=false`).

Screenshots and the visual tour live on the [website](https://momanreader.com).

---

## Build from source

Requirements: **JDK 21**, **Android SDK with platform 37.2** and **build-tools 37.0.0**. NDK is declared but not
used — no module has native sources.

```bash
git clone https://github.com/LordGrimTaken/MoMan.git
cd MoMan

# point Gradle at your SDK (local.properties is not tracked)
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

./gradlew :app:assembleDebug          # debug APK, package com.moman.reader.dev
./gradlew :app:bundleRelease -PabiSplits=false   # Play-style AAB
./gradlew :app:assembleRelease        # per-ABI release APKs (needs a signing config)
```

Artifacts:

```
app/build/outputs/apk/debug/       app-arm64-v8a-debug.apk   (and other ABIs)
app/build/outputs/bundle/release/  app-release.aab
```

Notes that save time:

- **ABI splits are on by default.** They are incompatible with an app bundle, hence `-PabiSplits=false` for
  `bundleRelease`.
- **Release builds need a keystore.** Without `keystore.properties` the release variant is signed with the local
  debug key, which cannot update an installed MoMan. Use the debug build (`.dev` package, installs alongside) for
  testing.
- The first cold build takes ~19 minutes and resolves ~1.7 GB of dependencies; later builds are incremental.
- Full details: [docs/BUILDING.md](docs/BUILDING.md) · release procedure: [docs/RELEASING.md](docs/RELEASING.md).

---

## Project layout

| Path | What lives there |
|---|---|
| `app/` | Application module: UI (Compose), reader, library, downloads, settings, entry points |
| `domain/` | Use cases and models — no Android dependencies |
| `data/` | Persistence (SQLDelight), repositories, network, trackers, the update client |
| `core/common`, `core/archive`, `core/metro` | Shared utilities, archive/EPUB readers, DI graph |
| `core-metadata` | ComicInfo and Tachiyomi metadata formats |
| `presentation-core`, `presentation-widget` | Shared Compose components and the Glance widgets |
| `source-api`, `source-local` | The extension API and the local-file source |
| `i18n` | Translations (moko-resources) |
| `icons` | Material Symbols and Simple Icons used by the UI |
| `server/` | The marketing site and release feed (`/latest.json`, `/apk/*`) — plain Node, no dependencies |
| `cloudflare/` | Worker + R2 publishing used by momanreader.com |
| `gradle/build-logic` | Convention plugins, version catalog, SDK levels |

---

## How updates work

`server/releases.json` is the single source of truth for a release:

```json
{
  "version": "1.0.11",
  "versionCode": 12,
  "minSupportedVersionCode": 1,
  "changelog": "…",
  "apks": { "arm64-v8a": "MoMan-1.0.11-arm64-v8a.apk" }
}
```

The app fetches `https://momanreader.com/latest.json`, picks the APK for the device ABI, downloads it, and opens
Android's installer. Nothing is uploaded; no account is involved.

---

## Contributing

Issues and pull requests are welcome — read [CONTRIBUTING.md](CONTRIBUTING.md) and
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) first. Please do not open issues asking for content or sources; that is not
what this project distributes.

Translations live in `i18n/src/commonMain/moko-resources/` and are always welcome.

---

## License

- **MoMan's own work** — the rebrand, the update client, `server/`, `cloudflare/`, `docs/` — is **MIT** (see
  [LICENSE](LICENSE)).
- **Code inherited from Mihon** stays under **Apache License 2.0**; the full text is kept in
  [LICENSE-APACHE](LICENSE-APACHE) and the attribution is recorded in [NOTICE](NOTICE).
- Mihon's historical changelog is preserved at [docs/CHANGELOG-MIHON.md](docs/CHANGELOG-MIHON.md).

---

## Disclaimer

MoMan is not affiliated with Mihon, Tachiyomi, Google, or any content provider. It does not host, provide, or
condone piracy. Extensions are third-party software; you are responsible for what you install and for complying with
the law where you live. The app is not distributed through Google Play.
