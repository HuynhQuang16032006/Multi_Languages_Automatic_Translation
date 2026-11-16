import test from 'node:test';
import assert from 'node:assert/strict';
import { requestPosition, freshPosition, locationMessage } from './location.js';

test('explains each GPS failure and how to recover', () => {
  assert.match(locationMessage('denied'), /cài đặt trình duyệt/);
  assert.match(locationMessage('timeout'), /quá lâu/);
  assert.match(locationMessage('unavailable'), /chưa lấy được vị trí/i);
  assert.match(locationMessage('insecure'), /HTTPS/);
  assert.match(locationMessage('unsupported'), /không hỗ trợ/);
  assert.match(locationMessage('stale'), /đã cũ/);
});

test('uses a recent GPS fix but rejects stale or invalid fixes', () => {
  const fix = { latitude: 10, longitude: 106, accuracy: 30, timestamp: 1000000 };
  assert.equal(freshPosition(fix, 1000000 + 4 * 60000), true);
  assert.equal(freshPosition(fix, 1000000 + 6 * 60000), false);
  assert.equal(freshPosition({ ...fix, latitude: 91 }, 1000000), false);
});

test('requests GPS only when called and reports denied, timeout, unavailable, unsupported and insecure states', async () => {
  let calls = 0;
  const geo = { getCurrentPosition(_success, error) { calls++; error({ code: 1 }); } };
  assert.equal(calls, 0);
  await assert.rejects(requestPosition(geo, true), error => error.code === 'denied');
  assert.equal(calls, 1);
  for (const [code, expected] of [[2, 'unavailable'], [3, 'timeout']]) {
    await assert.rejects(requestPosition({ getCurrentPosition(_, fail) { fail({ code }); } }, true), error => error.code === expected);
  }
  await assert.rejects(requestPosition(null, true), error => error.code === 'unsupported');
  await assert.rejects(requestPosition(geo, false), error => error.code === 'insecure');
  assert.equal(calls, 1);
});

test('returns geolocation timestamp and accuracy for user-visible quality warnings', async () => {
  const position = await requestPosition({ getCurrentPosition(success) {
    success({ coords: { latitude: 10, longitude: 106, accuracy: 200 }, timestamp: 1000 });
  } }, true);
  assert.deepEqual(position, { latitude: 10, longitude: 106, accuracy: 200, timestamp: 1000 });
});
