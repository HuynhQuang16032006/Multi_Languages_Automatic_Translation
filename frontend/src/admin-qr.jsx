import React, { useEffect, useState } from 'react';
import QRCode from 'qrcode';

const codePattern = /^[A-Za-z0-9_-]{1,50}$/;

function publicOrigin(value) {
  try {
    const url = new URL(value.trim());
    const host = url.hostname.toLowerCase().replace(/^\[|\]$/g, '');
    if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password || url.pathname !== '/' || url.search || url.hash || host === 'localhost' || host.endsWith('.localhost') || host === '::1' || host === '0.0.0.0' || /^127\./.test(host)) return null;
    return url.origin;
  } catch { return null; }
}

export function Qr({ request }) {
  const [places, setPlaces] = useState([]);
  const [selected, setSelected] = useState('');
  const [code, setCode] = useState('');
  const defaultOrigin = ['localhost', '127.0.0.1', '::1', '0.0.0.0'].includes(window.location.hostname.toLowerCase())
    ? `${window.location.protocol}//172.20.10.8:${window.location.port || '8080'}`
    : window.location.origin;
  const [host, setHost] = useState(defaultOrigin);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [generated, setGenerated] = useState(null);
  const [imageError, setImageError] = useState(false);
  const place = places.find(item => String(item.id) === selected);
  const origin = publicOrigin(host);
  const link = origin && place?.qrCode && codePattern.test(place.qrCode) ? `${origin}/next/qr?code=${encodeURIComponent(place.qrCode)}` : '';
  const image = generated?.link === link ? generated.url : '';

  useEffect(() => {
    let active = true;
    request('/api/admin/locations').then(items => {
      if (active && Array.isArray(items) && items.length > 0) {
        setPlaces(items);
        const def = items.find(i => i.id === 4 && i.qrCode) || items.find(i => i.qrCode) || items[0];
        if (def) {
          setSelected(String(def.id));
          setCode(def.qrCode || '');
        }
      }
    }).catch(error => { if (active) setMessage(error.message); });
    if (['localhost', '127.0.0.1', '::1', '0.0.0.0'].includes(window.location.hostname.toLowerCase())) {
      request('/api/analytics/dashboard').then(data => {
        if (active && data?.serverIp) {
          const port = window.location.port ? `:${window.location.port}` : ':8080';
          setHost(`${window.location.protocol}//${data.serverIp}${port}`);
        }
      }).catch(() => {});
    }
    return () => { active = false; };
  }, [request]);

  useEffect(() => {
    let active = true;
    setGenerated(null); setImageError(false);
    if (link) QRCode.toDataURL(link, { width: 400, margin: 4 }).then(url => { if (active) setGenerated({ link, url }); }).catch(() => { if (active) setImageError(true); });
    return () => { active = false; };
  }, [link]);

  async function save(event) {
    event.preventDefault();
    const value = code.trim();
    if (!place || !codePattern.test(value)) { setMessage('Mã phải có 1–50 ký tự chữ, số, dấu gạch ngang hoặc gạch dưới.'); return; }
    if (place.qrCode && place.qrCode !== value && !window.confirm('Đổi mã sẽ làm các mã QR đã in trước đây không còn hoạt động. Tiếp tục?')) return;
    setBusy(true); setMessage('');
    try {
      await request(`/api/locations/${place.id}/qr`, { method: 'PATCH', body: { qrCode: value, expectedQrCode: place.qrCode ?? null } });
      setPlaces(items => items.map(item => item.id === place.id ? { ...item, qrCode: value } : item));
      setImageError(false); setMessage('Đã lưu mã QR.');
    } catch (error) { setMessage(error.message); }
    finally { setBusy(false); }
  }

  function download() {
    const anchor = document.createElement('a');
    anchor.href = image; anchor.download = `qr-${place.qrCode}.png`; anchor.click();
  }

  return <><p className="admin-kicker">QUẢN LÝ / MÃ QR</p><h1>Mã QR</h1><p>Chọn địa điểm, lưu mã rồi xem trước hoặc tải ảnh. Thay đổi mã sẽ vô hiệu hóa các mã đã in.</p>
    <label>Địa điểm<select value={selected} onChange={event => { const id = event.target.value; setSelected(id); setCode(places.find(item => String(item.id) === id)?.qrCode || ''); setMessage(''); setImageError(false); }}><option value="">Chọn địa điểm</option>{places.map(item => <option key={item.id} value={item.id}>{item.name}{item.visible === false ? ' (đang ẩn)' : ''}</option>)}</select></label>
    {place && <><form className="admin-form" onSubmit={save}><label>Mã địa điểm<input value={code} onChange={event => setCode(event.target.value)} maxLength={50} pattern="[A-Za-z0-9_-]{1,50}" required /></label><button disabled={busy}>Lưu mã</button></form>
      <label>Địa chỉ gốc ứng dụng khách<input type="url" value={host} onChange={event => setHost(event.target.value)} placeholder="https://example.com" /></label><p>Nhập địa chỉ HTTP(S) mà điện thoại truy cập được, không dùng localhost. Với mạng nội bộ, điện thoại phải cùng mạng.</p>
      {!origin && <p role="alert">Địa chỉ phải là HTTP(S), không có đường dẫn, thông tin đăng nhập hoặc host loopback.</p>}
      {place.visible === false && <p role="status">Địa điểm đang ẩn; khách không thể mở mã này cho đến khi địa điểm được hiện.</p>}
      {place.qrCode && !codePattern.test(place.qrCode) && <p role="alert">Mã đã lưu không hợp lệ. Hãy cập nhật mã trước khi tạo ảnh.</p>}
      {link && <section><h2>Xem trước</h2><p><a href={link} target="_blank" rel="noreferrer">{link}</a></p>{imageError ? <p role="alert">Không tạo được ảnh QR. Hãy kiểm tra độ dài liên kết.</p> : image && <img src={image} alt={`Mã QR của ${place.name}`} width="200" height="200" />}<p><button type="button" onClick={download} disabled={!image || busy}>Tải ảnh PNG</button></p><p>Quét thử bằng điện thoại trước khi in; hệ thống không thể xác nhận điện thoại truy cập được host từ trình duyệt quản trị.</p></section>}</>}
    {message && <p role="status">{message}</p>}
  </>;
}
