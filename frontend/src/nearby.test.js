import test from 'node:test';
import assert from 'node:assert/strict';
import { nearbyPlaces } from './nearby.js';

test('sorts by Haversine distance and returns at most five actual POIs', () => {
  const places = [6, 2, 5, 1, 4, 3].map(id => ({ id, name: `${id}`, latitude: id / 100, longitude: 0 }));
  const result = nearbyPlaces(places, { latitude: 0, longitude: 0 }, 5);
  assert.deepEqual(result.map(({ place }) => place.id), [1, 2, 3, 4, 5]);
  assert.ok(result[0].distance >= 1100 && result[0].distance <= 1120);
});

test('keeps fewer than three valid POIs without padding, excludes invalid coordinates', () => {
  const places = [
    { id: 1, latitude: 0, longitude: 0 },
    { id: 2, latitude: -1, longitude: 0 },
    { id: 3, latitude: null, longitude: 1 },
    { id: 4, latitude: 91, longitude: 0 },
    { id: 5, latitude: 0, longitude: Infinity },
    { id: 6, latitude: '0', longitude: 0 },
  ];
  assert.deepEqual(nearbyPlaces(places, { latitude: 0, longitude: 0 }).map(({ place }) => place.id), [1, 2]);
  assert.deepEqual(nearbyPlaces(places, { latitude: NaN, longitude: 0 }), []);
});

test('computes finite distance for antipodal coordinates', () => {
  const [result] = nearbyPlaces([{ id: 1, latitude: -10, longitude: -74 }], { latitude: 10, longitude: 106 });
  assert.ok(Number.isFinite(result.distance));
});
