import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Link, NavLink, Route, Routes, useLocation, useParams, useSearchParams } from 'react-router-dom';
import { getPlaces } from './api.js';
import { canShowImage } from './image.js';
import { nearbyPlaces } from './nearby.js';
import { freshPosition, requestPosition, locationMessage } from './location.js';
import { getLanguages, getPlaceDetail } from './detail.js';
import { AudioPlayer } from './player.jsx';
import { ExploreMap } from './map.jsx';
import { Scanner, About } from './qr.jsx';
import { readCachedPlaces, saveCachedPlaces } from './cache.js';
import { Admin } from './admin.jsx';
import './styles.css';

function PlaceImage({ place, hero = false }) {
  const [failed, setFailed] = useState(false);
  if (!canShowImage(place?.imageUrl) || failed) return <div className={`image-placeholder${hero ? ' image-placeholder-hero' : ''}`}><span>FIELD NOTES / AUDIO TOUR</span>{hero && <b>{place?.name || 'Mỗi địa điểm, một câu chuyện.'}</b>}<span>Không có ảnh địa điểm</span></div>;
  return <img src={place.imageUrl} alt={place.name} loading={hero ? 'eager' : 'lazy'} onError={() => setFailed(true)} />;
}

function PlaceCard({ place, index, distance }) {
  return <Link className="place-card" to={`/places/${place.id}`}><div className="place-photo"><PlaceImage place={place} /></div><p className="eyebrow">ĐIỂM DỪNG {String(index + 1).padStart(2, '0')}</p><h3>{place.name}<span aria-hidden="true">↗</span></h3><p className="address">{place.address || 'Chưa có thông tin địa chỉ'}</p>{distance != null && <p className="address">{distance < 1000 ? `${Math.round(distance / 10) * 10} m` : `${(distance / 1000).toFixed(1)} km`} · đường chim bay</p>}</Link>;
}

function NearbyStatus({ position, locationState, requestLocation }) {
  return <><p className="location-help">Dùng vị trí của bạn để tìm các địa điểm gần nhất. Khoảng cách tính theo đường chim bay. Vị trí dùng để sắp xếp trên thiết bị; khi dùng trình phát cũ, dữ liệu vị trí có thể được gửi lên server qua heartbeat.</p>{position?.accuracy > 100 && <p role="status" className="location-help">Vị trí có độ chính xác thấp (±{Math.round(position.accuracy)} m); khoảng cách chỉ để tham khảo.</p>}{locationState !== 'idle' && locationState !== 'ready' && <p role="status" className="location-help">{locationMessage(locationState)}</p>}{locationState !== 'denied' && <button className="primary" disabled={locationState === 'loading'} onClick={requestLocation}>{locationState === 'loading' ? 'Đang định vị…' : position ? 'Cập nhật vị trí' : locationState === 'idle' ? 'Dùng vị trí của tôi' : 'Thử lại'}</button>}</>;
}

function Home({ places, position, locationState, requestLocation }) {
  const featured = places.filter(p => p.parentId == null).slice(0, 3);
  const nearby = position ? nearbyPlaces(places, position, 5) : [];
  return <><section className="hero"><div><p className="eyebrow">KHÁM PHÁ · THEO NHỊP CỦA BẠN</p><h1>Mỗi điểm dừng,<br />một câu chuyện.</h1><p className="intro">Chậm lại một nhịp. Khám phá những địa điểm quanh bạn qua lời kể, âm thanh và những góc nhìn mới.</p><Link className="primary" to="/explore">Khám phá địa điểm <span aria-hidden="true">↗</span></Link><p className="margin-note">Không cần tài khoản.<br />Chọn điểm đến và bắt đầu lắng nghe.</p></div><div className="hero-photo"><PlaceImage place={featured[0]} hero /></div></section><section className="nearby-section"><div className="section-heading"><div><p className="eyebrow">KHÁM PHÁ QUANH BẠN</p><h2>Địa điểm gần bạn</h2></div>{position && <Link to="/explore?sort=near">Xem tất cả địa điểm gần bạn ↗</Link>}</div><NearbyStatus position={position} locationState={locationState} requestLocation={requestLocation} />{position && <div className="place-grid">{nearby.map(({ place, distance }, index) => <PlaceCard key={place.id} place={place} index={index} distance={distance} />)}</div>}{position && !nearby.length && <p className="notice">Chưa có địa điểm có tọa độ hợp lệ gần bạn.</p>}</section><section><div className="section-heading"><div><p className="eyebrow">BẮT ĐẦU HÀNH TRÌNH</p><h2>Những điểm dừng đáng ghé</h2></div><Link to="/explore">Xem tất cả ↗</Link></div><div className="place-grid">{featured.map((place, index) => <PlaceCard key={place.id} place={place} index={index} />)}</div>{!featured.length && <p className="notice">Chưa có địa điểm để giới thiệu.</p>}</section></>;
}

