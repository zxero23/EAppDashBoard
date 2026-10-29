import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// En desarrollo, /api va al backend Spring Boot. El build se copia a los recursos estaticos del backend,
// asi el jar sirve front y API desde el mismo origen (cookies de sesion y CSRF sin CORS).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' },
  },
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
})
