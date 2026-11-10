import test from 'node:test';
import assert from 'node:assert/strict';
import { getPlaces } from './api.js';

test('reads actual POIs without transforming their IDs or coordinates', async () => {
  const places = [{ id: 14, name: 'Địa điểm API', latitude: 10, longitude: 106 }];
  const result = await getPlaces(undefined, async (url) => {
    assert.equal(url, '/api/locations/pois');
    return new Response(JSON.stringify(places));
  });
  assert.deepEqual(result, places);
});

test('does not substitute sample places when the server fails', async () => {
  await assert.rejects(getPlaces(undefined, async () => new Response('', { status: 503 })), /503/);
});

test('rejects unexpected API shapes instead of rendering false empty results', async () => {
  await assert.rejects(getPlaces(undefined, async () => new Response('{}')), /dữ liệu/);
});
