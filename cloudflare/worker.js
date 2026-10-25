// MoMan site on Cloudflare: Worker + R2 bucket (binding BUCKET).
// R2 keys: site/<path> (website files), apk/<name> (APKs), releases.json (update metadata).
// Authenticated uploads: PUT /_admin/put/<key>  (Authorization: Bearer ADMIN_TOKEN).
const CANONICAL_HOST = 'momanreader.com';

const MIME = {
  '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8', '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.webp': 'image/webp',
  '.ico': 'image/x-icon', '.txt': 'text/plain; charset=utf-8', '.xml': 'application/xml; charset=utf-8',
  '.webmanifest': 'application/manifest+json', '.woff2': 'font/woff2',
};

const SECURITY = {
  'X-Content-Type-Options': 'nosniff',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
  'X-Frame-Options': 'DENY',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=(), interest-cohort=()',
  'Content-Security-Policy':
    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; " +
    "font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'",
};

function respond(body, status, type, extra) {
  const h = new Headers({ ...SECURITY, ...(extra || {}) });
  if (type) h.set('Content-Type', type);
  return new Response(body, { status, headers: h });
}

function ext(name) {
  const i = name.lastIndexOf('.');
  return i < 0 ? '' : name.slice(i).toLowerCase();
}

function safeKey(k) {
  return k.length > 0 && k.length < 300 && !k.includes('..') && !k.includes('\\') && !k.includes('\0') && !k.startsWith('/');
}

function equalSecrets(a, b) {
  if (a.length !== b.length) return false;
  let r = 0;
  for (let i = 0; i < a.length; i++) r |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return r === 0;
}

async function admin(req, env, url) {
  const auth = req.headers.get('Authorization') || '';
  if (!env.ADMIN_TOKEN || !equalSecrets(auth, 'Bearer ' + env.ADMIN_TOKEN)) return respond('Forbidden', 403, 'text/plain');
  const m = url.pathname.match(/^\/_admin\/(put|delete|list)(?:\/(.*))?$/);
  if (!m) return respond('Not found', 404, 'text/plain');
  const key = m[2] ? decodeURIComponent(m[2]) : '';
  if (m[1] === 'list') {
    const l = await env.BUCKET.list({ prefix: key });
    return respond(JSON.stringify(l.objects.map((o) => ({ key: o.key, size: o.size }))), 200, 'application/json');
  }
  if (!safeKey(key)) return respond('Bad key', 400, 'text/plain');
  if (m[1] === 'delete') { await env.BUCKET.delete(key); return respond('deleted', 200, 'text/plain'); }
  if (req.method !== 'PUT') return respond('Use PUT', 405, 'text/plain');
  const obj = await env.BUCKET.put(key, req.body);
  return respond(JSON.stringify({ key, size: obj.size }), 200, 'application/json');
}

function parseRange(h, size) {
  const m = /^bytes=(\d*)-(\d*)$/.exec(h || '');
  if (!m || (m[1] === '' && m[2] === '')) return null;
  let start, end;
  if (m[1] === '') { const n = parseInt(m[2], 10); start = Math.max(0, size - n); end = size - 1; }
  else { start = parseInt(m[1], 10); end = m[2] === '' ? size - 1 : Math.min(parseInt(m[2], 10), size - 1); }
  if (start > end || start >= size) return 'bad';
  return { start, end };
}

