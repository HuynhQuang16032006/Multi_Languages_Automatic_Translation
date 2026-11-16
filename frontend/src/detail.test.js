import test from 'node:test';
import assert from 'node:assert/strict';
import { getPlaceDetail, getLanguages, preferredLanguage, savePreferredLanguage } from './detail.js';

test('reads real place, children and Vietnamese narration from their public endpoints', async () => {
  const urls = [];
  const result = await getPlaceDetail(4, undefined, async url => {
    urls.push(url);
    const body = url === '/api/locations/4' ? { id: 4, name: 'Dinh Độc Lập' }
      : url === '/api/locations/4/sub-locations' ? [{ id: 5, name: 'Phòng Khánh Tiết' }]
        : { locationId: 4, languageCode: 'vi', textContent: 'Thuyết minh thật.' };
    return new Response(JSON.stringify(body));
  });
  assert.deepEqual(urls, ['/api/locations/4', '/api/locations/4/sub-locations', '/api/narrations?locationId=4&languageCode=vi']);
  assert.equal(result.place.name, 'Dinh Độc Lập');
  assert.equal(result.children[0].id, 5);
  assert.equal(result.narration.textContent, 'Thuyết minh thật.');
});

test('rejects invalid ID without making a request and identifies missing place', async () => {
  await assert.rejects(getPlaceDetail('1abc', undefined, () => { throw Error('request should not run'); }), /ID địa điểm/);
  await assert.rejects(getPlaceDetail('0', undefined, () => { throw Error('request should not run'); }), /ID địa điểm/);
  await assert.rejects(getPlaceDetail('999', undefined, async () => new Response('', { status: 400 })), error => error.code === 'missing');
});

test('keeps place detail usable when narration is missing', async () => {
  const detail = await getPlaceDetail('1', undefined, async url => url.includes('narrations')
    ? new Response('', { status: 404 }) : new Response(JSON.stringify(url.includes('sub-locations') ? [] : { id: 1, name: 'Trạm dừng' })));
  assert.equal(detail.narration, null);
  assert.deepEqual(detail.children, []);
});

test('fetches narration in the selected language', async () => {
  const urls = [];
  const detail = await getPlaceDetail('4', undefined, async url => {
    urls.push(url);
    return new Response(JSON.stringify(url.includes('sub-locations') ? [] : url.includes('narrations')
      ? { locationId: 4, languageCode: 'en', textContent: 'English tour' } : { id: 4, name: 'Dinh Độc Lập' }));
  }, 'en');
  assert.ok(urls.includes('/api/narrations?locationId=4&languageCode=en'));
  assert.equal(detail.narration.textContent, 'English tour');
});

test('reads available language codes and labels without inventing language choices', async () => {
  const result = await getLanguages(undefined, async url => {
    assert.equal(url, '/api/languages');
    return new Response(JSON.stringify([{ id: 1, code: 'vi', name: 'Tiếng Việt' }, { id: 2, code: 'en', name: 'English' }]));
  });
  assert.deepEqual(result.map(({ code }) => code), ['vi', 'en']);
  await assert.rejects(getLanguages(undefined, async () => new Response('{}')), /ngôn ngữ không hợp lệ/);
});

test('resolves URL and saved language safely against supported languages', () => {
  const original = globalThis.localStorage;
  let saved = 'en';
  globalThis.localStorage = { getItem: key => { assert.equal(key, 'audiotour_preferred_lang'); return saved; }, setItem: (key, value) => { assert.equal(key, 'audiotour_preferred_lang'); saved = value; } };
  const languages = [{ code: 'vi' }, { code: 'en' }, { code: 'fr' }];
  try {
    assert.equal(preferredLanguage(languages, 'fr'), 'fr');
    assert.equal(preferredLanguage(languages, 'unknown'), 'en');
    savePreferredLanguage('fr');
    assert.equal(preferredLanguage(languages), 'fr');
    assert.equal(preferredLanguage([{ code: 'vi' }]), 'vi');
    assert.equal(preferredLanguage([{ code: 'en' }]), 'en');
    globalThis.localStorage = { getItem: () => { throw Error('blocked'); }, setItem: () => { throw Error('blocked'); } };
    assert.equal(preferredLanguage(languages), 'vi');
    assert.doesNotThrow(() => savePreferredLanguage('en'));
  } finally {
    if (original === undefined) delete globalThis.localStorage;
    else globalThis.localStorage = original;
  }
});
