import test from 'node:test';
import assert from 'node:assert/strict';
import { createListeningSession, sendListeningHeartbeat } from './listening.js';

test('records one session only after playback starts; navigation and GPS do not start another', () => {
  const calls = [];
  const session = createListeningSession((action, duration) => calls.push([action, duration]));
  session.tick(5); session.start(); session.start(); for (let i = 1; i <= 6; i++) session.tick(i); session.tick(6); session.end(); session.end();
  assert.deepEqual(calls, [['JOIN', 0], ['PING', 3], ['PING', 6], ['LEAVE', 6]]);
});

test('language change ends old session once before starting new one', () => {
  const calls = [];
  const session = createListeningSession((action, duration) => calls.push([action, duration]));
  session.start(); for (let i = 1; i <= 9; i++) session.tick(i); session.end(); session.start(); session.end();
  assert.deepEqual(calls, [['JOIN', 0], ['PING', 3], ['PING', 6], ['PING', 9], ['LEAVE', 9], ['JOIN', 0], ['LEAVE', 0]]);
});

test('sends same heartbeat contract without geographic coordinates', async () => {
  let payload;
  await sendListeningHeartbeat('device', { id: 7 }, 'en', 'PING', 12, async (url, options) => {
    assert.equal(url, '/api/analytics/heartbeat');
    payload = JSON.parse(options.body);
    return new Response('{}');
  });
  assert.deepEqual(payload, { deviceId: 'device', locationId: 7, languageCode: 'en', action: 'PING', listenDurationSeconds: 12, triggerType: 'MANUAL' });
});

test('reports unsuccessful heartbeat responses', async () => {
  await assert.rejects(sendListeningHeartbeat('device', { id: 7 }, 'vi', 'JOIN', 0,
    async () => new Response('', { status: 503 })), /503/);
});

test('session can keep one listening interval through navigation without duplicate JOIN', () => {
  const actions = [];
  const session = createListeningSession(action => actions.push(action));
  session.start(); session.tick(1); session.tick(2); session.tick(3); session.start(); session.tick(4); session.end();
  assert.deepEqual(actions, ['JOIN', 'PING', 'LEAVE']);
});

test('playback timeupdate is capped so seeking cannot inflate listened seconds', () => {
  const calls = [];
  const session = createListeningSession((action, seconds) => calls.push([action, seconds]));
  session.start(); session.tick(1); session.tick(120, true); session.end();
  assert.deepEqual(calls.at(-1), ['LEAVE', 1]);
});

test('a short seek does not count as listening time', () => {
  const calls = [];
  const session = createListeningSession((action, seconds) => calls.push([action, seconds]));
  session.start(); session.tick(1); session.seek(2); session.tick(3); session.end();
  assert.deepEqual(calls.at(-1), ['LEAVE', 2]);
});
