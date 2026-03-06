import { defineConfig } from 'vite';

export default defineConfig({
  root: 'src',
  build: {
    outDir: '../dist',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      // All API traffic (OPAQUE, OPRF, notes) goes to the Dropwizard backend
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
