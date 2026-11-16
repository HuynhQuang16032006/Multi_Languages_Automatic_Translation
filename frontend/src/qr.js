export function parseQr(input, origin) {
  const value = input.trim();
  if (/^[A-Za-z0-9_-]{1,50}$/.test(value)) return { code: value };
  try {
    const url = new URL(value);
    if (url.origin !== origin || !['http:', 'https:'].includes(url.protocol)) return null;
    if (url.pathname === '/next/qr' && /^[A-Za-z0-9_-]{1,50}$/.test(url.searchParams.get('code') || '')) return { code: url.searchParams.get('code') };
    if (url.pathname === '/player.html' && /^[A-Za-z0-9_-]{1,50}$/.test(url.searchParams.get('qr') || '')) return { code: url.searchParams.get('qr') };
    const venue = url.searchParams.get('venue');
    if (url.pathname === '/index.html' && /^[1-9]\d*$/.test(venue || '') && Number.isSafeInteger(Number(venue))) return { venue: Number(venue) };
  } catch { return null; }
  return null;
}

const pending = new Map();

export function lookupQr(parsed, fetcher = fetch) {
  const url = parsed.code ? `/api/locations/by-qr/${encodeURIComponent(parsed.code)}` : `/api/locations/${parsed.venue}`;
  if (!pending.has(url)) {
    const request = fetcher(url).then(async response => ({ status: response.status, ok: response.ok, place: response.ok ? await response.json() : null })).finally(() => pending.delete(url));
    pending.set(url, request);
  }
  return pending.get(url);
}

export function cameraSession() {
  let generation = 0;
  return { start: () => ++generation, stop: () => { generation++; }, current: id => id === generation };
}
