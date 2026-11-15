import test from 'node:test';
import assert from 'node:assert/strict';
import { createListeningSession, listeningTrigger, sendListeningHeartbeat } from './listening.js';

test('QR route state retains its source while ordinary navigation stays manual', () => {
  const scannerState = { triggerType: 'QR_CODE' };
  const detailState = { triggerType: listeningTrigger(scannerState) };
  assert.equal(listeningTrigger(detailState), 'QR_CODE');
  assert.equal(listeningTrigger(undefined), 'MANUAL');
  assert.equal(listeningTrigger({ triggerType: 'GEOFENCE' }), 'MANUAL');
});

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
test('QR playback sends QR_CODE in JOIN, PING and LEAVE payloads', async () => {
  const payloads = [];
  const requests = [];
  const request = async (_url, options) => { payloads.push(JSON.parse(options.body)); return new Response('{}'); };
  const session = createListeningSession((action, duration) => requests.push(
    sendListeningHeartbeat('device', { id: 7 }, 'vi', action, duration, request, 'QR_CODE')
  ));
  session.start(); session.tick(1); session.tick(2); session.tick(3); session.end();
  await Promise.all(requests);
  assert.deepEqual(payloads.map(({ action, triggerType }) => [action, triggerType]), [
    ['JOIN', 'QR_CODE'], ['PING', 'QR_CODE'], ['LEAVE', 'QR_CODE'],
  ]);
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

test('native seeks in either direction do not count as listening', () => {
  const calls = [];
  const session = createListeningSession((action, seconds) => calls.push([action, seconds]));
  session.start(); session.tick(1); session.seek(2); session.tick(2, true);
  session.seek(0); session.tick(0, true); session.tick(1); session.end();
  assert.deepEqual(calls.at(-1), ['LEAVE', 2]);
});

test('ending on error or page hide sends only one leave', () => {
  const calls = [];
  const session = createListeningSession((action, seconds) => calls.push([action, seconds]));
  session.start(); session.tick(1); session.end(); session.end(); session.ping();
  assert.deepEqual(calls, [['JOIN', 0], ['LEAVE', 1]]);
});
