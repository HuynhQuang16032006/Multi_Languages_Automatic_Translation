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
