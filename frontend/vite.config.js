import { defineConfig } from 'vite';

export default defineConfig({
  base: '/next/',
  server: {
    proxy: {
      '/api': 'http://127.0.0.1:8080',
      '/player.html': 'http://127.0.0.1:8080',
      '/index.html': 'http://127.0.0.1:8080',
    },
  },
});
