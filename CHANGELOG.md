# Changelog

MoMan's release history. Mihon's historical changelog — everything before this project existed — is preserved
separately at [docs/CHANGELOG-MIHON.md](docs/CHANGELOG-MIHON.md).

## Unreleased

### Changed
- The update client now points at `https://momanreader.com` (the Cloudflare Worker + R2 bucket). The former
  `moman-updates.onrender.com` host is decommissioned.
- About screen: privacy policy and website link to MoMan's own pages; the inherited Mihon social links were removed.

### Fixed
- `:app:bundleRelease` failed while ABI splits were enabled; splits are now opt-out (`-PabiSplits=false`) so an
  Android App Bundle can be produced.

### Other
- Repository relicensed: MIT for MoMan's own work, Apache-2.0 retained for inherited Mihon code (see `NOTICE`).
- Added `docs/BUILDING.md`, `docs/ARCHITECTURE.md`, a rewritten README, and store-listing metadata for MoMan.

## 1.0.11 — 2026-10-22

- Redesigned the lower half of the You tab: modes card, library tiles, app card and a brand footer.

## 1.0.10 — 2026-10-22

- Updater fixes.

## 1.0.9 — 2026-10-22

- New launcher icon (Enso Seal) and refreshed site icons.

## 1.0.8 — 2026-10-22

- Release build and site polish.

## 1.0.7 — 2026-10-22

- Release build.

## 1.0.6 — 2026-10-22

- Discover feed, You tab layout and a repository guide.

## 1.0.5 — 2026-10-22

- Fixed a crash during initial setup.

## 1.0.4 — 2026-10-22

- First release build, arm64-v8a.

## 1.0.3 / 1.0.2 / 1.0.1 — 2026-10-22

- Test builds while validating the update pipeline.

## 1.0.0 — 2026-10-22

- First MoMan build: rebranded from Mihon (app name, package id `com.moman.reader`, icon, telemetry removed), plus
  the marketing site, release feed and update server.