function Explore({ places, position, locationState, requestLocation }) {
  const [params, setParams] = useSearchParams();
  const query = params.get('q') || '';
  const sort = params.get('sort') || 'default';
  let results = places.filter(p => p.name.toLocaleLowerCase('vi').includes(query.trim().toLocaleLowerCase('vi')));
  if (sort === 'name') results.sort((a, b) => a.name.localeCompare(b.name, 'vi'));
  if (sort === 'near' && position) results = nearbyPlaces(results, position).map(({ place, distance }) => ({ ...place, distance }));
  function update(key, value) { const next = new URLSearchParams(params); if (value) next.set(key, value); else next.delete(key); setParams(next, { replace: true }); }
  return <section><p className="eyebrow">MỞ RA NHỮNG CÂU CHUYỆN MỚI</p><h1>Đi đâu hôm nay?</h1><p className="intro">Chọn một điểm dừng. Để câu chuyện dẫn lối.</p><div className="filters"><label><span>Tìm địa điểm</span><input type="search" value={query} onChange={event => update('q', event.target.value)} placeholder="Nhập tên địa điểm…" /></label><label><span>Sắp xếp</span><select value={sort === 'near' ? 'near' : sort === 'name' ? 'name' : 'default'} onChange={event => { const next = new URLSearchParams(params); if (event.target.value === 'near') next.set('previousSort', sort === 'near' ? params.get('previousSort') || 'default' : sort); next.set('sort', event.target.value); setParams(next, { replace: true }); }}><option value="default">Thứ tự danh sách</option><option value="name">Tên A–Z</option><option value="near">Gần tôi · đường chim bay</option></select></label></div>{sort === 'near' && <div className="location-panel"><NearbyStatus position={position} locationState={locationState} requestLocation={requestLocation} /><button className="text-button" onClick={() => { const next = new URLSearchParams(params); next.set('sort', params.get('previousSort') || 'default'); next.delete('previousSort'); setParams(next, { replace: true }); }}>Bỏ Gần tôi</button></div>}<p className="result-count" aria-live="polite">{results.length} địa điểm{sort === 'near' && !position ? ' · Chưa có vị trí; chưa sắp xếp theo khoảng cách' : ''}</p><div className="place-grid">{results.map((place, index) => <PlaceCard key={place.id} place={place} index={index} distance={place.distance} />)}</div>{!results.length && <p className="notice">{sort === 'near' && position ? 'Không có địa điểm khớp tìm kiếm với tọa độ hợp lệ.' : 'Không tìm thấy địa điểm phù hợp. Thử tên khác.'}</p>}</section>;
}

function LegacyDetail() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');
  const [attempt, setAttempt] = useState(0);
  const [languages, setLanguages] = useState([]);
  const [language, setLanguage] = useState('vi');
  useEffect(() => {
    const controller = new AbortController();
    getLanguages(controller.signal).then(setLanguages).catch(() => setLanguages([]));
    return () => controller.abort();
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    setDetail(null); setError('');
    getPlaceDetail(id, controller.signal, fetch, language).then(setDetail).catch(reason => {
      if (!controller.signal.aborted) setError(reason.message);
    });
    return () => controller.abort();
  }, [id, attempt, language]);
  if (error) return <section className="notice" role="alert"><h1>{error}</h1><Link to="/explore">Về khám phá</Link>{error !== 'Không tìm thấy địa điểm này.' && <button className="primary" onClick={() => setAttempt(value => value + 1)}>Thử lại</button>}</section>;
  if (!detail) return <p role="status" className="notice">Đang tải chi tiết địa điểm…</p>;
  const { place, children, narration } = detail;
  return <article className="detail"><Link to="/explore">← Quay lại khám phá</Link><div className="detail-hero"><div><p className="eyebrow">FIELD NOTES / ĐỊA ĐIỂM</p><h1>{place.name}</h1><p className="intro">{place.address || 'Chưa có thông tin địa chỉ'}</p>{languages.length > 0 && <label className="detail-language">Ngôn ngữ thuyết minh <select value={language} onChange={event => setLanguage(event.target.value)}>{!languages.some(item => item.code === language) && <option value={language}>{language}</option>}{languages.map(item => <option key={item.code} value={item.code}>{item.name}</option>)}</select></label>}<Link className="primary" to={`/player/${place.id}?lang=${encodeURIComponent(language)}`}>Nghe thuyết minh ↗</Link><p className="location-help">Chọn phát để bắt đầu; âm thanh không tự phát khi mở trang.</p>{Number.isFinite(place.latitude) && Math.abs(place.latitude) <= 90 && Number.isFinite(place.longitude) && Math.abs(place.longitude) <= 180 && <Link className="detail-map-link" to={`/map?place=${place.id}`}>Xem trên bản đồ ↗</Link>}</div><div className="detail-photo"><PlaceImage place={place} hero /></div></div><section className="detail-content"><h2>Câu chuyện địa điểm</h2>{narration?.textContent ? <p className="detail-text">{narration.textContent}</p> : <p className="notice">Chưa có bản thuyết minh cho ngôn ngữ đã chọn.</p>}</section>{children.length > 0 && <section><div className="section-heading"><h2>Khu vực bên trong</h2></div><div className="place-grid">{children.map((child, index) => <PlaceCard key={child.id} place={child} index={index} />)}</div></section>}</article>;
}