async function serveApk(req, env, url) {
  const name = decodeURIComponent(url.pathname.slice(5)).split('/').pop();
  if (!name.endsWith('.apk')) return respond('Not found', 404, 'text/plain');
  const key = 'apk/' + name;
  const head = await env.BUCKET.head(key);
  if (!head) return respond('Not found', 404, 'text/plain');
  const headers = {
    'Content-Disposition': `attachment; filename="${name}"`,
    'Accept-Ranges': 'bytes',
    ETag: head.httpEtag,
    'Cache-Control': 'public, max-age=3600',
  };
  const range = parseRange(req.headers.get('Range'), head.size);
  if (range === 'bad') return respond('Range not satisfiable', 416, 'text/plain', { 'Content-Range': `bytes */${head.size}` });
  if (req.method === 'HEAD') {
    return respond(null, 200, 'application/vnd.android.package-archive', { ...headers, 'Content-Length': String(head.size) });
  }
  if (range) {
    const obj = await env.BUCKET.get(key, { range: { offset: range.start, length: range.end - range.start + 1 } });
    return respond(obj.body, 206, 'application/vnd.android.package-archive', {
      ...headers,
      'Content-Range': `bytes ${range.start}-${range.end}/${head.size}`,
      'Content-Length': String(range.end - range.start + 1),
    });
  }
  const obj = await env.BUCKET.get(key);
  return respond(obj.body, 200, 'application/vnd.android.package-archive', { ...headers, 'Content-Length': String(head.size) });
}

async function latestJson(env, url) {
  const o = await env.BUCKET.get('releases.json');
  if (!o) return respond('No release', 404, 'text/plain');
  const r = JSON.parse(await o.text());
  const apks = {};
  for (const [abi, file] of Object.entries(r.apks || {})) apks[abi] = `${url.origin}/apk/${file}`;
  return respond(JSON.stringify({ ...r, apks }), 200, 'application/json', { 'Cache-Control': 'no-cache' });
}

async function notFound(req, env) {
  if ((req.headers.get('Accept') || '').includes('text/html')) {
    const o = await env.BUCKET.get('site/404.html');
    if (o) return respond(o.body, 404, MIME['.html'], { 'Cache-Control': 'no-cache' });
  }
  return respond('Not found', 404, 'text/plain');
}

async function serveStatic(req, env, rel) {
  let decoded;
  try { decoded = decodeURIComponent(rel); } catch { return null; }
  if (!safeKey(decoded)) return null;
  const type = MIME[ext(decoded)];
  if (!type) return null;
  const obj = await env.BUCKET.get('site/' + decoded);
  if (!obj) return null;
  const isHtml = ext(decoded) === '.html';
  let cache = isHtml ? 'no-cache' : 'public, max-age=31536000, immutable';
  if (!isHtml && /^(robots\.txt|sitemap\.xml|favicon\.svg|og\.svg|og\.png|apple-touch-icon\.png|icon-\d+\.png|img\/.+)$/.test(decoded)) cache = 'public, max-age=86400';
  const etag = obj.httpEtag;
  if (req.headers.get('If-None-Match') === etag) return respond(null, 304, null, { ETag: etag, 'Cache-Control': cache });
  return respond(req.method === 'HEAD' ? null : obj.body, 200, type, { ETag: etag, 'Cache-Control': cache });
}

export default {
  async fetch(req, env) {
    const url = new URL(req.url);
    const host = url.hostname.toLowerCase();
    if (url.pathname === '/healthz') return respond('ok', 200, 'text/plain');
    if (host === 'www.' + CANONICAL_HOST || (url.protocol === 'http:' && host === CANONICAL_HOST)) {
      return respond('Moved', 301, 'text/plain', { Location: `https://${CANONICAL_HOST}${url.pathname}${url.search}` });
    }
    if (url.pathname.startsWith('/_admin/')) return admin(req, env, url);
    if (url.pathname === '/latest.json') return latestJson(env, url);
    if (url.pathname.startsWith('/apk/')) return serveApk(req, env, url);
    if (req.method !== 'GET' && req.method !== 'HEAD') return respond('Method not allowed', 405, 'text/plain', { Allow: 'GET, HEAD' });
    let rel = url.pathname;
    if (rel === '/') rel = '/index.html';
    else if (rel === '/privacy' || rel === '/privacy/') rel = '/privacy.html';
    const res = await serveStatic(req, env, rel.slice(1));
    return res || notFound(req, env);
  },
};
