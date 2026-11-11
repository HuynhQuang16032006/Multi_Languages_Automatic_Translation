import test from 'node:test';
import assert from 'node:assert/strict';
import { parseAdminDate } from './admin-date.js';

test('converts Vietnamese dates to API dates and rejects invalid calendar dates', () => {
  assert.equal(parseAdminDate('31/12/2025'), '2025-12-31');
  assert.equal(parseAdminDate('29/02/2024'), '2024-02-29');
  assert.throws(() => parseAdminDate('29/02/2025'), /ngày/i);
  assert.throws(() => parseAdminDate('12/31/2025'), /ngày/i);
});

test('rejects impossible and incorrectly formatted dates', () => {
  for (const date of ['00/01/2026', '31/04/2026', '01/13/2026', '01/01/0000', '1/01/2026', '2026-01-01']) {
    assert.throws(() => parseAdminDate(date), /ngày/i);
  }
});
