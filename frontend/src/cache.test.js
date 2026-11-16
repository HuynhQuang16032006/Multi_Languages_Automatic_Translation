import test from 'node:test';
import assert from 'node:assert/strict';
import { readCachedPlaces, saveCachedPlaces } from './cache.js';

test('restores valid POIs and their save time after an API failure', () => {
  let stored;
  const storage = { setItem: (_, value) => { stored = value; }, getItem: () => stored };
  saveCachedPlaces(storage, [{ id: 1, name: 'Dinh' }], 123);
  assert.deepEqual(readCachedPlaces(storage), { places: [{ id: 1, name: 'Dinh' }], timestamp: 123 });
});

test('ignores corrupt or invalid cache without creating fake places', () => {
  assert.equal(readCachedPlaces({ getItem: () => '{' }), null);
  assert.equal(readCachedPlaces({ getItem: () => '{"timestamp":1,"places":[{}]}' }), null);
});
