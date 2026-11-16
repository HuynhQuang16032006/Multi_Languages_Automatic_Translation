function valid({ latitude, longitude } = {}) {
  return typeof latitude === 'number' && Number.isFinite(latitude) && Math.abs(latitude) <= 90
    && typeof longitude === 'number' && Number.isFinite(longitude) && Math.abs(longitude) <= 180;
}

export function nearbyPlaces(places, position, limit = Infinity) {
  if (!valid(position)) return [];
  const radians = Math.PI / 180;
  return places.filter(valid).map(place => {
    const deltaLat = (place.latitude - position.latitude) * radians;
    const deltaLon = (place.longitude - position.longitude) * radians;
    const a = Math.sin(deltaLat / 2) ** 2 + Math.cos(position.latitude * radians) * Math.cos(place.latitude * radians) * Math.sin(deltaLon / 2) ** 2;
    return { place, distance: 6371000 * 2 * Math.atan2(Math.sqrt(Math.min(1, a)), Math.sqrt(Math.max(0, 1 - a))) };
  }).sort((a, b) => a.distance - b.distance || a.place.id - b.place.id).slice(0, limit);
}
