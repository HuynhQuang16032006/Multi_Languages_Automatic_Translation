export async function getPlaces(signal, request = fetch) {
  const response = await request('/api/locations/pois', {
    signal,
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) throw new Error(`Không tải được địa điểm (HTTP ${response.status}).`);
  const data = await response.json();
  if (!Array.isArray(data) || data.some(place => !place || typeof place.id !== 'number' || typeof place.name !== 'string')) {
    throw new Error('Máy chủ trả về dữ liệu địa điểm không hợp lệ.');
  }
  return data;
}
