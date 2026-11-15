import React, { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useSearchParams } from 'react-router-dom';
import { getLanguages, preferredLanguage, savePreferredLanguage } from './detail.js';
import { createListeningSession, listeningTrigger, sendListeningHeartbeat } from './listening.js';

const clock = seconds => Number.isFinite(seconds) ? `${Math.floor(seconds / 60).toString().padStart(2, '0')}:${Math.floor(seconds % 60).toString().padStart(2, '0')}` : '--:--';

export function AudioPlayer({ places, active, id }) {
  const [params] = useSearchParams();
  const { state } = useLocation();
  const trigger = useRef('MANUAL');
  if (active) trigger.current = listeningTrigger(state);
  const [selectedId, setSelectedId] = useState(null);
  useEffect(() => { if (active) setSelectedId(id); }, [active, id]);
  const place = places.find(item => String(item.id) === (active ? id : selectedId));
  const [language, updateLanguage] = useState('vi');
  const setLanguage = code => { updateLanguage(code); savePreferredLanguage(code); };
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
  const urlLanguage = params.get('lang');
  useEffect(() => {
    if (!active || !languages.length) return;
    const code = preferredLanguage(languages, urlLanguage);
    updateLanguage(code);
    if (code === urlLanguage) savePreferredLanguage(code);
  }, [active, id, languages, urlLanguage]);

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
      }).then(data => { if (!controller.signal.aborted) setNarration(data ? { ...data, locationId: place.id, languageCode: language } : null); })
      .catch(error => { if (!controller.signal.aborted) setMessage(error.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [place?.id, language, attempt]);

  // One audio element and one session for the mounted app, including route changes.
  useEffect(() => {
    const previous = current.current;
    if (previous && (previous.id !== place?.id || previous.language !== language || previous.triggerType !== trigger.current)) {
      session.current?.end();
      if (audio.current) { audio.current.pause(); audio.current.removeAttribute('src'); audio.current.load(); }
      setPlaying(false); setTime(0); setDuration(0);
    }
    current.current = place ? { id: place.id, language, triggerType: trigger.current } : null;
    if (!place) return;
    const triggerType = trigger.current;
    session.current = createListeningSession((action, seconds) => {
      sendListeningHeartbeat(device.current, place, language, action, seconds, fetch, triggerType).catch(() => setMessage('Không gửi được trạng thái phiên nghe.'));
    });
  }, [place?.id, language, trigger.current]);

  useEffect(() => () => {
    session.current?.end();
    audio.current?.pause();
  }, []);

  useEffect(() => {
    const timer = setInterval(() => session.current?.ping(), 3000);
    const leave = () => session.current?.end();
    const restore = event => { if (event.persisted && audio.current && !audio.current.paused) session.current?.start(audio.current.currentTime); };
    window.addEventListener('pagehide', leave);
    window.addEventListener('pageshow', restore);
    return () => { clearInterval(timer); window.removeEventListener('pagehide', leave); window.removeEventListener('pageshow', restore); };
  }, []);

  async function play() {
    if (!audio.current || !place || !availableNarration?.audioUrl) return;
    try { await audio.current.play(); setMessage(''); }
    catch { session.current?.end(); setPlaying(false); setMessage('Không phát được audio. Kiểm tra kết nối hoặc chọn ngôn ngữ khác rồi thử lại.'); }
  }

  const availableNarration = narration?.locationId === place?.id && narration?.languageCode === language ? narration : null;

  if (!place && !active) return null;
  if (!place) return <section className="notice" role="alert"><h1>Không tìm thấy địa điểm.</h1><Link to="/explore">Về khám phá</Link></section>;
  return <><section className="tour-player" hidden={!active}><Link to={`/places/${place.id}`} state={trigger.current === 'QR_CODE' ? { triggerType: 'QR_CODE' } : undefined}>← Chi tiết địa điểm</Link><p className="eyebrow">FIELD NOTES / ĐANG LẮNG NGHE</p><h1>{place.name}</h1><label className="detail-language">Ngôn ngữ thuyết minh<select value={language} onChange={event => setLanguage(event.target.value)}>{!languages.some(item => item.code === language) && <option value={language}>{language}</option>}{languages.map(item => <option key={item.code} value={item.code}>{item.name}</option>)}</select></label><audio ref={audio} preload="metadata" src={availableNarration?.audioUrl ? `/api/packages/stream?locationId=${place.id}&languageCode=${encodeURIComponent(language)}` : undefined} onPlay={() => { setPlaying(true); session.current?.start(audio.current?.currentTime || 0); }} onPause={() => setPlaying(false)} onEnded={() => { setPlaying(false); session.current?.end(); }} onSeeking={event => session.current?.seek(event.currentTarget.currentTime)} onSeeked={event => session.current?.seek(event.currentTarget.currentTime)} onTimeUpdate={event => { const seconds = event.currentTarget.currentTime; setTime(seconds); session.current?.tick(seconds, event.currentTarget.seeking); }} onLoadedMetadata={event => setDuration(event.currentTarget.duration)} onError={() => { if (availableNarration?.audioUrl) { session.current?.end(); setPlaying(false); setMessage('Không tải được âm thanh cho ngôn ngữ này.'); } }} /><div className="player-controls"><button className="primary" disabled={!availableNarration?.audioUrl} onClick={() => playing ? audio.current.pause() : play()}>{playing ? 'Tạm dừng' : 'Phát audio'}</button><label>Tua audio <input aria-label="Vị trí phát audio" type="range" min="0" max={Number.isFinite(duration) && duration > 0 ? duration : 0} value={time} onChange={event => { audio.current.currentTime = Number(event.target.value); session.current?.seek(audio.current.currentTime); setTime(audio.current.currentTime); }} disabled={!Number.isFinite(duration) || duration <= 0} /></label><span>{clock(time)} / {clock(duration)}</span></div>{message && <p role="alert" className="notice">{message} <button onClick={() => setAttempt(value => value + 1)}>Thử lại</button></p>}{loading ? <p role="status">Đang tải thuyết minh…</p> : !availableNarration ? <p>Chưa có bản thuyết minh cho ngôn ngữ này. Hãy chọn ngôn ngữ khác.</p> : <><p>{!availableNarration.audioUrl && 'Chưa có file audio cho ngônữ này; bạn vẫn có thể đọc nội dung.'}</p><a className="detail-map-link" href={`/api/packages/offline?locationId=${place.id}&languageCode=${encodeURIComponent(language)}&deviceId=${encodeURIComponent(device.current)}`} download>Tải gói ZIP nội dung và audio ↧</a><div className="player-transcript"><h2>Văn bản thuyết minh</h2><p>{availableNarration.textContent || 'Chưa có văn bản thuyết minh.'}</p></div></>}</section>{!active && <aside className="mini-player" aria-label="Trình phát thu gọn"><span>{place.name} · {clock(time)}</span><button onClick={() => playing ? audio.current?.pause() : play()}>{playing ? 'Tạm dừng' : 'Tiếp tục'}</button><Link to={`/player/${place.id}?lang=${encodeURIComponent(language)}`} state={trigger.current === 'QR_CODE' ? { triggerType: 'QR_CODE' } : undefined}>Mở trình phát</Link></aside>}</>;
}
