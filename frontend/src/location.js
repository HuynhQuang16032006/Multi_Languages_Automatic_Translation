const locationMessages = { denied: 'Quyền vị trí đã bị từ chối. Bật lại trong cài đặt trình duyệt để dùng Gần tôi; bạn vẫn có thể khám phá bình thường.', timeout: 'Lấy vị trí quá lâu. Hãy thử lại.', unavailable: 'Chưa lấy được vị trí. Hãy thử lại.', unsupported: 'Trình duyệt không hỗ trợ định vị.', insecure: 'Cần kết nối HTTPS hoặc localhost để dùng định vị.', stale: 'Vị trí đã cũ. Hãy cập nhật để xem địa điểm gần bạn.', loading: 'Đang lấy vị trí…' };

export function locationMessage(state) {
  return locationMessages[state] || 'Không lấy được vị trí. Hãy thử lại.';
}

export function freshPosition(position, now = Date.now()) {
  return position && Number.isFinite(position.timestamp) && now >= position.timestamp && now - position.timestamp < 5 * 60000
    && typeof position.latitude === 'number' && Number.isFinite(position.latitude) && Math.abs(position.latitude) <= 90
    && typeof position.longitude === 'number' && Number.isFinite(position.longitude) && Math.abs(position.longitude) <= 180;
}

export function requestPosition(geo, secure) {
  if (!secure) return Promise.reject({ code: 'insecure' });
  if (!geo?.getCurrentPosition) return Promise.reject({ code: 'unsupported' });
  return new Promise((resolve, reject) => geo.getCurrentPosition(
    ({ coords, timestamp }) => resolve({ latitude: coords.latitude, longitude: coords.longitude, accuracy: coords.accuracy, timestamp }),
    error => reject({ code: ({ 1: 'denied', 2: 'unavailable', 3: 'timeout' })[error.code] || 'unavailable' }),
    { enableHighAccuracy: true, timeout: 12000, maximumAge: 60000 },
  ));
}
