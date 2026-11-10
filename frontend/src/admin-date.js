export function parseAdminDate(value) {
  const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
  if (!match) throw new Error('Nhập ngày theo định dạng dd/mm/yyyy.');
  const [, day, month, year] = match;
  const date = new Date(0);
  date.setUTCFullYear(Number(year), Number(month) - 1, Number(day));
  if (year === '0000' || date.getUTCFullYear() !== Number(year) || date.getUTCMonth() !== Number(month) - 1 || date.getUTCDate() !== Number(day)) throw new Error('Ngày không hợp lệ.');
  return `${year}-${month}-${day}`;
}
