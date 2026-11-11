import test from 'node:test';
import assert from 'node:assert/strict';
import { availableLanguages, translationMessage } from './admin-settings.js';

test('language onboarding excludes existing languages and reports missing audio', () => {
  const supported = [{ code: 'en', name: 'English' }, { code: 'ja', name: 'Japanese' }];
  assert.deepEqual(availableLanguages(supported, [{ code: 'en' }]), [supported[1]]);
  assert.match(translationMessage({ language: supported[1], translations: 2, audioGenerated: 1 }), /1\/2 file audio/);
  assert.doesNotMatch(translationMessage({ language: supported[1], translations: 2, audioGenerated: 2 }), /file audio/);
});
