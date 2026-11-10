import test from 'node:test';
import assert from 'node:assert/strict';
import { parseAdminDate } from './admin-date.js';

test('converts Vietnamese dates to API dates and rejects invalid calendar dates', () => {
  assert.equal(parseAdminDate('31/12/2025'), '2025-12-31');
  assert.equal(parseAdminDate('29/02/2024'), '2024-02-29');
  assert.throws(() => parseAdminDate('29/02/2025'), /ngày/i);
  assert.throws(() => parseAdminDate('12/31/2025'), /ngày/i);
});
