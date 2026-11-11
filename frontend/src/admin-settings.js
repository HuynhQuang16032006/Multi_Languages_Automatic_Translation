export function availableLanguages(supported, current) {
  const existing = new Set(current.map(language => language.code));
  return supported.filter(language => !existing.has(language.code));
}

export function translationMessage(result) {
  const note = result.audioGenerated < result.translations
    ? `, chỉ ${result.audioGenerated}/${result.translations} file audio; sinh lại audio cho phần thiếu`
    : '';
  return `Đã thêm ${result.language.name}: ${result.translations} bản dịch${note}.`;
}
