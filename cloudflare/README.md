# MoMan on Cloudflare
momanreader.com is served by the `moman-site` Worker (worker.js) reading an R2 bucket (`moman-site`).
R2 keys: `site/<file>` website files, `apk/<name>` APKs, `releases.json` update metadata.
Publish with `./publish.sh site` or `./publish.sh release <apk> <releases.json>`.
