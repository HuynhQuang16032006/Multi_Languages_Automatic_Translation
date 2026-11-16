export async function login(username, password, request = fetch) {
  const response = await request('/api/auth/login', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) {
    throw new Error(response.status === 429 ? 'Quá nhiều lần đăng nhập. Hãy thử lại sau.' : response.status === 401 ? 'Tên đăng nhập hoặc mật khẩu không đúng.' : 'Không thể đăng nhập. Hãy thử lại.');
  }
  const data = await response.json();
  if (!data.token) throw new Error('Phản hồi đăng nhập không hợp lệ.');
  return data.token;
}

export async function adminRequest(url, token, options = {}, request = fetch) {
  const headers = { ...options.headers, Authorization: `Bearer ${token}` };
  let body = options.body;
  if (body != null && typeof body !== 'string') { headers['Content-Type'] = 'application/json'; body = JSON.stringify(body); }
  const response = await request(url, { ...options, body, headers });
  if (response.status === 401) { const error = new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'); error.status = 401; throw error; }
  if (!response.ok) {
    let message = '';
    if (response.status < 500) { try { const data = await response.json(); message = typeof data.message === 'string' ? data.message : ''; } catch { /* fallback below */ } }
    throw new Error(message || `Yêu cầu thất bại (HTTP ${response.status}).`);
  }
  return response.status === 204 ? null : response.json();
}
