import test from 'node:test';
import assert from 'node:assert/strict';
import { parseQr } from './qr.js';

test('accepts plain QR codes and supported same-origin player links', () => {
  assert.deepEqual(parseQr('QR-KHANH-HOI-01', 'https://tour.example'), { code: 'QR-KHANH-HOI-01' });
  assert.deepEqual(parseQr('https://tour.example/player.html?qr=QR-1', 'https://tour.example'), { code: 'QR-1' });
  assert.deepEqual(parseQr('https://tour.example/index.html?venue=4', 'https://tour.example'), { venue: 4 });
  assert.deepEqual(parseQr('https://tour.example/next/qr?code=QR-1', 'https://tour.example'), { code: 'QR-1' });
});

test('rejects foreign and malformed links instead of navigating to them', () => {
  for (const value of ['https://evil.example/player.html?qr=QR-1', 'javascript:alert(1)', 'https://tour.example/unknown?venue=4', '../player.html?qr=x', '']) {
    assert.equal(parseQr(value, 'https://tour.example'), null);
  }
});

test('invalidates pending camera operations when stopped or restarted', async () => {
  const { cameraSession } = await import('./qr.js');
  const session = cameraSession();
  const first = session.start();
  assert.equal(session.current(first), true);
  session.stop();
  assert.equal(session.current(first), false);
  const second = session.start();
  assert.equal(session.current(first), false);
  assert.equal(session.current(second), true);
});

test('shares concurrent lookups but retries after completion, including 404', async () => {
  const { lookupQr } = await import('./qr.js');
  let calls = 0;
  let finish;
  const fetcher = url => { calls++; assert.equal(url, '/api/locations/by-qr/QR-1'); return new Promise(resolve => { finish = resolve; }); };
  const first = lookupQr({ code: 'QR-1' }, fetcher);
  const second = lookupQr({ code: 'QR-1' }, fetcher);
  assert.equal(calls, 1);
  finish({ ok: false, status: 404 });
  assert.equal((await first).status, 404);
  assert.equal((await second).status, 404);
  const retry = lookupQr({ code: 'QR-1' }, fetcher);
  assert.equal(calls, 2);
  finish({ ok: true, json: async () => ({ id: 4 }) });
  assert.deepEqual((await retry).place, { id: 4 });
});
