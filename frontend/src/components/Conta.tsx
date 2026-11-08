import { useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router'
import { SignOutIcon } from '@phosphor-icons/react'
import { routes } from '../content'
import { nomeDoPerfil, useConta, useSessao } from '../lib/sessao'
import { ActionButton } from './controls'
import { AppShell, ManageNav } from './Shell'

// Quem está logado e a saída, no canto do cabeçalho.
function Conta() {
  const conta = useConta()
  const { sair } = useSessao()
  const navigate = useNavigate()
  const [saindo, setSaindo] = useState(false)
  return (
    <div className="flex min-w-0 items-center gap-3">
      <p className="min-w-0 text-right leading-tight">
        <span className="block max-w-[11rem] truncate font-medium sm:max-w-[16rem]">{conta.nome}</span>
        <span className="text-sm text-ink-soft">{nomeDoPerfil[conta.perfil]}</span>
      </p>
      <ActionButton
        variant="quiet"
        busy={saindo}
        onClick={async () => {
          setSaindo(true)
          await sair()
          navigate(routes.login, { replace: true })
        }}
      >
        <SignOutIcon size={18} aria-hidden />
        Sair
      </ActionButton>
    </div>
  )
}

// Moldura das telas de quem está logado: logo leva ao painel, a conta à direita e, para o admin,
// a barra das áreas de gestão.
export function TelaLogada({ children, narrow = false }: { children: ReactNode; narrow?: boolean }) {
  const conta = useConta()
  return (
    <AppShell aside={<Conta />} nav={conta.perfil === 'ADMIN' ? <ManageNav /> : undefined} logoHref={routes.painel} narrow={narrow} wide={!narrow}>
      {children}
    </AppShell>
  )
}
