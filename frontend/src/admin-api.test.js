import test from 'node:test';
import assert from 'node:assert/strict';
import { login, adminRequest } from './admin-api.js';

test('login sends credentials to the real login endpoint and returns the token', async () => {
  const token = await login('admin', 'secret', async (url, options) => {
    assert.equal(url, '/api/auth/login');
    assert.deepEqual(JSON.parse(options.body), { username: 'admin', password: 'secret' });
    return new Response(JSON.stringify({ token: 'jwt' }), { status: 200 });
  });
  assert.equal(token, 'jwt');
});

test('admin requests include the bearer token and surface expired sessions', async () => {
  await assert.rejects(adminRequest('/api/analytics/dashboard', 'jwt', {}, async (url, options) => {
    assert.equal(options.headers.Authorization, 'Bearer jwt');
    return new Response(null, { status: 401 });
  }), /phiên/i);
});

test('admin writes send JSON and preserve server validation messages', async () => {
  await assert.rejects(adminRequest('/api/locations', 'jwt', { method: 'POST', body: { name: '' } }, async (url, options) => {
    assert.equal(options.headers['Content-Type'], 'application/json');
    assert.deepEqual(JSON.parse(options.body), { name: '' });
    return Response.json({ message: 'Tên không hợp lệ' }, { status: 400 });
  }), /Tên không hợp lệ/);
});

test('wrong current password keeps validation error distinct from expired session', async () => {
  const options = { method: 'POST', body: { oldPassword: 'wrong', newPassword: 'newpass' } };
  await assert.rejects(adminRequest('/api/auth/change-password', 'jwt', options, async () =>
    Response.json({ message: 'Mật khẩu cũ không chính xác' }, { status: 400 })),
  error => error.message === 'Mật khẩu cũ không chính xác' && error.status !== 401);
  await assert.rejects(adminRequest('/api/auth/change-password', 'expired', options, async () =>
    new Response(null, { status: 401 })), error => error.status === 401);
});
