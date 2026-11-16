import { nearbyPlaces } from './nearby.js';

export function updateGeofence(state, places, position, now = Date.now()) {
  if (!position || !Number.isFinite(position.accuracy) || position.accuracy < 0 || position.accuracy > 30 || !Number.isFinite(position.timestamp) || now - position.timestamp > 15000 || now < position.timestamp) return { state, entered: null };
  const inside = new Set(state.inside || []);
  const last = { ...(state.last || {}) };
  const candidates = [];
  for (const { place, distance } of nearbyPlaces(places, position)) {
    const radius = Number(place.radiusInMeters) > 0 ? Number(place.radiusInMeters) : 30;
    if (distance > radius + 15) inside.delete(place.id);
    else if (distance <= radius && !inside.has(place.id)) {
      inside.add(place.id);
      if (last[place.id] == null || now - last[place.id] >= 60000) candidates.push({ place, distance });
    }
  }
  candidates.sort((a, b) => (Number(b.place.priority) || 0) - (Number(a.place.priority) || 0) || a.distance - b.distance || a.place.id - b.place.id);
  const entered = candidates[0]?.place || null;
  if (entered) last[entered.id] = now;
  return { state: { inside: [...inside], last }, entered };
}
