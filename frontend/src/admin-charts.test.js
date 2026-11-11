import test from 'node:test';
import assert from 'node:assert/strict';
import { chartQuery, chartRows } from './admin-charts.js';

test('chart dates include the complete last day and reject reversed ranges', () => {
  assert.equal(chartQuery('2026-01-01', '2026-01-03'), '?from=2026-01-01T00%3A00%3A00&to=2026-01-03T23%3A59%3A59.999999');
  assert.throws(() => chartQuery('2026-01-04', '2026-01-03'), /ngày/i);
});

test('chart rows use server totals rather than recent logs', () => {
  assert.deepEqual(chartRows({ listensByDay: [{ date: '2026-01-01', listenCount: 4 }], listensByLanguage: { vi: 3, en: 1 } }), {
    days: [['01/01/2026', 4]], languages: [['en', 1], ['vi', 3]],
  });
  assert.throws(() => chartRows({ recentLogs: [] }), /dữ liệu/i);
});

test('dates are sorted chronologically before formatting across months and years', () => {
  const data = { listensByDay: [{ date: '2026-01-02', listenCount: 2 }, { date: '2025-12-31', listenCount: 1 }], listensByLanguage: {} };
  assert.deepEqual(chartRows(data).days, [['31/12/2025', 1], ['02/01/2026', 2]]);
});

test('empty and malformed chart data are handled explicitly', () => {
  assert.deepEqual(chartRows({ listensByDay: [], listensByLanguage: {} }), { days: [], languages: [] });
  for (const data of [
    { listensByDay: [null], listensByLanguage: {} },
    { listensByDay: [{ date: '2026-01-01', listenCount: -1 }], listensByLanguage: {} },
    { listensByDay: [], listensByLanguage: [] },
    { listensByDay: [], listensByLanguage: { vi: '2' } },
  ]) assert.throws(() => chartRows(data), /dữ liệu/i);
});

test('same-day ranges are accepted', () => {
  assert.equal(chartQuery('2026-01-03', '2026-01-03'), '?from=2026-01-03T00%3A00%3A00&to=2026-01-03T23%3A59%3A59.999999');
});
