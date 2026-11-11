export async function getPlaceDetail(id, signal, request = fetch, languageCode = 'vi') {
  if (!/^[1-9]\d*$/.test(String(id)) || !Number.isSafeInteger(Number(id))) throw new Error('ID địa điểm không hợp lệ.');
  const read = async (url, missing = false) => {
    const response = await request(url, { signal, headers: { Accept: 'application/json' } });
    if (!response.ok) {
      if (missing && (response.status === 400 || response.status === 404)) throw Object.assign(new Error('Không tìm thấy địa điểm này.'), { code: 'missing' });
      if (response.status === 404) return null;
      throw new Error(`Không tải được thông tin địa điểm (HTTP ${response.status}).`);
    }
    return response.json();
  };
  const place = await read(`/api/locations/${id}`, true);
  if (place?.id !== Number(id) || typeof place.name !== 'string') throw new Error('Dữ liệu địa điểm không hợp lệ.');
  const [children, narration] = await Promise.all([
    read(`/api/locations/${id}/sub-locations`),
    read(`/api/narrations?locationId=${id}&languageCode=${encodeURIComponent(languageCode)}`),
  ]);
  if (!Array.isArray(children) || (narration && (narration.locationId !== place.id || typeof narration.textContent !== 'string'))) throw new Error('Dữ liệu địa điểm không hợp lệ.');
  return { place, children, narration };
}

export async function getLanguages(signal, request = fetch) {
  const response = await request('/api/languages', { signal, headers: { Accept: 'application/json' } });
  if (!response.ok) throw new Error(`Không tải được ngôn ngữ (HTTP ${response.status}).`);
  const languages = await response.json();
  if (!Array.isArray(languages) || languages.some(language => typeof language?.code !== 'string' || typeof language.name !== 'string')) throw new Error('Dữ liệu ngôn ngữ không hợp lệ.');
  return languages;
}

export function preferredLanguage(languages, urlLanguage) {
  let saved;
  try { saved = localStorage.getItem('audiotour_preferred_lang'); } catch { /* Storage may be unavailable. */ }
  return [urlLanguage, saved, 'vi', languages[0]?.code].find(code => languages.some(language => language.code === code)) || 'vi';
}

export function savePreferredLanguage(code) {
  try { localStorage.setItem('audiotour_preferred_lang', code); } catch { /* Storage may be unavailable. */ }
}
