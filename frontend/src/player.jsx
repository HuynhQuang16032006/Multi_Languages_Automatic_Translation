import React, { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getLanguages } from './detail.js';
import { createListeningSession, sendListeningHeartbeat } from './listening.js';

const clock = seconds => Number.isFinite(seconds) ? `${Math.floor(seconds / 60).toString().padStart(2, '0')}:${Math.floor(seconds % 60).toString().padStart(2, '0')}` : '--:--';

export function AudioPlayer({ places, active, id }) {
  const [params] = useSearchParams();
  const [selectedId, setSelectedId] = useState(null);
  useEffect(() => { if (active) setSelectedId(id); }, [active, id]);
  const place = places.find(item => String(item.id) === (active ? id : selectedId));
  const [language, setLanguage] = useState('vi');
  const [languages, setLanguages] = useState([]);
  const [narration, setNarration] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [playing, setPlaying] = useState(false);
  const [time, setTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [attempt, setAttempt] = useState(0);
  const audio = useRef(null);
  const device = useRef(null);
  const session = useRef(null);
  const current = useRef(null);
  useEffect(() => { if (active && params.get('lang') && languages.some(item => item.code === params.get('lang'))) setLanguage(params.get('lang')); }, [active, id, languages]);

  if (!device.current) {
    try {
      device.current = localStorage.getItem('audiotour_device_id') || crypto.randomUUID();
      localStorage.setItem('audiotour_device_id', device.current);
    } catch { device.current = crypto.randomUUID(); }
  }

  useEffect(() => {
    const controller = new AbortController();
    getLanguages(controller.signal).then(setLanguages).catch(() => setLanguages([]));
    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (!place) return;
    const controller = new AbortController();
    setLoading(true); setMessage(''); setNarration(null);
    fetch(`/api/narrations?locationId=${place.id}&languageCode=${encodeURIComponent(language)}`, { signal: controller.signal })
      .then(async response => {
        if (response.status === 404) return null;
        if (!response.ok) throw new Error(`Không tải được thuyết minh (HTTP ${response.status}).`);
        return response.json();
      }).then(data => { if (!controller.signal.aborted) setNarration(data); })
      .catch(error => { if (!controller.signal.aborted) setMessage(error.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [place?.id, language, attempt]);

  // One audio element and one session for the mounted app, including route changes.
  useEffect(() => {
    const previous = current.current;
    if (previous && (previous.id !== place?.id || previous.language !== language)) {
      session.current?.end();
      if (audio.current) { audio.current.pause(); audio.current.removeAttribute('src'); audio.current.load(); }
      setPlaying(false); setTime(0); setDuration(0);
    }
    current.current = place ? { id: place.id, language } : null;
    if (!place) return;
    session.current = createListeningSession((action, seconds) => {
      sendListeningHeartbeat(device.current, place, language, action, seconds).catch(() => setMessage('Không gửi được trạng thái phiên nghe.'));
    });
  }, [place?.id, language]);

  useEffect(() => () => {
    session.current?.end();
    audio.current?.pause();
  }, []);

  useEffect(() => {
    const timer = setInterval(() => session.current?.ping(), 3000);
    const leave = () => session.current?.end();
    window.addEventListener('pagehide', leave);
    return () => { clearInterval(timer); window.removeEventListener('pagehide', leave); };
  }, []);

  async function play() {
    if (!audio.current || !place || !narration?.audioUrl) return;
    try { await audio.current.play(); setMessage(''); }
    catch { setMessage('Không phát được audio. Kiểm tra kết nối hoặc chọn ngôn ngữ khác rồi thử lại.'); }
  }

  if (!place && !active) return null;
  if (!place) return <section className="notice" role="alert"><h1>Không tìm thấy địa điểm.</h1><Link to="/explore">Về khám phá</Link></section>;
  return <><section className="tour-player" hidden={!active}><Link to={`/places/${place.id}`}>← Chi tiết địa điểm</Link><p className="eyebrow">FIELD NOTES / ĐANG LẮNG NGHE</p><h1>{place.name}</h1><label className="detail-language">Ngôn ngữ thuyết minh<select value={language} onChange={event => setLanguage(event.target.value)}>{!languages.some(item => item.code === language) && <option value={language}>{language}</option>}{languages.map(item => <option key={item.code} value={item.code}>{item.name}</option>)}</select></label><audio ref={audio} preload="metadata" src={narration?.audioUrl ? `/api/packages/stream?locationId=${place.id}&languageCode=${encodeURIComponent(language)}` : undefined} onPlay={() => { setPlaying(true); session.current?.start(audio.current?.currentTime || 0); }} onPause={() => setPlaying(false)} onEnded={() => { setPlaying(false); session.current?.end(); }} onTimeUpdate={event => { const seconds = event.currentTarget.currentTime; setTime(seconds); session.current?.tick(seconds, event.currentTarget.seeking); }} onLoadedMetadata={event => setDuration(event.currentTarget.duration)} onError={() => { if (narration?.audioUrl) setMessage('Không tải được âm thanh cho ngôn ngữ này.'); }} /><div className="player-controls"><button className="primary" disabled={!narration?.audioUrl} onClick={() => playing ? audio.current.pause() : play()}>{playing ? 'Tạm dừng' : 'Phát audio'}</button><label>Tua audio <input aria-label="Vị trí phát audio" type="range" min="0" max={Number.isFinite(duration) && duration > 0 ? duration : 0} value={time} onChange={event => { audio.current.currentTime = Number(event.target.value); session.current?.seek(audio.current.currentTime); setTime(audio.current.currentTime); }} disabled={!Number.isFinite(duration) || duration <= 0} /></label><span>{clock(time)} / {clock(duration)}</span></div>{message && <p role="alert" className="notice">{message} <button onClick={() => setAttempt(value => value + 1)}>Thử lại</button></p>}{loading ? <p role="status">Đang tải thuyết minh…</p> : !narration ? <p>Chưa có bản thuyết minh cho ngôn ngữ này. Hãy chọn ngôn ngữ khác.</p> : <><p>{!narration.audioUrl && 'Chưa có file audio cho ngôn ngữ này; bạn vẫn có thể đọc nội dung.'}</p><a className="detail-map-link" href={`/api/packages/offline?locationId=${place.id}&languageCode=${encodeURIComponent(language)}`} download>Tải gói ZIP nội dung và audio ↧</a><div className="player-transcript"><h2>Văn bản thuyết minh</h2><p>{narration.textContent || 'Chưa có văn bản thuyết minh.'}</p></div></>}</section>{!active && <aside className="mini-player" aria-label="Trình phát thu gọn"><span>{place.name} · {clock(time)}</span><button onClick={() => playing ? audio.current?.pause() : play()}>{playing ? 'Tạm dừng' : 'Tiếp tục'}</button><Link to={`/player/${place.id}?lang=${encodeURIComponent(language)}`}>Mở trình phát</Link></aside>}</>;
}
