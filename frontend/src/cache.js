const key = 'audiotour_next_pois';
export function saveCachedPlaces(storage, places, timestamp = Date.now()) {
  try { storage.setItem(key, JSON.stringify({ places, timestamp })); } catch { return; }
}
export function readCachedPlaces(storage) {
  try {
    const data = JSON.parse(storage.getItem(key));
    if (!Number.isFinite(data?.timestamp) || !Array.isArray(data.places) || data.places.some(place => !place || !Number.isSafeInteger(place.id) || typeof place.name !== 'string')) return null;
    return data;
  } catch { return null; }
}
