import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig, type ProxyOptions } from 'vite'

// A API (backend/) responde em /alunos, /empresas... As telas usam esses mesmos caminhos,
// então as chamadas saem com o prefixo /api e o proxy o retira antes de repassar.
// ws: true repassa também o WebSocket dos avisos (/api/ws/notificacoes).
const api: Record<string, ProxyOptions> = {
  '/api': {
    target: 'http://localhost:8080',
    rewrite: (path) => path.replace(/^\/api/, ''),
    ws: true,
  },
}

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: { proxy: api },
  preview: { proxy: api },
})
