import test from 'node:test';
import assert from 'node:assert/strict';
import { matchesPlace } from './search.js';

test('matches Vietnamese names with or without diacritics', () => {
  assert.equal(matchesPlace('Dinh Độc Lập', 'dinh doc lap'), true);
  assert.equal(matchesPlace('Trạm Khánh Hội', 'KHÁNH hội'), true);
  assert.equal(matchesPlace('Trạm Khánh Hội', 'xa la'), false);
});
