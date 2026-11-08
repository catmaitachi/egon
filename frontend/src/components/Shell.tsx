import { useEffect, useId, type ReactNode } from 'react'
import { NavLink } from 'react-router'
import { routes } from '../content'
import { Logo } from './ui'

type AppShellProps = { children: ReactNode; aside?: ReactNode; nav?: ReactNode; narrow?: boolean; wide?: boolean; logoHref?: string }

// Moldura das telas de app. Sem `aside` o logo fica sozinho e centralizado, como no hero
// (cadastro, entrar, não encontrada); com `aside`, logo à esquerda e a conta à direita. `nav` é a
// barra de áreas do admin, logo abaixo.
// `narrow` limita o conteúdo a 42rem (formulários e escolhas); o padrão é 64rem. `wide` (telas de quem
// está logado) usa a tela até 100rem, com margem que cresce no desktop: painéis e listas sem faixas vazias.
export function AppShell({ children, aside, nav, narrow = false, wide = false, logoHref = '/' }: AppShellProps) {
  const largura = narrow ? 'max-w-2xl' : wide ? 'max-w-[100rem]' : 'max-w-5xl'
  const faixa = wide ? 'max-w-[100rem]' : 'max-w-5xl'
  // Cada tela começa do topo: a troca de rota do React Router mantém a rolagem da anterior.
  // Chaves de propósito: no Chrome novo o scrollTo devolve uma Promise, que o React tomaria por cleanup.
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' })
  }, [])

  return (
    <div className={`min-h-[100dvh] bg-night px-4 pb-24 sm:px-6 ${wide ? 'lg:px-10 2xl:px-14' : ''}`}>
      <header className={`mx-auto flex ${faixa} flex-wrap items-center gap-4 py-6 sm:py-8 ${aside ? 'justify-between' : 'justify-center'}`}>
        <Logo href={logoHref} />
        {aside}
      </header>
      {nav && <div className={`mx-auto ${faixa} pb-2`}>{nav}</div>}
      <main className={`mx-auto mt-4 sm:mt-6 ${largura}`}>{children}</main>
    </div>
  )
}

type PageTitleProps = { title: ReactNode; lead?: ReactNode; center?: boolean; children?: ReactNode }

// O h1 da tela, sem rótulo acima. `children` são as ações da página (à direita; embaixo no celular).
export function PageTitle({ title, lead, center = false, children }: PageTitleProps) {
  return (
    <div className={center ? 'flex flex-col items-center gap-6 text-center' : 'flex flex-wrap items-end justify-between gap-x-6 gap-y-5'}>
      <div className={`min-w-0 ${center ? 'flex flex-col items-center' : ''}`}>
        <h1 className="font-display text-3xl leading-[1.05] font-extrabold tracking-[-0.02em] break-words uppercase md:text-4xl">
          {title}
        </h1>
        {lead && <p className="mt-3 max-w-[40rem] text-base leading-relaxed text-ink-soft md:text-lg">{lead}</p>}
      </div>
      {children && <div className="flex flex-wrap gap-3">{children}</div>}
    </div>
  )
}

type PanelProps = { title?: ReactNode; actions?: ReactNode; flush?: boolean; className?: string; bodyClassName?: string; children: ReactNode }

// A superfície das telas de app: carvão, 1px de linha, 20px de canto. Nunca um painel dentro de outro.
// Com `title` vira uma <section> com h2. `flush` tira o padding do corpo para listas de borda a borda.
export function Panel({ title, actions, flush = false, className = '', bodyClassName = '', children }: PanelProps) {
  const titleId = useId()
  const body = flush ? (title ? 'mt-4 border-t border-line' : '') : title ? 'px-5 pt-4 pb-5 sm:px-7 sm:pb-7' : 'p-5 sm:p-7'
  const Tag = title ? 'section' : 'div'
  return (
    <Tag aria-labelledby={title ? titleId : undefined} className={`rounded-[20px] bg-coal ring-1 ring-line ${className}`}>
      {title && (
        <div className="flex flex-wrap items-center justify-between gap-3 px-5 pt-5 sm:px-7 sm:pt-6">
          <h2 id={titleId} className="font-display text-lg font-bold tracking-[-0.01em] uppercase">
            {title}
          </h2>
          {actions}
        </div>
      )}
      <div className={`${body} ${bodyClassName}`}>{children}</div>
    </Tag>
  )
}

const areas = [
  { to: routes.painel, label: 'Painel', end: true },
  { to: routes.admin.instituicoes, label: 'Instituições' },
  { to: routes.admin.cursos, label: 'Cursos' },
  { to: routes.admin.professores, label: 'Professores' },
  { to: routes.admin.alunos, label: 'Alunos' },
  { to: routes.admin.empresas, label: 'Empresas' },
  { to: routes.admin.admins, label: 'Admins' },
]

// As áreas do admin. No celular o trilho rola de lado em vez de quebrar em várias linhas.
export function ManageNav() {
  return (
    <nav aria-label="Áreas do admin" className="-mx-4 overflow-x-auto px-4 sm:mx-0 sm:px-0">
      <div className="flex w-max rounded-full bg-coal p-1.5 ring-1 ring-line">
      {areas.map((a) => (
        <NavLink
          key={a.to}
          to={a.to}
          end={a.end}
          className={({ isActive }) =>
            `flex h-11 items-center rounded-full px-4 font-display text-xs font-semibold tracking-wide uppercase transition-[color,background-color,transform] duration-200 active:scale-[0.98] sm:px-5 sm:text-sm ${
              isActive ? 'bg-snow text-night' : 'text-ink-soft hover:text-snow'
            }`
          }
        >
          {a.label}
        </NavLink>
      ))}
      </div>
    </nav>
  )
}
