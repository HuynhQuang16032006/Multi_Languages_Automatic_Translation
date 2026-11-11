import React, { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { mapPlaces, selectedPlace } from './map-data.js';
import { nearbyPlaces } from './nearby.js';
import { updateGeofence } from './geofence.js';
import { locationMessage } from './location.js';

const tiles = 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}';
const attribution = 'Tiles &copy; Esri &mdash; Source: Esri, Maxar, Earthstar Geographics, and the GIS User Community';

export function ExploreMap({ places, position, locationState, requestLocation }) {
  const [params, setParams] = useSearchParams();
  const [expanded, setExpanded] = useState(false);
  const [query, setQuery] = useState('');
  const mapElement = useRef(null);
  const map = useRef(null);
  const markers = useRef(null);
  const userLayer = useRef(null);
  const [ready, setReady] = useState(false);
  const [mapError, setMapError] = useState('');
  const [auto, setAuto] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const fence = useRef({});
  const valid = mapPlaces(places);
  const filtered = valid.filter(item => item.name.toLocaleLowerCase('vi').includes(query.trim().toLocaleLowerCase('vi')));
  const ranked = position ? nearbyPlaces(filtered, position).map(({ place, distance }) => ({ ...place, distance })) : filtered;
  const selected = selectedPlace(valid, params.get('place'));
  const select = id => { const next = new URLSearchParams(params); if (id) next.set('place', id); else next.delete('place'); setParams(next); };

  useEffect(() => {
    if (!auto) return;
    if (!navigator.geolocation || !window.isSecureContext) { setAuto(false); setMapError('GPS không được hỗ trợ hoặc kết nối chưa an toàn.'); return; }
    const watch = navigator.geolocation.watchPosition(fix => {
      const result = updateGeofence(fence.current, places, { latitude: fix.coords.latitude, longitude: fix.coords.longitude, accuracy: fix.coords.accuracy, timestamp: fix.timestamp });
      fence.current = result.state;
      if (result.entered) setSuggestion(result.entered);
    }, () => { setAuto(false); setMapError('Không theo dõi được GPS. Hãy kiểm tra quyền vị trí và thử lại.'); }, { enableHighAccuracy: true, timeout: 12000, maximumAge: 5000 });
    return () => navigator.geolocation.clearWatch(watch);
  }, [auto, places]);

  useEffect(() => {
    let cancelled = false;
    const load = () => {
      if (window.L) return Promise.resolve(window.L);
      if (!document.querySelector('#leaflet-css')) { const style = document.createElement('link'); style.id = 'leaflet-css'; style.rel = 'stylesheet'; style.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'; document.head.append(style); }
      return new Promise((resolve, reject) => {
        const script = document.createElement('script'); script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
        script.onload = () => resolve(window.L); script.onerror = () => reject(new Error('Không tải được thư viện bản đồ.'));
        document.head.append(script);
      });
    };
    load().then(L => {
      if (cancelled || !mapElement.current) return;
      map.current = L.map(mapElement.current).setView([10.7705, 106.699], 13);
      L.tileLayer(tiles, { attribution, maxZoom: 19 }).on('tileerror', () => setMapError('Không tải được một số ô bản đồ. Kiểm tra kết nối mạng.')).addTo(map.current);
      markers.current = L.layerGroup().addTo(map.current);
      userLayer.current = L.layerGroup().addTo(map.current);
      setReady(true);
    }).catch(error => { if (!cancelled) setMapError(error.message); });
    return () => { cancelled = true; map.current?.remove(); map.current = null; setReady(false); };
  }, []);

  useEffect(() => {
    if (!ready) return;
    const L = window.L;
    markers.current.clearLayers();
    ranked.forEach(item => {
      const marker = L.circleMarker([item.latitude, item.longitude], { radius: selected?.id === item.id ? 11 : 8, color: selected?.id === item.id ? '#a6402d' : '#173b34', weight: 3, fillOpacity: .85 });
      const tooltip = document.createElement('span');
      tooltip.textContent = item.name;
      marker.bindTooltip(tooltip).on('click', () => select(item.id));
      marker.addTo(markers.current);
      const path = marker.getElement();
      if (path) {
        path.setAttribute('tabindex', '0');
        path.setAttribute('role', 'button');
        path.setAttribute('aria-label', item.name);
        path.addEventListener('keydown', event => {
          if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); select(item.id); }
        });
      }
    });
  }, [ready, places, query, position, selected?.id]);

  useEffect(() => {
    if (!ready) return;
    userLayer.current.clearLayers();
    if (position) {
      const L = window.L;
      L.circle([position.latitude, position.longitude], { radius: Math.max(0, position.accuracy || 0), color: '#a6402d', fillOpacity: .08 }).addTo(userLayer.current);
      L.circleMarker([position.latitude, position.longitude], { radius: 7, color: '#fff', weight: 2, fillColor: '#a6402d', fillOpacity: 1 }).bindTooltip('Vị trí của tôi').addTo(userLayer.current);
    }
  }, [ready, position]);

  useEffect(() => { if (ready) map.current.invalidateSize(); }, [ready, expanded]);

  return <section className="map-page"><p className="eyebrow">FIELD NOTES / BẢN ĐỒ</p><h1>Khám phá trên bản đồ</h1><p className="intro">Chọn ghim hoặc tên địa điểm. Chọn ghim không tự phát âm thanh.</p><div className="geo-option"><p>Thông báo khi vào vùng địa điểm. GPS chỉ theo dõi khi trang bản đồ đang mở; vị trí cũ hoặc sai số lớn không kích hoạt. Không tự chuyển bài khi bạn đang nghe hoặc đã tạm dừng.</p><button className="text-button" aria-pressed={auto} onClick={() => { setAuto(value => !value); setSuggestion(null); fence.current = {}; }}>{auto ? 'Tắt gợi ý theo GPS' : 'Bật gợi ý theo GPS'}</button>{suggestion && <p role="status">Bạn đã vào vùng {suggestion.name}. <Link to={`/places/${suggestion.id}`}>Xem địa điểm</Link></p>}</div><div className="map-layout"><div className="map-panel"><div className="map-tools"><button className="primary" onClick={() => { if (!position) requestLocation(); else map.current?.setView([position.latitude, position.longitude], Math.max(map.current.getZoom(), 15)); }}>{position ? 'Vị trí của tôi' : 'Dùng vị trí của tôi'}</button>{locationState !== 'idle' && locationState !== 'ready' && <p role="status">{locationMessage(locationState)}</p>}</div><div ref={mapElement} className="leaflet-map" aria-label="Bản đồ địa điểm" />{mapError && <p role="alert">{mapError} Danh sách địa điểm vẫn dùng được.</p>}</div><aside className={`map-sheet${expanded ? ' map-sheet-open' : ''}`}><button className="map-sheet-toggle" aria-expanded={expanded} onClick={() => setExpanded(value => !value)}>{expanded ? 'Thu gọn danh sách' : 'Mở danh sách địa điểm'}</button><label>Tìm trên bản đồ<input type="search" value={query} onChange={event => setQuery(event.target.value)} placeholder="Nhập tên địa điểm…" /></label>{selected && <div className="map-selected"><p className="eyebrow">ĐỊA ĐIỂM ĐÃ CHỌN</p><h2>{selected.name}</h2><p>{selected.address || 'Chưa có địa chỉ'}</p>{position && <p>{Math.round(nearbyPlaces([selected], position)[0]?.distance || 0)} m · đường chim bay</p>}<Link className="primary" to={`/places/${selected.id}`}>Xem địa điểm ↗</Link></div>}<p className="result-count">{ranked.length} địa điểm trên bản đồ</p><div className="map-list">{ranked.map(item => <button key={item.id} aria-pressed={selected?.id === item.id} onClick={() => select(item.id)}><b>{item.name}</b><span>{item.address || 'Chưa có địa chỉ'}</span>{item.distance != null && <span>{Math.round(item.distance)} m · đường chim bay</span>}</button>)}</div>{!ranked.length && <p className="notice">Không có địa điểm khớp với từ khóa và tọa độ hợp lệ.</p>}</aside></div></section>;
}
