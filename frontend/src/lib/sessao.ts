import { createContext, useContext } from 'react'
import type { PerfilSessao, Sessao } from './api'

// Quem está logado. `undefined` enquanto /auth/eu não respondeu; `null` sem sessão.
export type EstadoSessao = {
  sessao: Sessao | null | undefined
  // A sessão acabou no meio do uso (expirou, saiu em outra aba, conta excluída).
  expirou: boolean
  entrar: (login: string, senha: string) => Promise<Sessao>
  // Modo dev: entra na conta escolhida sem senha (a API só aceita em dev).
  entrarComo: (perfil: PerfilSessao, id: string) => Promise<Sessao>
  sair: () => Promise<void>
}

export const SessaoContexto = createContext<EstadoSessao | null>(null)

export function useSessao() {
  const estado = useContext(SessaoContexto)
  if (!estado) throw new Error('useSessao fora do SessaoProvider')
  return estado
}

// A sessão de quem já se sabe que está logado (dentro de uma RotaProtegida).
export function useConta() {
  const { sessao } = useSessao()
  if (!sessao) throw new Error('useConta fora de uma rota protegida')
  return sessao
}

export const nomeDoPerfil: Record<PerfilSessao, string> = {
  ALUNO: 'Aluno',
  PROFESSOR: 'Professor',
  EMPRESA: 'Empresa',
  ADMIN: 'Admin',
}
