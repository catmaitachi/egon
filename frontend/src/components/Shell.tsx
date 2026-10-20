import type { ReactNode } from 'react'
import { NavLink } from 'react-router'
import { routes } from '../content'
import { Logo } from './ui'

const areas = [
  { to: routes.students, label: 'Alunos' },
  { to: routes.partners, label: 'Empresas' },
]

// Moldura das telas fora da landing. Nos cadastros públicos o logo fica sozinho e
// centralizado, como no hero; na gestão, ganha a troca entre alunos e empresas.
export function Shell({ children, nav = false }: { children: ReactNode; nav?: boolean }) {
  return (
    <div className="min-h-[100dvh] px-4 pb-24 sm:px-6">
      <header className={`mx-auto flex max-w-5xl flex-wrap items-center gap-4 py-8 ${nav ? 'justify-between' : 'justify-center'}`}>
        <Logo href="/" />
        {nav && (
          <nav aria-label="Cadastros" className="flex rounded-full bg-coal p-1.5 ring-1 ring-line">
            {areas.map((a) => (
              <NavLink
                key={a.to}
                to={a.to}
                className={({ isActive }) =>
                  `flex h-10 items-center rounded-full px-4 font-display text-xs font-semibold tracking-wide uppercase transition-colors duration-300 sm:px-5 sm:text-sm ${
                    isActive ? 'bg-snow text-night' : 'text-ink-soft hover:text-snow'
                  }`
                }
              >
                {a.label}
              </NavLink>
            ))}
          </nav>
        )}
      </header>
      <main className="mx-auto max-w-5xl">{children}</main>
    </div>
  )
}

type PageTitleProps = { eyebrow: string; title: string; lead?: string; center?: boolean; children?: ReactNode }

export function PageTitle({ eyebrow, title, lead, center = false, children }: PageTitleProps) {
  return (
    <div className={`flex flex-wrap items-end gap-6 ${center ? 'flex-col items-center text-center' : 'justify-between'}`}>
      <div className={center ? 'flex flex-col items-center' : ''}>
        <p className="font-display text-xs font-semibold tracking-[0.18em] text-ink-soft uppercase">{eyebrow}</p>
        <h1 className="mt-3 font-display text-4xl leading-[1.02] font-extrabold tracking-[-0.02em] uppercase md:text-5xl">
          {title}
        </h1>
        {lead && <p className="mt-4 max-w-[40rem] text-lg leading-relaxed">{lead}</p>}
      </div>
      {children}
    </div>
  )
}

export function Panel({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-[20px] bg-coal ring-1 ring-line ${className}`}>{children}</div>
}
