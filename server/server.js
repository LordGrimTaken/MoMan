// MoMan server: marketing site (public/), /latest.json, /apk/, /healthz.
// Dependency-free (Node core only).
const http = require('http');
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const PORT = process.env.PORT || 3000;
const APK_DIR = path.join(__dirname, 'apks');
const PUBLIC_DIR = path.join(__dirname, 'public');
const RELEASES = path.join(__dirname, 'releases.json');
const CANONICAL_HOST = 'momanreader.com';

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.webp': 'image/webp',
  '.ico': 'image/x-icon',
  '.txt': 'text/plain; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8',
  '.webmanifest': 'application/manifest+json',
  '.woff2': 'font/woff2',
};
const COMPRESSIBLE = new Set(['.html', '.css', '.js', '.json', '.svg', '.txt', '.xml', '.webmanifest']);

const SECURITY_HEADERS = {
  'X-Content-Type-Options': 'nosniff',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
  'X-Frame-Options': 'DENY',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=(), interest-cohort=()',
  'Content-Security-Policy':
    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; " +
    "font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'",
};

function readReleases() {
  return JSON.parse(fs.readFileSync(RELEASES, 'utf8'));
}

function baseUrl(req) {
  const proto = req.headers['x-forwarded-proto'] || 'http';
  return `${proto}://${req.headers.host}`;
}

function send(res, code, type, body, extra = {}) {
  res.writeHead(code, { 'Content-Type': type, ...SECURITY_HEADERS, ...extra });
  res.end(body);
}

// In-memory cache of static files (small site): { mtimeMs, buf, gz }
const fileCache = new Map();

function serveStatic(req, res, rel) {
  let decoded;
  try { decoded = decodeURIComponent(rel); } catch { return false; }
  if (decoded.includes('\0') || decoded.includes('\\')) return false;
  const file = path.resolve(PUBLIC_DIR, '.' + path.sep + decoded);
  if (file !== PUBLIC_DIR && !file.startsWith(PUBLIC_DIR + path.sep)) return false;
  let st;
  try { st = fs.statSync(file); } catch { return false; }
  if (!st.isFile()) return false;
  const ext = path.extname(file).toLowerCase();
  const type = MIME[ext];
  if (!type) return false;

  let entry = fileCache.get(file);
  if (!entry || entry.mtimeMs !== st.mtimeMs) {
    const buf = fs.readFileSync(file);
    entry = { mtimeMs: st.mtimeMs, buf, gz: COMPRESSIBLE.has(ext) && buf.length > 512 ? zlib.gzipSync(buf, { level: 9 }) : null };
    entry.etag = '"' + st.mtimeMs.toString(36) + '-' + buf.length.toString(36) + '"';
    fileCache.set(file, entry);
  }
  const isHtml = ext === '.html';
  const headers = {
    'Cache-Control': isHtml ? 'no-cache' : 'public, max-age=31536000, immutable',
    ETag: entry.etag,
    Vary: 'Accept-Encoding',
  };
  // Assets are referenced with ?v= cache-busters; robots/sitemap/favicon get a shorter TTL.
  if (!isHtml && /^(robots\.txt|sitemap\.xml|favicon\.svg|og\.svg|og\.png|img\/.+)$/.test(decoded)) {
    headers['Cache-Control'] = 'public, max-age=86400';
  }
  if (req.headers['if-none-match'] === entry.etag) {
    res.writeHead(304, { ...SECURITY_HEADERS, ...headers });
    res.end();
    return true;
  }
  let body = entry.buf;
  if (entry.gz && /\bgzip\b/.test(req.headers['accept-encoding'] || '')) {
    body = entry.gz;
    headers['Content-Encoding'] = 'gzip';
  }
  headers['Content-Length'] = body.length;
  res.writeHead(200, { 'Content-Type': type, ...SECURITY_HEADERS, ...headers });
  res.end(req.method === 'HEAD' ? undefined : body);
  return true;
}

function notFound(req, res) {
  const accept = req.headers.accept || '';
  const p404 = path.join(PUBLIC_DIR, '404.html');
  if (accept.includes('text/html') && fs.existsSync(p404)) {
    return send(res, 404, MIME['.html'], fs.readFileSync(p404), { 'Cache-Control': 'no-cache' });
  }
  send(res, 404, 'text/plain', 'Not found');
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://x');
  const host = (req.headers.host || '').toLowerCase().split(':')[0];

  // Canonical host + HTTPS redirects (health checks exempt).
  if (url.pathname !== '/healthz') {
    const wwwRedirect = host === 'www.' + CANONICAL_HOST;
    const httpRedirect = req.headers['x-forwarded-proto'] === 'http' && (host === CANONICAL_HOST || wwwRedirect);
    if (wwwRedirect || httpRedirect) {
      return send(res, 301, 'text/plain', 'Moved', { Location: `https://${CANONICAL_HOST}${req.url}` });
    }
  }

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
      ...SECURITY_HEADERS,
    });
    return fs.createReadStream(file).pipe(res);
  }

  if (req.method !== 'GET' && req.method !== 'HEAD') {
    return send(res, 405, 'text/plain', 'Method not allowed', { Allow: 'GET, HEAD' });
  }

  let rel = url.pathname;
  if (rel === '/') rel = '/index.html';
  else if (rel === '/privacy' || rel === '/privacy/') rel = '/privacy.html';
  if (serveStatic(req, res, rel.slice(1))) return;

  notFound(req, res);
});

server.listen(PORT, () => console.log(`MoMan server on :${PORT}`));
