export function createListeningSession(send) {
  let active = false;
  let duration = 0;
  let lastPosition = null;
  return {
    start(position = 0) { if (!active) { active = true; duration = 0; lastPosition = position; send('JOIN', 0); } },
    tick(seconds, seeking = false) { if (active && Number.isFinite(seconds)) { const delta = seconds - lastPosition; lastPosition = seconds; if (!seeking && delta > 0 && delta <= 2) { const previous = Math.floor(duration); duration += delta; if (Math.floor(previous / 3) !== Math.floor(duration / 3)) send('PING', Math.floor(duration)); } } },
    seek(seconds) { if (active && Number.isFinite(seconds)) lastPosition = seconds; },
    ping() { if (active) send('PING', Math.floor(duration)); },
    end() { if (active) { active = false; send('LEAVE', Math.floor(duration)); } },
    get active() { return active; },
  };
}

export function sendListeningHeartbeat(deviceId, place, language, action, duration, request = fetch, triggerType = 'MANUAL') {
  return request('/api/analytics/heartbeat', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, keepalive: true,
    body: JSON.stringify({ deviceId, locationId: place.id, languageCode: language, action, listenDurationSeconds: duration, triggerType }),
  }).then(response => {
    if (!response.ok) throw new Error(`Heartbeat HTTP ${response.status}`);
    return response;
  });
}
