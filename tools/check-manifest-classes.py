#!/usr/bin/env python3
"""Fail if a manifest names an app class that does not exist in the source.

The namespace rename moved classes but left two `android:name` strings pointing at the old
package. Nothing caught it: the compiler does not resolve manifest class names, the manifest
merger only checks syntax, and unit tests never touch the file. The app crashed on launch
with ClassNotFoundException (fixed in c4764dc).

This checks every AndroidManifest.xml in the repo: relative names are resolved against the
module's `namespace`, and any app-owned class (com.moman / eu.kanade / tachiyomi / mihon)
must exist as a class, object or interface in some source file. Third-party names and intent
actions are ignored.

    python3 tools/check-manifest-classes.py          # exit 1 on a dangling reference
"""

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
APP_PREFIXES = ("com.moman.", "eu.kanade.", "tachiyomi.", "mihon.")
SKIP_DIRS = {"build", ".git", ".toolchain", ".gradle", "node_modules"}

DECL = re.compile(r'^\s*(?:public |internal |private |abstract |open |final |sealed |data )*'
                  r'(?:class|object|interface)\s+([A-Z]\w*)', re.M)


def source_classes() -> set[str]:
    """package.Class for every top-level declaration in the repo."""
    index: set[str] = set()
    for path in ROOT.rglob("*.kt"):
        if any(part in SKIP_DIRS for part in path.parts):
            continue
        text = path.read_text(errors="ignore")
        pkg = re.search(r'^package\s+([\w.]+)', text, re.M)
        if not pkg:
            continue
        for name in DECL.findall(text):
            index.add(f"{pkg.group(1)}.{name}")
    return index


def module_namespace(manifest: pathlib.Path) -> str | None:
    build = manifest.parent.parent / "build.gradle.kts"
    if not build.exists():
        build = manifest.parent.parent.parent / "build.gradle.kts"
    if not build.exists():
        return None
    m = re.search(r'namespace\s*=\s*"([\w.]+)"', build.read_text(errors="ignore"))
    return m.group(1) if m else None


def manifest_names(path: pathlib.Path) -> list[tuple[str, int]]:
    found = []
    for lineno, line in enumerate(path.read_text(errors="ignore").splitlines(), 1):
        for m in re.finditer(r'android:name="([^"]+)"', line):
            found.append((m.group(1), lineno))
    return found


def main() -> int:
    known = source_classes()
    problems = []
    checked = 0

    for manifest in ROOT.rglob("AndroidManifest.xml"):
        if any(part in SKIP_DIRS for part in manifest.parts):
            continue
        namespace = module_namespace(manifest)
        for name, lineno in manifest_names(manifest):
            if name.startswith("."):
                if not namespace:
                    continue
                fqn = namespace + name
            else:
                fqn = name
            if not fqn.startswith(APP_PREFIXES):
                continue
            # intent actions look like classes too; they are upper-case final segments with no declaration
            if fqn in known:
                checked += 1
                continue
            last = fqn.rsplit(".", 1)[-1]
            # an action constant is not a class and has no source declaration; allow those we cannot prove
            if last.isupper():
                continue
            problems.append(f"{manifest.relative_to(ROOT)}:{lineno}  {fqn}")

    print(f"manifest class references checked: {checked}")
    if problems:
        print("dangling manifest class references:")
        for p in problems:
            print("  " + p)
        return 1
    print("all app-owned manifest class references resolve")
    return 0


if __name__ == "__main__":
    sys.exit(main())
