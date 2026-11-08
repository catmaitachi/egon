import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { routes } from '../content'
import { api, SESSAO_EXPIRADA, type PerfilSessao, type Sessao } from '../lib/api'
import { SessaoContexto, useSessao } from '../lib/sessao'

// Pergunta à API quem está logado uma vez, ao abrir; depois segue o login, a saída e a sessão que expira.
export function SessaoProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null | undefined>(undefined)
  const [expirou, setExpirou] = useState(false)

  useEffect(() => {
    // Sem sessão ou sem conexão, o app abre deslogado; o erro de conexão aparece ao tentar entrar.
    api.auth.eu().then(setSessao, () => setSessao(null))
    const acabou = () => {
      setSessao((atual) => {
        if (atual) setExpirou(true)
        return null
      })
    }
    window.addEventListener(SESSAO_EXPIRADA, acabou)
    return () => window.removeEventListener(SESSAO_EXPIRADA, acabou)
  }, [])

  const entrar = useCallback(async (login: string, senha: string) => {
    const nova = await api.auth.entrar(login, senha)
    setExpirou(false)
    setSessao(nova)
    return nova
  }, [])

  const entrarComo = useCallback(async (perfil: PerfilSessao, id: string) => {
    const nova = await api.dev.entrar(perfil, id)
    setExpirou(false)
    setSessao(nova)
    return nova
  }, [])

  const sair = useCallback(async () => {
    await api.auth.sair().catch(() => undefined)
    setExpirou(false)
    setSessao(null)
  }, [])

  const valor = useMemo(() => ({ sessao, expirou, entrar, entrarComo, sair }), [sessao, expirou, entrar, entrarComo, sair])
  return <SessaoContexto value={valor}>{children}</SessaoContexto>
}

// Só abre para quem está logado num dos perfis aceitos. Sem sessão vai ao login (e volta depois);
// com outro perfil, ao painel dele.
export function RotaProtegida({ perfis, children }: { perfis?: PerfilSessao[]; children: ReactNode }) {
  const { sessao } = useSessao()
  const local = useLocation()
  if (sessao === undefined) {
    return (
      <p role="status" className="sr-only">
        Carregando…
      </p>
    )
  }
  if (!sessao) {
    return <Navigate to={`${routes.login}?volta=${encodeURIComponent(local.pathname + local.search)}`} replace />
  }
  if (perfis && !perfis.includes(sessao.perfil)) return <Navigate to={routes.painel} replace />
  return children
}
