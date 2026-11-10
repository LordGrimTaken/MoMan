#!/usr/bin/env python3
"""Fail when a manifest exports a component that is not on the reviewed allowlist.

Exported components are reachable by any app on the device. Most of the ones here are deliberate: the launcher
activity, the search/share activity, and the OAuth redirect handler. A new exported component added by accident (or a
copied manifest block) would be a silent hole, so it has to be named here to pass.

Add the component to ALLOWED with a comment saying why it must be exported.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SKIP = {"build", ".git", ".toolchain", ".gradle", "node_modules"}

# (module manifest, component name) -> why it is exported
ALLOWED = {
    ("app/src/main/AndroidManifest.xml", ".ui.main.MainActivity"):
        "launcher entry point plus the tachiyomi:// and mihon:// deep links",
    ("app/src/main/AndroidManifest.xml", ".ui.deeplink.DeepLinkActivity"):
        "receives SEARCH/SEND from the system and other apps; actions allowlisted in DeepLinkActions",
    ("app/src/main/AndroidManifest.xml", ".ui.setting.track.TrackLoginActivity"):
        "OAuth redirect target; state is verified for Comick and MangaBaka, PKCE protects MyAnimeList",
}

TAG = re.compile(r'<(activity|activity-alias|service|receiver|provider)\b([^>]*?)(/>|>(.*?)</\1>)', re.S)


def main() -> int:
    problems = []
    seen = 0
    for mf in ROOT.rglob("AndroidManifest.xml"):
        if any(part in SKIP for part in mf.parts):
            continue
        rel = mf.relative_to(ROOT).as_posix()
        for m in TAG.finditer(mf.read_text(errors="ignore")):
            attrs, body = m.group(2), m.group(4) or ""
            name = re.search(r'android:name="([^"]+)"', attrs)
            exported = re.search(r'android:exported="([^"]+)"', attrs)
            perm = re.search(r'android:permission="([^"]+)"', attrs)
            if not name or not exported or exported.group(1) != "true" or perm:
                continue
            seen += 1
            if (rel, name.group(1)) not in ALLOWED:
                problems.append(f"{rel}: {name.group(1)} is exported without a permission and is not allowlisted")

    print(f"exported components without a permission: {seen}")
    if problems:
        for p in problems:
            print("  " + p)
        return 1
    print("every exported component without a permission is reviewed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
