import test from 'node:test';
import assert from 'node:assert/strict';
import { canShowImage } from './image.js';

test('shows supplied image URLs including Unsplash and falls back when missing', () => {
  assert.equal(canShowImage('https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=600'), true);
  assert.equal(canShowImage('https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600'), true);
  assert.equal(canShowImage('https://cdn.example.org/verified-location.jpg'), true);
  assert.equal(canShowImage(''), false);
  assert.equal(canShowImage(undefined), false);
});
