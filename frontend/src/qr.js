export function parseQr(input, origin) {
  const value = input.trim();
  if (/^[A-Za-z0-9_-]{1,50}$/.test(value)) return { code: value };
  try {
    const url = new URL(value);
    if (url.origin !== origin || !['http:', 'https:'].includes(url.protocol)) return null;
    if (url.pathname === '/player.html' && /^[A-Za-z0-9_-]{1,50}$/.test(url.searchParams.get('qr') || '')) return { code: url.searchParams.get('qr') };
    const venue = url.searchParams.get('venue');
    if (url.pathname === '/index.html' && /^[1-9]\d*$/.test(venue || '') && Number.isSafeInteger(Number(venue))) return { venue: Number(venue) };
  } catch { return null; }
  return null;
}

export function cameraSession() {
  let generation = 0;
  return { start: () => ++generation, stop: () => { generation++; }, current: id => id === generation };
}
