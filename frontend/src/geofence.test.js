import test from 'node:test';
import assert from 'node:assert/strict';
import { updateGeofence } from './geofence.js';

const pois = [
  { id: 1, latitude: 0, longitude: 0, radiusInMeters: 30, priority: 1 },
  { id: 2, latitude: 0, longitude: 0.00002, radiusInMeters: 30, priority: 4 },
];
const fix = (longitude, accuracy = 5, timestamp = 100000) => ({ latitude: 0, longitude, accuracy, timestamp });

test('prioritizes overlapping zones deterministically and never repeats on GPS fixes', () => {
  let state = {};
  let result = updateGeofence(state, pois, fix(0), 100000);
  assert.equal(result.entered.id, 2); state = result.state;
  result = updateGeofence(state, pois, fix(0.00001), 101000);
  assert.equal(result.entered, null);
});

test('hysteresis and cooldown require exit then delayed re-entry', () => {
  let result = updateGeofence({}, pois, fix(0), 100000);
  result = updateGeofence(result.state, pois, fix(0.0003, 5, 101000), 101000);
  assert.equal(result.entered, null);
  result = updateGeofence(result.state, pois, fix(0, 5, 102000), 102000);
  assert.equal(result.entered, null);
  result = updateGeofence(result.state, pois, fix(0.001, 5, 103000), 103000);
  result = updateGeofence(result.state, pois, fix(0, 5, 170000), 170000);
  assert.equal(result.entered.id, 2);
});

test('stale or inaccurate fixes cannot trigger geofence', () => {
  assert.equal(updateGeofence({}, pois, fix(0, 100), 100000).entered, null);
  assert.equal(updateGeofence({}, pois, fix(0, 5, 1000), 100000).entered, null);
});
