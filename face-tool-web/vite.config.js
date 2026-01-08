import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  define: {
    global: 'globalThis',
  },
  optimizeDeps: {
    include: ['stompjs', 'sockjs-client']
  },
  server: {
    port: 3000,
    proxy: {
      '/ws': {
        target: 'http://localhost:28089',
        ws: true,
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:28089',
        changeOrigin: true
      }
    }
  }
})

