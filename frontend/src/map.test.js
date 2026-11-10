import test from 'node:test';
import assert from 'node:assert/strict';
import { mapPlaces, selectedPlace } from './map-data.js';

test('map skips invalid coordinates without dropping POIs from Explore', () => {
  const items = [{ id: 1, latitude: 10, longitude: 106 }, { id: 2, latitude: null, longitude: 106 }, { id: 3, latitude: 91, longitude: 0 }];
  assert.deepEqual(mapPlaces(items).map(item => item.id), [1]);
  assert.equal(items.length, 3);
});

test('marker and list select the same actual POI by ID, invalid selection is ignored', () => {
  const places = [{ id: 1, name: 'First' }, { id: 2, name: 'Second' }];
  assert.equal(selectedPlace(places, '2'), places[1]);
  assert.equal(selectedPlace(places, '9'), null);
});
