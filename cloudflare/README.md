# MoMan on Cloudflare

`momanreader.com` is served by the **`moman-site`** Worker (`worker.js`) reading the R2 bucket of the same name.

| R2 key | Contents |
|---|---|
| `site/<path>` | the website (`server/public/**`) |
| `apk/<name>` | release APKs |
| `releases.json` | update metadata; the Worker rewrites `apks` to absolute URLs for `/latest.json` |

## Publishing

```bash
export CLOUDFLARE_API_TOKEN=...     # or put it in ${MOMAN_KEYS:-$HOME/moman-keys}/cf-api-token
./publish.sh site                   # upload server/public/**
./publish.sh release ../app-arm64-v8a-release.apk ../server/releases.json
./publish.sh verify                 # print /latest.json and HEAD its APK
```

`CLOUDFLARE_ACCOUNT_ID` (defaults to the MoMan account) and `MOMAN_BUCKET` (defaults to `moman-site`) can override
the target.

## Why the API instead of the Worker route

The script used to `PUT https://momanreader.com/_admin/put/<key>` and authenticate with the Worker's `ADMIN_TOKEN`
binding. That worked, but it coupled publishing to the Worker being deployed with the right secret, and the token
lived only on the build machine. Writing straight to R2 needs an API token with **Workers R2 Storage: Edit** and
nothing else; the Worker stays a read-only front door.

The token needs exactly two permissions: `Workers R2 Storage: Edit` (bucket objects) and, only if you want to touch
the Worker itself, `Workers Scripts: Read/Edit`. Create it under **My Profile → API Tokens**.

## Notes

- `worker.js` still contains the `/_admin/put` route for compatibility; it is unused by this script. Removing it
  later is safe once nothing calls it.
- Never commit the token or the signing keystore.
