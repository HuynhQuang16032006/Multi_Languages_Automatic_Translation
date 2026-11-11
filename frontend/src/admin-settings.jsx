import React, { useEffect, useState } from 'react';
import { availableLanguages, translationMessage } from './admin-settings.js';

export function AdminSettings({ request, onPasswordChanged }) {
  const [languages, setLanguages] = useState(null);
  const [selected, setSelected] = useState('');
  const [languageError, setLanguageError] = useState('');
  const [languageMessage, setLanguageMessage] = useState('');
  const [languageBusy, setLanguageBusy] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [passwordBusy, setPasswordBusy] = useState(false);

  async function loadLanguages() {
    setLanguageError('');
    try {
      const [supported, current] = await Promise.all([request('/api/languages/supported'), request('/api/languages')]);
      const available = availableLanguages(supported, current);
      setLanguages({ current, available });
      setSelected(available[0]?.code || '');
    } catch (error) {
      setLanguages(null);
      setLanguageError(error.message);
    }
  }

  useEffect(() => { loadLanguages(); }, [request]);

  async function changePassword(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    setPasswordError('');
    if (data.get('newPassword') !== data.get('confirmPassword')) {
      setPasswordError('Mật khẩu xác nhận không khớp.');
      return;
    }
    setPasswordBusy(true);
    try {
      await request('/api/auth/change-password', { method: 'POST', body: { oldPassword: data.get('oldPassword'), newPassword: data.get('newPassword') } });
      form.reset();
      onPasswordChanged();
    } catch (error) { setPasswordError(error.message); }
    finally { setPasswordBusy(false); }
  }

  async function addLanguage(event) {
    event.preventDefault();
    if (!selected || !window.confirm('Thêm ngôn ngữ sẽ dịch toàn bộ thuyết minh tiếng Việt và sinh audio, có thể tốn credit. Tiếp tục?')) return;
    setLanguageBusy(true); setLanguageError(''); setLanguageMessage('Đang dịch và sinh audio, có thể mất một lúc…');
    try {
      const result = await request(`/api/languages/${encodeURIComponent(selected)}/translate`, { method: 'POST' });
      setLanguageMessage(translationMessage(result));
      await loadLanguages();
    } catch (error) { setLanguageMessage(''); setLanguageError(error.message); }
    finally { setLanguageBusy(false); }
  }

  return <>
    <p className="admin-kicker">QUẢN TRỊ / CÀI ĐẶT</p><h1>Cài đặt</h1>
    <section aria-labelledby="password-title">
      <h2 id="password-title">Đổi mật khẩu</h2>
      <form className="admin-filters" onSubmit={changePassword}>
        <label>Mật khẩu hiện tại<input name="oldPassword" type="password" autoComplete="current-password" required /></label>
        <label>Mật khẩu mới (tối thiểu 6 ký tự)<input name="newPassword" type="password" autoComplete="new-password" minLength={6} required /></label>
        <label>Xác nhận mật khẩu mới<input name="confirmPassword" type="password" autoComplete="new-password" minLength={6} required /></label>
        <button disabled={passwordBusy}>{passwordBusy ? 'Đang lưu…' : 'Đổi mật khẩu'}</button>
      </form>
      {passwordError && <p role="alert">{passwordError}</p>}
    </section>
    <section aria-labelledby="languages-title">
      <h2 id="languages-title">Ngôn ngữ thuyết minh</h2>
      <p>Thêm ngôn ngữ sẽ dịch các bản thuyết minh tiếng Việt và sinh audio.</p>
      {languages && <p>Ngôn ngữ hiện có: {languages.current.length ? languages.current.map(language => `${language.name} (${language.code})`).join(', ') : 'Chưa có'}</p>}
      {languageError && <p role="alert">{languageError} <button type="button" onClick={loadLanguages} disabled={languageBusy}>Tải lại danh sách</button></p>}
      {!languages && !languageError && <p role="status">Đang tải danh sách ngôn ngữ…</p>}
      {languages && <form className="admin-filters" onSubmit={addLanguage}>
        <label>Ngôn ngữ cần thêm<select value={selected} onChange={event => setSelected(event.target.value)} disabled={languageBusy || !languages.available.length}>
          {!languages.available.length && <option value="">Tất cả ngôn ngữ hỗ trợ đã có</option>}
          {languages.available.map(language => <option key={language.code} value={language.code}>{language.name} ({language.code})</option>)}
        </select></label>
        <button disabled={languageBusy || !selected}>{languageBusy ? 'Đang xử lý…' : 'Thêm ngôn ngữ (dịch + sinh audio)'}</button>
      </form>}
      {languageMessage && <p role="status">{languageMessage}</p>}
    </section>
  </>;
}