function App() {
  const { pathname } = useLocation();
  const previousPath = useRef(pathname);
  useEffect(() => {
    if (previousPath.current !== pathname) {
      document.getElementById('content')?.focus();
      previousPath.current = pathname;
    }
  }, [pathname]);
  const [places, setPlaces] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [attempt, setAttempt] = useState(0);
  const [cacheTime, setCacheTime] = useState(null);
  const [position, setPosition] = useState(null);
  const [locationState, setLocationState] = useState('idle');
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    if (!position) return;
    const timer = setInterval(() => setNow(Date.now()), 60000);
    return () => clearInterval(timer);
  }, [position]);
  const currentPosition = freshPosition(position, now) ? position : null;
  const currentLocationState = position && !currentPosition && locationState === 'ready' ? 'stale' : locationState;
  function requestLocation() {
    setLocationState('loading');
    requestPosition(navigator.geolocation, window.isSecureContext).then(fix => {
      if (!freshPosition(fix)) throw { code: 'stale' };
      setPosition(fix); setNow(Date.now()); setLocationState('ready');
    }).catch(error => { if (error.code === 'denied') setPosition(null); setLocationState(error.code || 'unavailable'); });
  }
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError('');
    getPlaces(controller.signal).then(data => { setPlaces(data); setCacheTime(null); saveCachedPlaces(localStorage, data); }).catch(error => { if (error.name !== 'AbortError') { const cached = readCachedPlaces(localStorage); if (cached) { setPlaces(cached.places); setCacheTime(cached.timestamp); } else setError(error.message); } }).finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [attempt]);
  const playerId = pathname.match(/^\/player\/([1-9]\d*)$/)?.[1];
  return <><a className="skip-link" href="#content">Đến nội dung</a><div className="release-note">Giao diện mới đang triển khai theo giai đoạn. <a href="/index.html">Dùng phiên bản hiện tại ↗</a></div><header><Link className="brand" to="/">audio tour<span>.</span></Link><nav aria-label="Điều hướng chính"><NavLink to="/" end>Trang chủ</NavLink><NavLink to="/explore">Khám phá</NavLink><NavLink to="/map">Bản đồ</NavLink><NavLink to="/qr">Quét QR</NavLink><NavLink to="/about">Giới thiệu</NavLink></nav><span className="edition">FIELD NOTES / 01</span></header><main id="content" tabIndex="-1">{loading ? <p role="status" className="notice">Đang tải địa điểm…</p> : error ? <div role="alert" className="notice"><h1>Chưa kết nối được hành trình.</h1><p>{error}</p><button className="primary" onClick={() => setAttempt(value => value + 1)}>Thử lại</button></div> : <>{cacheTime !== null && <div className="notice" role="status">Đang dùng danh sách đã lưu lúc {new Date(cacheTime).toLocaleString('vi-VN')}. Bản đồ và audio vẫn cần mạng. <button onClick={() => setAttempt(value => value + 1)}>Thử kết nối lại</button></div>}<Routes><Route path="/" element={<Home places={places} position={currentPosition} locationState={currentLocationState} requestLocation={requestLocation} />} /><Route path="/explore" element={<Explore places={places} position={currentPosition} locationState={currentLocationState} requestLocation={requestLocation} />} /><Route path="/places/:id" element={<LegacyDetail />} /><Route path="/map" element={<ExploreMap places={places} position={currentPosition} locationState={currentLocationState} requestLocation={requestLocation} />} /><Route path="/player/:id" element={null} /><Route path="/qr" element={<Scanner />} /><Route path="/about" element={<About />} /><Route path="*" element={<section className="notice"><h1>Không tìm thấy trang.</h1><Link to="/">Về trang chủ</Link></section>} /></Routes><AudioPlayer places={places} active={!!playerId} id={playerId} /></>}</main><footer className="site-footer"><Link className="brand" to="/">audio tour.</Link><p>Mỗi điểm dừng, một câu chuyện.</p><a href="/index.html">Phiên bản hiện tại ↗</a></footer></>;
}

createRoot(document.getElementById('root')).render(<React.StrictMode><BrowserRouter basename="/next"><Routes><Route path="/admin/*" element={<Admin />} /><Route path="*" element={<App />} /></Routes></BrowserRouter></React.StrictMode>);
