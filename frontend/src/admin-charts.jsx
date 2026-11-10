import React, { useState } from 'react';
import { chartQuery, chartRows } from './admin-charts.js';
import { parseAdminDate } from './admin-date.js';

function Bars({ title, rows, vertical = false }) {
  const max = Math.max(1, ...rows.map(([, count]) => count));
  return <section><h2>{title}</h2>{rows.length ? vertical ? <div className="chart-columns-scroll"><ul className="chart-columns">{rows.map(([label, count]) => <li key={label}><strong>{count}</strong><span className="chart-column-track" aria-hidden="true"><span style={{ height: `${count / max * 100}%` }} /></span><span>{label}</span></li>)}</ul></div> : <ul className="chart-bars">{rows.map(([label, count]) => <li key={label}><span>{label}</span><span className="chart-track" aria-hidden="true"><span style={{ width: `${count / max * 100}%` }} /></span><strong>{count}</strong></li>)}</ul> : <p>Không có lượt nghe trong khoảng ngày này.</p>}</section>;
}

export function Charts({ request }) {
  const [charts, setCharts] = useState(null); const [error, setError] = useState(''); const [loading, setLoading] = useState(false);
  const [online, setOnline] = useState(null); const [onlineError, setOnlineError] = useState(''); const [onlineBusy, setOnlineBusy] = useState(false);
  async function load(event) {
    event.preventDefault(); const form = new FormData(event.currentTarget); setError(''); setLoading(true); setCharts(null);
    try { setCharts(chartRows(await request(`/api/analytics/dashboard${chartQuery(parseAdminDate(form.get('from')), parseAdminDate(form.get('to')))}`))); }
    catch (reason) { setError(reason.message); } finally { setLoading(false); }
  }
  async function updateOnline() {
    setOnlineBusy(true); setOnlineError('');
    try { const data = await request('/api/analytics/dashboard'); if (!Number.isInteger(data.listeningUsers)) throw new Error('Dữ liệu phiên nghe không hợp lệ.'); setOnline({ count: data.listeningUsers, time: new Date() }); }
    catch (reason) { setOnline(null); setOnlineError(reason.message); } finally { setOnlineBusy(false); }
  }
  return <><p className="admin-kicker">THỐNG KÊ / LƯỢT NGHE</p><h1>Biểu đồ</h1><p>Lượt nghe đã kết thúc theo thời điểm ghi nhận. Hai biểu đồ dùng cùng khoảng ngày.</p><form className="admin-filters" onSubmit={load}><label>Từ ngày (dd/mm/yyyy)<input name="from" placeholder="dd/mm/yyyy" inputMode="numeric" required /></label><label>Đến ngày (dd/mm/yyyy)<input name="to" placeholder="dd/mm/yyyy" inputMode="numeric" required /></label><button disabled={loading}>{loading ? 'Đang tải…' : 'Xem biểu đồ'}</button></form>{error && <p role="alert">{error}</p>}{charts && <><Bars title="Lượt nghe theo ngày" rows={charts.days} vertical /><Bars title="Lượt nghe theo ngôn ngữ" rows={charts.languages} /></>}<section><h2>Phiên nghe trực tuyến</h2><p>Gồm người đang tạm dừng. Số phiên chỉ cập nhật khi bấm nút; phiên mất kết nối có thể mất tối đa 8 giây để hết hạn.</p><button onClick={updateOnline} disabled={onlineBusy}>{onlineBusy ? 'Đang cập nhật…' : 'Cập nhật số đang nghe'}</button>{onlineError && <p role="alert">{onlineError}</p>}{online && <p role="status"><strong>{online.count}</strong> phiên nghe · cập nhật lúc {online.time.toLocaleTimeString('vi-VN')}</p>}</section></>;
}
