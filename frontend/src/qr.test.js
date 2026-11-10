import test from 'node:test';
import assert from 'node:assert/strict';
import { parseQr } from './qr.js';

test('accepts plain QR codes and supported same-origin player links', () => {
  assert.deepEqual(parseQr('QR-KHANH-HOI-01', 'https://tour.example'), { code: 'QR-KHANH-HOI-01' });
  assert.deepEqual(parseQr('https://tour.example/player.html?qr=QR-1', 'https://tour.example'), { code: 'QR-1' });
  assert.deepEqual(parseQr('https://tour.example/index.html?venue=4', 'https://tour.example'), { venue: 4 });
});

test('rejects foreign and malformed links instead of navigating to them', () => {
  for (const value of ['https://evil.example/player.html?qr=QR-1', 'javascript:alert(1)', 'https://tour.example/unknown?venue=4', '../player.html?qr=x', '']) {
    assert.equal(parseQr(value, 'https://tour.example'), null);
  }
});
