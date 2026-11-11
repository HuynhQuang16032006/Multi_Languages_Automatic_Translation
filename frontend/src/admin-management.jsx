import React, { useEffect, useState } from 'react';

const fields = [['name', 'Tên địa điểm', 'text'], ['address', 'Địa chỉ', 'text'], ['latitude', 'Vĩ độ', 'number'], ['longitude', 'Kinh độ', 'number'], ['radiusInMeters', 'Bán kính (m)', 'number'], ['priority', 'Ưu tiên', 'number'], ['qrCode', 'Mã QR', 'text'], ['imageUrl', 'URL ảnh', 'url'], ['parentId', 'ID địa điểm cha', 'number']];

function usePlaces(request) {
  const [places, setPlaces] = useState([]); const [error, setError] = useState('');
  async function reload() { try { setPlaces(await request('/api/admin/locations')); setError(''); } catch (e) { setError(e.message); } }
  useEffect(() => { reload(); }, [request]);
  return { places, error, reload };
}

export function Places({ request }) {
  const { places, error, reload } = usePlaces(request); const [item, setItem] = useState(null); const [message, setMessage] = useState('');
  async function save(event) {
    event.preventDefault(); const form = new FormData(event.currentTarget); const body = {};
    for (const [name, , type] of fields) { const value = form.get(name).trim(); body[name] = value === '' ? null : type === 'number' ? Number(value) : value; }
    try { await request(item.id ? `/api/locations/${item.id}` : '/api/locations', { method: item.id ? 'PUT' : 'POST', body }); setItem(null); await reload(); setMessage('Đã lưu địa điểm.'); } catch (e) { setMessage(e.message); }
  }
  async function remove(place) { if (!window.confirm(`Xóa địa điểm “${place.name}”?`)) return; try { await request(`/api/locations/${place.id}`, { method: 'DELETE' }); await reload(); setMessage('Đã xóa địa điểm.'); } catch (e) { setMessage(e.message); } }
  async function visibility(place) { const visible = !place.visible; if (!window.confirm(`${visible ? 'Hiện' : 'Ẩn'} địa điểm “${place.name}”?`)) return; try { await request(`/api/admin/locations/${place.id}/visibility`, { method: 'PATCH', body: { visible } }); await reload(); setMessage(visible ? 'Đã hiện địa điểm.' : 'Đã ẩn địa điểm.'); } catch (e) { setMessage(e.message); } }
  return <><p className="admin-kicker">QUẢN LÝ / ĐỊA ĐIỂM</p><h1>Địa điểm</h1><p>Danh sách bao gồm cả địa điểm ẩn.</p><button onClick={() => { setItem({}); setMessage(''); }}>Thêm địa điểm</button>{(message || error) && <p role="status">{message || error}</p>}{item && <section><h2>{item.id ? 'Sửa địa điểm' : 'Thêm địa điểm'}</h2><form className="admin-form" key={item.id || 'new'} onSubmit={save}>{fields.map(([name, label, type]) => <label key={name}>{label}<input name={name} type={type} defaultValue={item[name] ?? ''} required={name === 'name'} step={type === 'number' ? 'any' : undefined} /></label>)}<div><button>Lưu địa điểm</button> <button type="button" onClick={() => setItem(null)}>Hủy</button></div></form></section>}<section><h2>Danh sách địa điểm</h2>{places.length ? <ul className="admin-list">{places.map(place => <li key={place.id}><span><strong>{place.name}</strong><br />{place.address || 'Chưa có địa chỉ'} · ID {place.id} · {place.visible ? 'Đang hiện' : 'Đang ẩn'}</span><span><button onClick={() => setItem(place)}>Sửa</button> <button onClick={() => visibility(place)}>{place.visible ? 'Ẩn' : 'Hiện'}</button> <button onClick={() => remove(place)}>Xóa</button></span></li>)}</ul> : <p>{error || 'Không có địa điểm.'}</p>}</section></>;
}

export function Audio({ request }) {
  const { places, error } = usePlaces(request); const [location, setLocation] = useState(''); const [items, setItems] = useState([]); const [edit, setEdit] = useState(null); const [message, setMessage] = useState('');
  async function reload(id) { if (!id) { setItems([]); return; } try { setItems(await request(`/api/admin/locations/${id}/narrations`)); } catch (e) { setMessage(e.message); } }
  useEffect(() => { reload(location); }, [location, request]);
  async function save(event) { event.preventDefault(); const form = new FormData(event.currentTarget); try { await request(`/api/admin/locations/${location}/narrations${edit.id ? `/${edit.id}` : ''}`, { method: edit.id ? 'PUT' : 'POST', body: Object.fromEntries(form) }); setEdit(null); await reload(location); setMessage('Đã lưu bài thuyết minh. Audio cũ được giữ nguyên khi sửa văn bản.'); } catch (e) { setMessage(e.message); } }
  async function remove(item) { if (!window.confirm('Xóa bài thuyết minh này?')) return; try { await request(`/api/admin/locations/${location}/narrations/${item.id}`, { method: 'DELETE' }); await reload(location); setMessage('Đã xóa bài thuyết minh.'); } catch (e) { setMessage(e.message); } }
  async function generate(item) { if (!window.confirm('Sinh audio có thể dùng credit TTS. Tiếp tục?')) return; try { await request(`/api/narrations/${item.id}/audio?force=false`, { method: 'POST' }); await reload(location); setMessage('Đã xử lý yêu cầu audio.'); } catch (e) { setMessage(e.message); } }
  return <><p className="admin-kicker">QUẢN LÝ / NỘI DUNG</p><h1>Thuyết minh & audio</h1><label>Địa điểm<select value={location} onChange={e => { setLocation(e.target.value); setEdit(null); }}><option value="">Chọn địa điểm</option>{places.map(place => <option key={place.id} value={place.id}>{place.name}</option>)}</select></label>{(message || error) && <p role="status">{message || error}</p>}{location && <><p>Mỗi địa điểm chỉ có một bài cho mỗi ngôn ngữ. Sửa văn bản không thay audio đã sinh.</p><button onClick={() => setEdit({})}>Thêm bài</button>{edit && <section><h2>{edit.id ? 'Sửa bài' : 'Thêm bài'}</h2><form className="admin-form" key={edit.id || 'new'} onSubmit={save}><label>Mã ngôn ngữ<input name="languageCode" defaultValue={edit.languageCode || ''} required maxLength="10" /></label><label>Nội dung<textarea name="textContent" defaultValue={edit.textContent || ''} required rows="8" /></label><div><button>Lưu</button> <button type="button" onClick={() => setEdit(null)}>Hủy</button></div></form></section>}<section><h2>Danh sách bài</h2>{items.length ? <ul className="admin-list">{items.map(item => <li key={item.id}><span><strong>{item.languageCode}</strong><br />{item.audioUrl ? 'Có audio' : 'Chưa có audio'}</span><span><button onClick={() => setEdit(item)}>Sửa</button> <button onClick={() => remove(item)}>Xóa</button> {!item.audioUrl && <button onClick={() => generate(item)}>Sinh audio</button>}</span></li>)}</ul> : <p>Chưa có bài thuyết minh.</p>}</section></>}</>;
}

export { Qr } from './admin-qr.jsx';
