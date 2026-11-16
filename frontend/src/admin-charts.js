export function chartQuery(from, to) {
  if (!from || !to || from > to) throw new Error('Hãy chọn khoảng ngày hợp lệ.');
  return `?${new URLSearchParams({ from: `${from}T00:00:00`, to: `${to}T23:59:59.999999` })}`;
}

export function chartRows(data) {
  if (!Array.isArray(data?.listensByDay) || !data.listensByLanguage || typeof data.listensByLanguage !== 'object' || Array.isArray(data.listensByLanguage) || data.listensByDay.some(item => !item || typeof item.date !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(item.date) || !Number.isSafeInteger(item.listenCount) || item.listenCount < 0) || Object.values(data.listensByLanguage).some(count => !Number.isSafeInteger(count) || count < 0)) throw new Error('Dữ liệu biểu đồ không hợp lệ.');
  return { days: [...data.listensByDay].sort((a, b) => a.date.localeCompare(b.date)).map(item => [item.date.split('-').reverse().join('/'), item.listenCount]), languages: Object.entries(data.listensByLanguage).sort(([a], [b]) => a.localeCompare(b)) };
}
