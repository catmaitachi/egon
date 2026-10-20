import { useId, useState, type ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import {
  ArrowDownLeftIcon,
  ArrowUpRightIcon,
  CalendarCheckIcon,
  EnvelopeSimpleIcon,
  SignOutIcon,
  TicketIcon,
  type Icon,
} from '@phosphor-icons/react'
import { coins, painelDe, quando, routes } from '../content'
import type { Aviso, Lancamento } from '../lib/api'
import { encerrarSessao, useSessao, type Perfil } from '../lib/sessao'
import { Coin } from './Coin'
import { Panel } from './Shell'
import { Logo } from './ui'

const nomePerfil: Record<Perfil, string> = { ALUNO: 'Aluno', PROFESSOR: 'Professor', EMPRESA: 'Empresa parceira' }

// Moldura das áreas logadas. Sem sessão, manda para o login; com outro perfil, para o painel certo.
export function PainelShell({ perfil, children }: { perfil: Perfil; children: ReactNode }) {
  const sessao = useSessao()
  const location = useLocation()
  // Quem clicou em Sair vai para o início; quem perdeu a sessão (token vencido), para o login.
  const [saindo, setSaindo] = useState(false)

  if (!sessao) {
    return saindo ? <Navigate to="/" replace /> : <Navigate to={routes.login} replace state={{ de: location.pathname }} />
  }
  if (sessao.perfil !== perfil) return <Navigate to={painelDe[sessao.perfil]} replace />

  const sair = () => {
    setSaindo(true)
    encerrarSessao()
  }

  return (
    <div className="min-h-[100dvh] px-4 pb-24 sm:px-6">
      <header className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-4 py-8">
        <Logo href="/" />
        <div className="flex items-center gap-4">
          <div className="text-right">
            <p className="font-display text-sm font-semibold">{sessao.nome}</p>
            <p className="text-xs text-ink-soft">{nomePerfil[sessao.perfil]}</p>
          </div>
          <button
            type="button"
            onClick={sair}
            className="flex h-11 items-center gap-2 rounded-full px-4 font-display text-xs font-semibold tracking-wide text-ink-soft uppercase ring-1 ring-line transition-colors duration-300 hover:text-snow hover:ring-snow/60"
          >
            <SignOutIcon size={18} />
            Sair
          </button>
        </div>
      </header>
      <main className="mx-auto max-w-5xl">{children}</main>
    </div>
  )
}

// O saldo é o número que importa na tela: grande, com a moeda ao lado.
export function Saldo({ valor, rotulo, children }: { valor: number | null; rotulo: string; children?: ReactNode }) {
  return (
    <Panel className="flex flex-wrap items-center justify-between gap-6 px-6 py-7 md:px-9">
      <div className="flex items-center gap-5">
        <Coin size={52} glow />
        <div>
          <p className="font-display text-xs font-semibold tracking-[0.18em] text-ink-soft uppercase">{rotulo}</p>
          <p className="mt-1 font-display text-4xl font-extrabold tracking-tight tabular-nums md:text-5xl" aria-live="polite">
            {valor === null ? '…' : coins(valor)}
          </p>
        </div>
      </div>
      {children}
    </Panel>
  )
}

// Custo em Egos: o selo branco com a moeda, como os preços da landing.
export function Preco({ valor }: { valor: number }) {
  return (
    <span className="flex shrink-0 items-center gap-1.5 rounded-full bg-snow py-1 pr-3.5 pl-1 font-display text-sm font-bold text-night tabular-nums">
      <Coin size={18} />
      {coins(valor)}
    </span>
  )
}

type Aba<T extends string> = { id: T; label: string; icon: Icon }

// Abas do painel, no mesmo controle em pílula da landing. Setas trocam de aba pelo teclado.
export function Abas<T extends string>({ abas, atual, onChange }: { abas: Aba<T>[]; atual: T; onChange: (id: T) => void }) {
  const base = useId()
  const mover = (e: React.KeyboardEvent, i: number) => {
    const proxima = e.key === 'ArrowRight' ? (i + 1) % abas.length : e.key === 'ArrowLeft' ? (i - 1 + abas.length) % abas.length : null
    if (proxima === null) return
    e.preventDefault()
    onChange(abas[proxima].id)
    document.getElementById(`${base}-${abas[proxima].id}`)?.focus()
  }
  return (
    <div role="tablist" className="flex w-fit max-w-full flex-wrap rounded-[26px] bg-coal p-1.5 ring-1 ring-line">
      {abas.map((aba, i) => {
        const ativa = aba.id === atual
        const Glyph = aba.icon
        return (
          <button
            key={aba.id}
            id={`${base}-${aba.id}`}
            type="button"
            role="tab"
            aria-selected={ativa}
            tabIndex={ativa ? 0 : -1}
            onClick={() => onChange(aba.id)}
            onKeyDown={(e) => mover(e, i)}
            className={`flex h-11 items-center gap-2 rounded-full px-3.5 font-display text-xs font-semibold tracking-wide uppercase transition-colors duration-300 sm:px-5 sm:text-sm ${
              ativa ? 'bg-snow text-night' : 'text-ink-soft hover:text-snow'
            }`}
          >
            <Glyph size={18} weight={ativa ? 'fill' : 'regular'} className="hidden sm:block" />
            {aba.label}
          </button>
        )
      })}
    </div>
  )
}

function Vazio({ children }: { children: ReactNode }) {
  return <Panel className="px-7 py-12 text-center text-ink-soft">{children}</Panel>
}

function Carregando() {
  return (
    <Panel className="px-7 py-10 text-center text-ink-soft">
      <p role="status">Carregando…</p>
    </Panel>
  )
}

// Uma linha do extrato. O que aparece depende da origem: envio, troca ou cota semestral.
function Linha({ l }: { l: Lancamento }) {
  const entrada = l.tipo === 'ENTRADA'
  const Glyph = l.origem === 'TROCA' ? TicketIcon : l.origem === 'COTA' ? CalendarCheckIcon : entrada ? ArrowDownLeftIcon : ArrowUpRightIcon
  const titulo =
    l.origem === 'COTA'
      ? `Cota do semestre ${l.semestre}`
      : l.origem === 'TROCA'
        ? `${l.vantagem} · ${l.empresa}`
        : entrada
          ? `De ${l.pessoa}`
          : `Para ${l.pessoa}`
  return (
    <li className="flex gap-4 px-6 py-5 md:px-8">
      <span
        className={`mt-0.5 grid size-10 shrink-0 place-items-center rounded-full ring-1 ${entrada ? 'text-snow ring-snow/40' : 'text-ink-soft ring-line'}`}
      >
        <Glyph size={20} />
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1">
          <p className="font-display font-semibold">{titulo}</p>
          <p className={`font-display font-bold tabular-nums ${entrada ? 'text-snow' : 'text-ink-soft'}`}>
            {entrada ? '+' : '−'}
            {coins(l.valor)}
          </p>
        </div>
        <p className="mt-1 text-sm text-ink-soft">{quando(l.data)}</p>
        {l.motivo && <p className="mt-2 leading-relaxed">“{l.motivo}”</p>}
        {l.codigoCupom && (
          <p className="mt-2 text-sm">
            Cupom <span className="font-display font-semibold tracking-wider">{l.codigoCupom}</span>
          </p>
        )}
      </div>
    </li>
  )
}

export function ExtratoLista({ lancamentos, vazio }: { lancamentos: Lancamento[] | null; vazio: string }) {
  if (!lancamentos) return <Carregando />
  if (lancamentos.length === 0) return <Vazio>{vazio}</Vazio>
  return (
    <div className="grid gap-3">
      <p className="text-sm text-ink-soft">Mais recentes primeiro.</p>
      <Panel>
        <ul className="divide-y divide-line">
          {lancamentos.map((l, i) => (
            <Linha key={`${l.data}-${i}`} l={l} />
          ))}
        </ul>
      </Panel>
    </div>
  )
}

// A caixa de e-mails que o sistema mandou (HU07, HU10), para quem não tem SMTP configurado
// também conseguir ler. Cada aviso abre e fecha.
export function AvisosLista({ avisos }: { avisos: Aviso[] | null }) {
  if (!avisos) return <Carregando />
  if (avisos.length === 0) return <Vazio>Nenhum e-mail ainda.</Vazio>
  return (
    <div className="grid gap-3">
      <p className="text-sm text-ink-soft">Cópia dos e-mails enviados para o seu endereço cadastrado.</p>
      <Panel>
        <ul className="divide-y divide-line">
          {avisos.map((a) => (
            <li key={a.id}>
              <details className="group px-6 py-5 md:px-8">
                <summary className="flex cursor-pointer list-none items-start gap-4 rounded-[12px]">
                  <EnvelopeSimpleIcon size={22} className="mt-0.5 shrink-0 text-ink-soft" aria-hidden />
                  <span className="min-w-0 flex-1">
                    <span className="block font-display font-semibold">{a.assunto}</span>
                    <span className="mt-1 block text-sm text-ink-soft">{quando(a.data)}</span>
                  </span>
                  <span className="text-sm text-ink-soft group-open:hidden">Abrir</span>
                  <span className="hidden text-sm text-ink-soft group-open:inline">Fechar</span>
                </summary>
                <p className="mt-4 ml-[38px] leading-relaxed whitespace-pre-line">{a.descricao}</p>
              </details>
            </li>
          ))}
        </ul>
      </Panel>
    </div>
  )
}

export { Carregando, Vazio }
