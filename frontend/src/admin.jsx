import React, { useEffect, useState } from 'react';
import { Link, NavLink, Route, Routes } from 'react-router-dom';
import { adminRequest, login } from './admin-api.js';
import { Places, Audio, Qr } from './admin-management.jsx';
import { Charts } from './admin-charts.jsx';
import { parseAdminDate } from './admin-date.js';
import './admin.css';

function Login({ onLogin }) {
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('');
    const data = new FormData(event.currentTarget);
    try { onLogin(await login(data.get('username'), data.get('password'))); }
    catch (reason) { setError(reason.message); }
    finally { setBusy(false); }
  }
  return <main className="admin-login"><form onSubmit={submit}><p className="admin-kicker">AUDIO TOUR / STUDIO CONSOLE</p><h1>Đăng nhập quản trị</h1><p>Quản lý địa điểm và nội dung thuyết minh.</p><label>Tên đăng nhập<input name="username" autoComplete="username" required /></label><label>Mật khẩu<input name="password" type="password" autoComplete="current-password" required /></label>{error && <p role="alert">{error}</p>}<button disabled={busy}>{busy ? 'Đang đăng nhập…' : 'Đăng nhập'}</button><p><Link to="/">← Về trang du khách</Link></p></form></main>;
}

function Dashboard({ request }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ from: '', to: '', languageCode: '' });
  useEffect(() => {
    let active = true; setData(null); setError('');
    const params = new URLSearchParams(Object.entries(filters).filter(([, value]) => value));
    request(`/api/analytics/dashboard${params.size ? `?${params}` : ''}`).then(result => { if (active) setData(result); }).catch(reason => { if (active) setError(reason.message); });
    return () => { active = false; };
  }, [filters, request]);
  function submit(event) { event.preventDefault(); const values = new FormData(event.currentTarget); try { const from = values.get('from') ? `${parseAdminDate(values.get('from'))}T${values.get('fromTime') || '00:00'}` : ''; const to = values.get('to') ? `${parseAdminDate(values.get('to'))}T${values.get('toTime') || '23:59:59.999999'}` : ''; if (from && to && from > to) throw new Error('Khoảng ngày không hợp lệ.'); setError(''); setFilters({ from, to, languageCode: values.get('languageCode') }); } catch (reason) { setError(reason.message); } }
  return <><p className="admin-kicker">TỔNG QUAN / DỮ LIỆU THỰC TẾ</p><h1>Dashboard</h1><p>Thống kê lượt nghe và phiên hoạt động từ hệ thống.</p><form className="admin-filters" onSubmit={submit}><label>Từ ngày (dd/mm/yyyy)<input name="from" placeholder="dd/mm/yyyy" inputMode="numeric" /></label><label>Giờ bắt đầu<input name="fromTime" type="time" /></label><label>Đến ngày (dd/mm/yyyy)<input name="to" placeholder="dd/mm/yyyy" inputMode="numeric" /></label><label>Giờ kết thúc<input name="toTime" type="time" /></label><label>Mã ngôn ngữ<input name="languageCode" placeholder="Tất cả" /></label><button>Áp dụng</button></form>{error ? <p role="alert">{error}</p> : !data ? <p role="status">Đang tải thống kê…</p> : <><div className="admin-metrics"><div><span>Lượt nghe</span><strong>{data.totalListens ?? '—'}</strong></div><div><span>Thiết bị đang hoạt động</span><strong>{data.activeUsers ?? '—'}</strong></div><div><span>Thiết bị duy nhất</span><strong>{data.uniqueDevices ?? '—'}</strong></div><div><span>Lượt tải offline</span><strong>{data.offlineDownloads ?? '—'}</strong></div></div><section><h2>Địa điểm được nghe</h2>{data.topLocations?.length ? <ul>{data.topLocations.map(item => <li key={item.locationId}>{item.locationName} — {item.listenCount} lượt</li>)}</ul> : <p>Không có dữ liệu trong khoảng đã chọn.</p>}</section><section><h2>Phiên gần đây</h2>{data.recentLogs?.length ? <ul>{data.recentLogs.map(item => <li key={item.id}>{item.locationName} · {item.languageCode} · {item.durationSeconds} giây · {item.completedAt}</li>)}</ul> : <p>Chưa có phiên nghe.</p>}</section></>}</>;
}

export function Admin() {
  const [token, setToken] = useState(null);
  const [sessionError, setSessionError] = useState('');
  const request = React.useCallback(async (url, options) => {
    try { return await adminRequest(url, token, options); }
    catch (error) { if (error.status === 401) { setToken(null); setSessionError(error.message); } throw error; }
  }, [token]);
  if (!token) return <>{sessionError && <p className="admin-session" role="alert">{sessionError}</p>}<Login onLogin={value => { setSessionError(''); setToken(value); }} /></>;
  return <div className="admin-shell"><aside><Link to="/admin" className="admin-brand">audio tour<span>.</span><small>STUDIO CONSOLE</small></Link><nav aria-label="Điều hướng quản trị"><NavLink to="/admin" end>Dashboard</NavLink><NavLink to="/admin/charts">Biểu đồ</NavLink><NavLink to="/admin/places">Địa điểm</NavLink><NavLink to="/admin/audio">Thuyết minh</NavLink><NavLink to="/admin/qr">Mã QR</NavLink></nav><a href="/dashboard.html">Quản trị phiên bản cũ ↗</a><button onClick={() => setToken(null)}>Đăng xuất</button></aside><main id="admin-content"><Routes><Route index element={<Dashboard request={request} />} /><Route path="charts" element={<Charts request={request} />} /><Route path="places" element={<Places request={request} />} /><Route path="audio" element={<Audio request={request} />} /><Route path="qr" element={<Qr request={request} />} /><Route path="*" element={<section><h1>Không tìm thấy trang</h1><Link to="/admin">Về Dashboard</Link></section>} /></Routes></main></div>;
}
