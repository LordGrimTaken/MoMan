#!/usr/bin/env python3
"""Fail when a translation's format placeholders do not match the base string.

A locale that drops `%s` from a sentence loses the variable the code passes (the category name disappears from
"Delete this category?"), and one that invents an extra placeholder can throw at runtime. Neither is caught by the
compiler, and both only show up in that one language.
"""
import collections
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "i18n/src/commonMain/moko-resources"
POSITIONAL = re.compile(r"%(\d+)\$[sd]")
UNNUMBERED = re.compile(r"%(?!\d+\$)[sd]")


def load(path):
    text = path.read_text(errors="ignore")
    out = {}
    for m in re.finditer(r'<string name="([^"]+)"[^>]*>(.*?)</string>', text, re.S):
        body = re.sub(r"\\%", "", m.group(2))
        # A positional argument may be reused ("%1$s ... %1$s") without harm; what matters is the set of indices
        # the sentence expects, plus the count of unnumbered ones.
        out[m.group(1)] = (frozenset(POSITIONAL.findall(body)), len(UNNUMBERED.findall(body)))
    return out


def main():
    base = load(RES / "base/strings.xml")
    problems = []
    for f in sorted(RES.glob("*/strings.xml")):
        if f.parent.name == "base":
            continue
        locale = load(f)
        for name, want in base.items():
            if name not in locale:
                continue
            if locale[name] != want:
                problems.append(
                    f"{f.parent.name}: {name}  base=positional{sorted(want[0])}+{want[1]}unnumbered"
                    f" translation=positional{sorted(locale[name][0])}+{locale[name][1]}unnumbered",
                )
    print(f"translation strings checked against {len(base)} base entries")
    if problems:
        print("placeholder mismatches:")
        for p in problems:
            print("  " + p)
        return 1
    print("all translations match the base placeholders")
    return 0


if __name__ == "__main__":
    sys.exit(main())
