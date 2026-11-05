#!/bin/bash
# Publish MoMan to Cloudflare R2 with the Cloudflare API.
#
#   publish.sh site                  upload server/public/** as site/<path>
#   publish.sh release <apk> <json>  upload apk/<basename>, then releases.json
#   publish.sh verify                print /latest.json and HEAD its APK
#
# Credentials (never commit them):
#   CLOUDFLARE_API_TOKEN  - or a file at ${MOMAN_KEYS}/cf-api-token
#   CLOUDFLARE_ACCOUNT_ID - defaults to the MoMan account below
#   MOMAN_BUCKET          - defaults to moman-site
#
# This replaces the older Worker admin route (/ _admin/put), which needed the Worker's
# ADMIN_TOKEN binding. Writing to R2 directly needs only an API token with
# "Workers R2 Storage: Edit" and does not depend on the Worker being deployed.
set -euo pipefail

ACCOUNT=${CLOUDFLARE_ACCOUNT_ID:-c654f1f31d9b2e88e3681ae35e08f86b}
BUCKET=${MOMAN_BUCKET:-moman-site}
KEYS=${MOMAN_KEYS:-$HOME/moman-keys}
HOST=${MOMAN_HOST:-momanreader.com}
API="https://api.cloudflare.com/client/v4/accounts/$ACCOUNT/r2/buckets/$BUCKET/objects"

if [ -n "${CLOUDFLARE_API_TOKEN:-}" ]; then
  TOKEN=$CLOUDFLARE_API_TOKEN
elif [ -f "$KEYS/cf-api-token" ]; then
  TOKEN=$(tr -d '\r\n' < "$KEYS/cf-api-token")
else
  echo "no Cloudflare token: set CLOUDFLARE_API_TOKEN or write $KEYS/cf-api-token" >&2
  exit 2
fi

content_type() {
  case "$1" in
    *.html)      echo "text/html; charset=utf-8" ;;
    *.css)       echo "text/css; charset=utf-8" ;;
    *.js)        echo "text/javascript; charset=utf-8" ;;
    *.json)      echo "application/json" ;;
    *.svg)       echo "image/svg+xml" ;;
    *.png)       echo "image/png" ;;
    *.jpg|*.jpeg) echo "image/jpeg" ;;
    *.webp)      echo "image/webp" ;;
    *.ico)       echo "image/x-icon" ;;
    *.txt)       echo "text/plain; charset=utf-8" ;;
    *.xml)       echo "application/xml" ;;
    *.apk)       echo "application/vnd.android.package-archive" ;;
    *)           echo "application/octet-stream" ;;
  esac
}

put() { # <r2-key> <local-file>
  local key=$1 file=$2 code out
  out=$(mktemp)
  code=$(curl -sS -m 900 -o "$out" -w '%{http_code}' -X PUT \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: $(content_type "$file")" \
    --data-binary @"$file" "$API/$key")
  if [ "$code" = "200" ]; then
    echo "  ok   $key"
  else
    echo "  FAIL $key (HTTP $code)" >&2
    head -c 400 "$out" >&2; echo >&2
    rm -f "$out"
    exit 1
  fi
  rm -f "$out"
}

case "${1:-}" in
  site)
    cd "$(dirname "$0")/../server/public"
    find . -type f | sed 's#^\./##' | while IFS= read -r f; do put "site/$f" "$f"; done
    ;;
  release)
    apk=${2:?usage: publish.sh release <apk> <releases.json>}
    json=${3:?usage: publish.sh release <apk> <releases.json>}
    put "apk/$(basename "$apk")" "$apk"
    put "releases.json" "$json"
    echo "published $(basename "$apk"); https://$HOST/latest.json now points at it"
    ;;
  verify)
    curl -sS "https://$HOST/latest.json"; echo
    url=$(curl -sS "https://$HOST/latest.json" | sed -n 's/.*"arm64-v8a":"\([^"]*\)".*/\1/p')
    [ -n "$url" ] && curl -sSI "$url" | grep -i '^HTTP\|^content-length'
    ;;
  *)
    echo "usage: $0 site | release <apk> <releases.json> | verify" >&2
    exit 2
    ;;
esac
