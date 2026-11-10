#!/usr/bin/env bash
# One command for everything that proves this tree is shippable.
#
#   scripts/verify.sh            unit tests, both lints, the three guards
#   scripts/verify.sh --fast     guards + debug lint only (seconds, no compile)
#
# Exits non-zero on the first failure. Requires JAVA_HOME/ANDROID_HOME or a local.properties, as Gradle does.
set -euo pipefail
cd "$(dirname "$0")/.."

# Use a workspace-local toolchain when the environment does not provide one (this repo is often built on a host
# without a system JDK).
if [ -z "${JAVA_HOME:-}" ]; then
  for d in .toolchain/jdk-*; do
    [ -x "$d/bin/java" ] && export JAVA_HOME="$PWD/$d" && break
  done
fi
if [ -z "${ANDROID_HOME:-}" ] && [ -d .toolchain/android-sdk ]; then
  export ANDROID_HOME="$PWD/.toolchain/android-sdk"
  export ANDROID_SDK_ROOT="$ANDROID_HOME"
fi
[ -n "${JAVA_HOME:-}" ] && export PATH="$JAVA_HOME/bin:$PATH"

FAST=""
[ "${1:-}" = "--fast" ] && FAST=1

run() { printf '\n== %s\n' "$*"; "$@"; }

run ./gradlew --console=plain checkManifestClasses checkTranslations checkExportedComponents

if [ -z "$FAST" ]; then
  run ./gradlew --console=plain test
  run ./gradlew --console=plain :app:lintDebug :app:lintRelease
fi

printf '\nall checks passed\n'
