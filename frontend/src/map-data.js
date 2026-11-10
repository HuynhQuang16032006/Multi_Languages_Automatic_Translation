export const mapPlaces = places => places.filter(place =>
  typeof place.latitude === 'number' && Number.isFinite(place.latitude) && Math.abs(place.latitude) <= 90 &&
  typeof place.longitude === 'number' && Number.isFinite(place.longitude) && Math.abs(place.longitude) <= 180);

export const selectedPlace = (places, id) => places.find(place => String(place.id) === String(id)) || null;
