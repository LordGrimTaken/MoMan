// MoMan update server: serves /latest.json, APKs under /apk/, and a simple download page.
// Dependency-free (Node core only).
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const APK_DIR = path.join(__dirname, 'apks');
const RELEASES = path.join(__dirname, 'releases.json');

function readReleases() {
  return JSON.parse(fs.readFileSync(RELEASES, 'utf8'));
}

function baseUrl(req) {
  const proto = req.headers['x-forwarded-proto'] || 'http';
  return `${proto}://${req.headers.host}`;
}

function send(res, code, type, body, extra = {}) {
  res.writeHead(code, { 'Content-Type': type, ...extra });
  res.end(body);
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://x');

  if (url.pathname === '/healthz') return send(res, 200, 'text/plain', 'ok');

  if (url.pathname === '/latest.json') {
    const r = readReleases();
    const base = baseUrl(req);
    const apks = {};
    for (const [abi, file] of Object.entries(r.apks || {})) apks[abi] = `${base}/apk/${file}`;
    return send(res, 200, 'application/json', JSON.stringify({ ...r, apks }), {
      'Cache-Control': 'no-cache',
    });
  }

  if (url.pathname.startsWith('/apk/')) {
    const name = path.basename(decodeURIComponent(url.pathname.slice(5)));
    const file = path.join(APK_DIR, name);
    if (!name.endsWith('.apk') || !fs.existsSync(file)) return send(res, 404, 'text/plain', 'Not found');
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Length': fs.statSync(file).size,
      'Content-Disposition': `attachment; filename="${name}"`,
    });
    return fs.createReadStream(file).pipe(res);
  }

  if (url.pathname === '/') {
    const r = readReleases();
    const links = Object.entries(r.apks || {})
      .map(([abi, f]) => `<li><a href="/apk/${f}">${abi}</a></li>`)
      .join('') || '<li>No APK published yet.</li>';
    return send(res, 200, 'text/html; charset=utf-8',
      `<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>墨漫 MoMan</title>
<body style="font-family:system-ui;background:#15110f;color:#f4ede4;max-width:480px;margin:10vh auto;padding:0 20px">
<h1>墨漫 MoMan</h1><p>Latest version: <b>${r.version}</b></p><ul>${links}</ul><p>${r.changelog}</p></body>`);
  }

  send(res, 404, 'text/plain', 'Not found');
});

server.listen(PORT, () => console.log(`MoMan update server on :${PORT}`));
