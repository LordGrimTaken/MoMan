#!/bin/bash
# Publish to the live MoMan site on Cloudflare (R2 via the Worker's authenticated upload route).
#   publish.sh site                      upload every file in server/public
#   publish.sh release <apk> <json>      upload an APK, then releases.json (switches /latest.json to it)
# The admin token lives in /root/moman-keys/cf-admin-token (never commit it).
set -euo pipefail
HOST=${MOMAN_HOST:-momanreader.com}
TOKEN=$(cat /root/moman-keys/cf-admin-token)
put() { # key file
  code=$(curl -s -m 600 -o /dev/null -w '%{http_code}' -X PUT -H "Authorization: Bearer $TOKEN" --data-binary @"$2" "https://$HOST/_admin/put/$1")
  echo "$code $1"; [ "$code" = 200 ] || exit 1
}
case "${1:-}" in
  site)
    cd "$(dirname "$0")/../server/public"
    find . -type f | sed 's#^\./##' | while read -r f; do put "site/$f" "$f"; done ;;
  release)
    apk=$2; json=$3
    put "apk/$(basename "$apk")" "$apk"
    put releases.json "$json" ;;
  *) echo "usage: $0 site | release <apk> <releases.json>"; exit 2 ;;
esac
