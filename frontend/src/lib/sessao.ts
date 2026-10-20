import { useSyncExternalStore } from 'react'

// Quem está logado (HU01). Fica no localStorage para sobreviver ao recarregar a página; o token
// vai no cabeçalho de cada requisição (lib/api.ts) e a API o recusa quando vence.

export type Perfil = 'ALUNO' | 'PROFESSOR' | 'EMPRESA'
export type Sessao = { token: string; perfil: Perfil; id: string; nome: string }

const CHAVE = 'egon.sessao'
const listeners = new Set<() => void>()

function ler(): Sessao | null {
  try {
    const texto = localStorage.getItem(CHAVE)
    return texto ? (JSON.parse(texto) as Sessao) : null
  } catch {
    return null
  }
}

let atual = ler()

function avisar() {
  listeners.forEach((l) => l())
}

export function guardarSessao(sessao: Sessao) {
  atual = sessao
  try {
    localStorage.setItem(CHAVE, JSON.stringify(sessao))
  } catch {
    // Sem armazenamento (aba anônima, por exemplo): a sessão vale até recarregar a página.
  }
  avisar()
}

export function encerrarSessao() {
  if (!atual) return
  atual = null
  try {
    localStorage.removeItem(CHAVE)
  } catch {
    // Nada a limpar.
  }
  avisar()
}

export const sessaoAtual = () => atual

export function useSessao() {
  return useSyncExternalStore(
    (l) => {
      listeners.add(l)
      return () => listeners.delete(l)
    },
    () => atual,
  )
}
