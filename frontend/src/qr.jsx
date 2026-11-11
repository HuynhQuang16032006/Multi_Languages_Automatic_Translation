import React, { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cameraSession, parseQr } from './qr.js';

export function Scanner() {
  const [input, setInput] = useState('');
  const [message, setMessage] = useState('');
  const [scanning, setScanning] = useState(false);
  const [busy, setBusy] = useState(false);
  const video = useRef(null);
  const stream = useRef(null);
  const frame = useRef(null);
  const alive = useRef(true);
  const session = useRef(cameraSession());
  const navigate = useNavigate();
  const stop = () => { session.current.stop(); if (frame.current) cancelAnimationFrame(frame.current); stream.current?.getTracks().forEach(track => track.stop()); stream.current = null; setScanning(false); };
  useEffect(() => { alive.current = true; return () => { alive.current = false; session.current.stop(); if (frame.current) cancelAnimationFrame(frame.current); stream.current?.getTracks().forEach(track => track.stop()); stream.current = null; }; }, []);

  async function resolve(value) {
    const parsed = parseQr(value, window.location.origin);
    if (!parsed) { setMessage('Mã hoặc liên kết không hợp lệ. Hãy nhập mã địa điểm hoặc liên kết Audio Tour cùng trang.'); return false; }
    setBusy(true); setMessage('');
    try {
      const response = await fetch(parsed.code ? `/api/locations/by-qr/${encodeURIComponent(parsed.code)}` : `/api/locations/${parsed.venue}`);
      if (!response.ok) { setMessage(response.status === 404 || response.status === 400 ? 'Không tìm thấy địa điểm cho mã này.' : 'Không tra được mã. Hãy thử lại.'); return false; }
      const place = await response.json();
      if (!Number.isSafeInteger(place?.id) || place.id <= 0) throw new Error('invalid');
      stop(); navigate(`/places/${place.id}`); return true;
    } catch { setMessage('Không tra được mã. Kiểm tra kết nối và thử lại.'); return false; }
    finally { setBusy(false); }
  }

  async function startCamera() {
    setMessage('');
    if (!window.isSecureContext) { setMessage('Camera cần HTTPS hoặc localhost. Bạn vẫn có thể nhập mã thủ công.'); return; }
    if (!navigator.mediaDevices?.getUserMedia || !('BarcodeDetector' in window)) { setMessage('Trình duyệt chưa hỗ trợ quét QR. Vui lòng nhập mã thủ công.'); return; }
    stop();
    const attempt = session.current.start();
    try {
      const detector = new window.BarcodeDetector({ formats: ['qr_code'] });
      const acquired = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false });
      if (!alive.current || !session.current.current(attempt)) { acquired.getTracks().forEach(track => track.stop()); return; }
      stream.current = acquired;
      video.current.srcObject = stream.current;
      await video.current.play();
      if (!alive.current || !session.current.current(attempt)) return;
      setScanning(true);
      const scan = async () => {
        if (!session.current.current(attempt)) return;
        try {
          const codes = await detector.detect(video.current);
          if (!session.current.current(attempt)) return;
          if (codes[0]?.rawValue) { const value = codes[0].rawValue; stop(); setInput(value); await resolve(value); return; }
        } catch { if (session.current.current(attempt)) { stop(); setMessage('Không đọc được camera. Hãy thử lại hoặc nhập mã.'); } return; }
        frame.current = requestAnimationFrame(scan);
      };
      frame.current = requestAnimationFrame(scan);
    } catch { if (alive.current && session.current.current(attempt)) { stop(); setMessage('Không mở được camera hoặc quyền bị từ chối. Kiểm tra quyền trình duyệt hoặc nhập mã.'); } }
  }

  return <section className="qr-page"><p className="eyebrow">FIELD NOTES / QUÉT MÃ</p><h1>Quét mã địa điểm</h1><p className="intro">Mở camera để nhận mã QR, hoặc nhập mã trên biển chỉ dẫn. Camera chỉ bật sau khi bạn chọn.</p><video ref={video} className="qr-video" playsInline muted hidden={!scanning} aria-label="Hình ảnh camera quét mã QR" />{scanning ? <button className="primary" onClick={stop}>Tắt camera</button> : <button className="primary" onClick={startCamera}>Mở camera quét QR</button>}<form className="qr-form" onSubmit={event => { event.preventDefault(); resolve(input); }}><label>Nhập mã QR hoặc liên kết<input value={input} onChange={event => setInput(event.target.value)} maxLength={2048} placeholder="Ví dụ: QR-KHANH-HOI-01" /></label><button className="primary" disabled={busy}>{busy ? 'Đang tra cứu…' : 'Tìm địa điểm'}</button></form>{message && <p className="notice" role="alert">{message}</p>}<Link to="/explore">Tiếp tục khám phá địa điểm →</Link></section>;
}

export function About() {
  return <article className="about-page"><p className="eyebrow">FIELD NOTES / HƯỚNG DẪN</p><h1>Mỗi điểm dừng, một câu chuyện.</h1><section><h2>Cách khám phá</h2><p>Chọn địa điểm trong danh sách hoặc trên bản đồ, mở chi tiết và nhấn phát để nghe thuyết minh. Trên trình phát, bạn có thể đổi ngôn ngữ, tua hoặc đọc văn bản.</p></section><section><h2>Quyền vị trí và camera</h2><p>Vị trí giúp xếp địa điểm theo khoảng cách đường chim bay và hiển thị trên bản đồ. Camera chỉ dùng khi bạn chọn quét QR; bạn có thể nhập mã thay thế. Trình duyệt có thể yêu cầu HTTPS và quyền thiết bị. Trình phát hiện có gửi heartbeat phiên nghe tới máy chủ.</p></section><section><h2>Kết nối và tải xuống</h2><p>Bản đồ, audio và nội dung cần kết nối. Gói ZIP chứa nội dung và audio để bạn tải về; tải ZIP không đồng nghĩa ứng dụng web phát offline. Không cam kết bản đồ có tile offline.</p><p>HTML cũ tiếp tục cung cấp bộ nhớ đệm danh sách POI khi mất mạng.</p></section><Link className="primary" to="/explore">Khám phá địa điểm ↗</Link></article>;
}
