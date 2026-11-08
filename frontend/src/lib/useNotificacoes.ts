import { useEffect, useRef, useState } from 'react'
import { urlNotificacoes, type Notificacao } from './api'

export type EstadoConexao = 'conectando' | 'ao-vivo' | 'reconectando'

type Handlers = {
  onMensagem: (n: Notificacao) => void
  // Chamado sempre que a conexão abre, inclusive a primeira: o que chegou enquanto ela
  // estava fechada (ou antes de abrir) só aparece recarregando o extrato.
  onConectar?: () => void
}

const ESPERA_MAX = 30_000

// Avisos ao vivo de quem está logado (a pessoa vem da sessão, no cookie do handshake). Se a conexão
// cai, tenta de novo esperando 1s, 2s, 4s... até 30s. Com `ativo` falso não conecta.
export function useNotificacoes(handlers: Handlers, ativo = true): EstadoConexao {
  const [estado, setEstado] = useState<EstadoConexao>('conectando')
  // Os handlers mudam a cada render; a conexão não deve reabrir por isso.
  const latest = useRef(handlers)
  useEffect(() => {
    latest.current = handlers
  })

  useEffect(() => {
    if (!ativo) return
    let socket: WebSocket | null = null
    let timer = 0
    let tentativas = 0
    let abriu = false
    let parado = false

    const conectar = () => {
      socket = new WebSocket(urlNotificacoes())
      socket.onopen = () => {
        abriu = true
        tentativas = 0
        setEstado('ao-vivo')
        latest.current.onConectar?.()
      }
      socket.onmessage = (e) => {
        let mensagem: Notificacao
        try {
          mensagem = JSON.parse(e.data)
        } catch {
          return
        }
        latest.current.onMensagem(mensagem)
      }
      socket.onclose = () => {
        if (parado) return
        setEstado(abriu ? 'reconectando' : 'conectando')
        timer = window.setTimeout(conectar, Math.min(ESPERA_MAX, 1000 * 2 ** tentativas++))
      }
    }

    conectar()
    return () => {
      parado = true
      window.clearTimeout(timer)
      socket?.close()
    }
  }, [ativo])

  return estado
}
